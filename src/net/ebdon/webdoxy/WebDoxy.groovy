package net.ebdon.webdoxy;

import java.time.temporal.IsoFields;
import java.time.temporal.ChronoUnit;
import groovy.ant.AntBuilder          // AntBuilder has moved.
import org.codehaus.groovy.ant.FileScanner
import groovy.cli.picocli.CliBuilder;

/**
 * @file
 * @author  Terry Ebdon
 * @date    2017-06-30
 * @copyright
 *
 * Copyright 2017 Terry Ebdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
@author Terry Ebdon

@brief Groovy script to build web-sites using Doxygen for static generation.

@todo
<ul>
 <li> Use per-site configuration files.
 <li> Eliminate dot.cmd via a Doxyfile configuration?
 <li> Delete temp Doxygen cfg file when build fails. i.e. catch the exception & delete.
 <li> Only check for Perl if citations are used.
 <li> Move the perl command and version string into the configuration file.
 <li> Only check for latex if latex output is required.
 <li> Move the latex command and version string into the configuration file.
 <li> Only check for Dot if Dot output is required.
 <li> Move the Dot command and version string into the configuration file.
</ul>

@todo Move all string constants into config.groovy
@todo Move all message strings into Language.properties
@todo Run the Latex make.cmd fie, if present
 - check for Latex build errors.
 - check that refman.pdf has been created
@todo Is there a way to specify the name of the generated PDF?
If not then add code to rename the PDF after it's been created.
@note The build depends on dot.cmd, which is a wrapper script to run
the dot executable . This allows dot to be used, by Doxygen,
without being on the Windows path.

# Overview
At run-time the script will:
1. Scan all input folders, checking files for obvious errors that may not be spotted by Doxygen
2. Generate documentation for each configuration

# Process

For each configured site (live, staged, draft...)
 - create a config file
 - Add the config specific input folders
 - Add the config name as an enabled section
 - Change the output folder name to be configuration specific
 - delete output of the last run
 - Generate documentation via doxygen

 */

@groovy.util.logging.Log4j2('logger')
class WebDoxy {
  static final defaultConfigs = [ 'live', 'staged' ];
  static Resource resource = new Resource();

  def projects = []; ///< List of projects that the command(s) will apply to
  Boolean doxygenInitialised = false;
  def buildConfig;
  static AntBuilder ant = new AntBuilder();
  final def cliOptions;

  public static main( args ) {
    logger.info "args: $args"

    def cli = new CliBuilder(usage: resource.message('help.usage') )

    cli.with {
      h args: 0, longOpt: 'help',                   resource.message( 'help.help' )
      c args: 0, longOpt: 'create',                 resource.message( 'help.create' )
      g args: 0, longOpt: 'generate',               resource.message( 'help.generate' )
      b args: 0, longOpt: 'backup',                 resource.message( 'help.backup' )
      v args: 0, longOpt: 'validate',               resource.message( 'help.validate' )
      t args: 0, longOpt: 'toc',                    resource.message( 'help.toc' )
      j args: 0, longOpt: 'journal',                resource.message( 'help.journal' )
      d args: 1, longOpt: 'date',                   resource.message( 'help.date' )
      n args: 1, longOpt: 'number', type: Integer,  resource.message( 'help.number' )
      p args: 1, longOpt: 'project',                resource.message( 'help.project' )
      s args: 0, longOpt: 'stub',                   resource.message( 'help.stub' )
      w args: 0, longOpt: 'week',                   resource.message( 'help.week' )
    }

    def options = cli.parse(args)
    logger.info "CliBuilder.arguments: ${options.arguments()}"
    if (options) {
      logger.info "Working..."
      def before = System.currentTimeMillis()
      WebDoxy build = new WebDoxy( options )

      if (options.help) {
        println "\n"
        cli.usage()
        return
      }
      if ( options.create ) {
        build.create()
      }
      if ( options.journal ) {
        build.addDiaryPage()
      }
      if ( options.week ) {
        build.addWeeklyPage()
      }

      if ( options.validate ) { // before generate, to guarantee warning appears
        build.validate()
      }

      if ( options.generate ) {
        build.generate()
      }
      if ( options.toc ) {
        build.createToc()
      }
      if ( options.backup ) {
        new Backup().run()
      }

      if ( options.stub) {
        if ( options.p ) {
          build.stubs()
        } else {
          ant.fail resource.message( 'webDoxy.noProjectName' )
        }
      }

      def after = System.currentTimeMillis()
      logger.info "WebDoxy run completed in ${(after-before)/1000} seconds"
    } else {
      logger.warn 'No options provided, nothing to do.'
    }
  }

  WebDoxy( options ) {
    checkInstall()
    final String configFileName = 'config.groovy'

    File configFile = new File( configFileName )

    if ( configFile.exists() ) {
      logger.info "Using config file: ${configFile.absolutePath}"
      buildConfig = new ConfigSlurper().parse( configFile.toURI().toURL())

      if ( options.project ) {
        projects = [ options.project ]
      } else {
        projects = options.arguments() ?: buildConfig.defaultProjects
      }

      cliOptions = options
      initDoxygen()
    } else {
      ant.fail(
        resource.message(
          'webDoxy.noConfigFile',
          [configFile.absolutePath] as Object[]
        )
      )
    }
  }

  void checkInstall() {
    //~ assert "perl --version".execute().text.contains('This is perl 5')
    //~ assert "latex --version".execute().text.contains('pdfTeX')
    //~ final String dotProperty = System.env['GRAPHVIZ_DOT']
    //~ assert dotProperty
    //~ // assert dotProperty.length() > 5
    //~ assert new File(dotProperty).exists()
    //~ assert "dot -?".execute().text.contains('Usage: dot')
  }

  final void validate() {
    if ( !cliOptions.generate ) {
      validateMarkDown()
    } else {
      logger.error resource.message( 'webDoxy.validateSuperfluous' )
        //> Assumes validate is called before generate
    }
  }

  void stubs() {
    if ( cliOptions.arguments().size ) {
      logger.info "Adding stubs to projects: ${projects.join(', ')}"
      logger.info "Stubs are: ${cliOptions.arguments()}"

      projects.each { projectName ->
        Project project = new Project( projectName, buildConfig )
        cliOptions.arguments().each { pageName ->
          project.createStub pageName
        }
      }
    } else {
      logger.warn resource.message( 'webDoxy.stubs.noStubList' )
    }
  }

  void addDiaryPage() {
    logger.info "Adding journal page to projects in list: $projects"
    projects.each { projectName ->
      logger.info "Adding journal page to project: $projectName"

      Date pageDate = targetDate

      int numPagesWanted = cliOptions.number ?: 1
      logger.info "Generating $numPagesWanted journal pages."
      logger.info "Max pages allowed: $maxPages"

      if ( numPagesWanted > 0 && numPagesWanted <= maxPages ) {
        new JournalProject( projectName, buildConfig ).with {
          numPagesWanted.times {
            logger.info "Generating journal page for ${pageDate}"
            createPage( pageDate++ )
          }
        }
      } else {
        ant.fail(
          resource().message(
            'webDoxy.badNumPages',
            [numPagesWanted,maxPages] as Object[]
          )
        )
      }
    }
  }

  Date getTargetDate() {
    Date pageDate = new Date()

    if ( cliOptions.date ) {
      pageDate = Date.parse( buildConfig.datePattern, cliOptions.date )
    } else {
      logger.warn 'Date not specified, defaulting to today.'
    }
    pageDate
  }

  int getDateIncrement() {
    cliOptions.week ? 7 : 1
  }

  int getMaxPages() {
    cliOptions.week ? 53 : 366
  }

  void addWeeklyPage() {
    logger.info "Adding weekly page to projects: ${projects}"
    projects.each { projectName ->
      logger.info "Adding weekly page to: $projectName"
      def pageDate = targetDate

      logger.info 'Creating a weekly page'
      final zonedPageDate = pageDate.toZonedDateTime()
      final pageYear      = zonedPageDate.get( IsoFields.WEEK_BASED_YEAR )
      final pageWeek      = zonedPageDate.get( IsoFields.WEEK_OF_WEEK_BASED_YEAR )

      logger.info "Creating a weekly page for year $pageYear, week $pageWeek"
      logger.info "Max pages allowed: $maxPages"

      final int numPagesWanted = cliOptions.number ?: 1
      logger.info "Generating $numPagesWanted journal pages with increment of $dateIncrement."
      if ( numPagesWanted > 0 && numPagesWanted <= maxPages ) {
        try {
          final WeeklyProject weekProj = new WeeklyProject( projectName, buildConfig )
          numPagesWanted.times {
            weekProj.createPage( pageDate )
            zonedPageDate.plus( dateIncrement, ChronoUnit.DAYS)
          }
        } catch ( final Exception ex ) {
          logger.fatal "Failed to create weekly pages - ${ex.message}"
          throw ex
        }
      } else {
        ant.fail(
          resource().message(
            'webDoxy.badNumPages',
            [numPagesWanted,maxPages] as Object[]
          )
        )
      }
    }
  }

  void createToc() {
    TocProject toc = new TocProject( buildConfig )
    toc.create()
    buildProject toc.name
    cleanUp()
  }

  void create() {
    projects.each { name ->
      logger.info "Creating project $name"
      new Project( name, buildConfig ).create()
    }
  }

  void generate() {
    assert projects.size()
    validateMarkDown()
    build()
    cleanUp()
  }

  void initDoxygen() {
    if ( !doxygenInitialised ) {
      logger.debug  "buildConfig.doxygen.path $buildConfig.doxygen.path"
      logger.info   "buildConfig.doxygen.ant.classPath $buildConfig.doxygen.ant.classPath"
      logger.debug  "buildConfig.doxygen.ant.className $buildConfig.doxygen.ant.className"
      ant.taskdef(
        name: 'doxygen',
        classname: buildConfig.doxygen.ant.className,
        classpath: buildConfig.doxygen.ant.classPath
      )
      doxygenInitialised = true
    }
  }

  void cleanUp() {
    ant.delete( verbose: buildConfig.verboseCleanUp ) {
      fileset( dir: tempFolder ) {
        include name: 'doxygen*.cfg'
      }
    }
  }

  private String getTempFolder() {
    System.properties[ 'java.io.tmpdir' ]
  }

  void build() {
    logger.info "Building $projects"
    projects.each { projectName ->
      buildProject projectName
    }
    logger.info "Success: Built projects $projects"
  }

  void buildProject( projectName ) {
    Project project = new Project( projectName, buildConfig )
    final BigDecimal before = System.currentTimeMillis()
    logger.info  "Building project: ${project.name}"
    logger.debug "Building project: $project"
    project.cleanOutputFolders()

    ant.doxygen(
        verbose: buildConfig.doxygen.verbose,
        configFilename: project.configFileName,
        doxygenPath: buildConfig.doxygen.path ) {
      //property( name: 'PROJECT_NAME', value: "$config Portfolio" )
      //property( name: 'ENABLED_SECTIONS', value: config )
    }

    final BigDecimal after          = System.currentTimeMillis()
    final BigDecimal runTimeSeconds = (after - before) / 1000

    logger.info(
      resource.message(
        'webDoxy.buildProject.done',
        [projectName, runTimeSeconds] as Object[]
      )
    )
  }

  void validateMarkDown() {
    logger.info (
      resource.message(
        'webDoxy.validateMarkDown',
        [projects.toString()] as Object[]
      )
    )

    projects.each { projectName ->
      validateMarkDownProject( projectName )
    }
  }

  /// Check for bad \\page directives in markdown files.
  /// @todo also check for the \@page variant.
  void validateMarkDownProject( final String projectName ) {
    final Project project = new Project( projectName, buildConfig )

    logger.info(
      resource.message(
        'webDoxy.validateMarkDownProject',
        [project.name] as Object[]
      )
    )

    ant.with {
      FileScanner scanner = fileScanner {
        fileset( dir: project.rootFolder ) {
          include( name: "**/*.md" )
              /// @note ^^ double quotes, not single, as otherwise
              /// Doxygen thinks the regex starts a comment block.
              /// \todo Fix this, maybe with an include file?
          exclude( name: 'doxygen.md' )
        }
      }

      int badPages = 0
      for (file in scanner) {
        logger.debug "Checking file: $file"
        file.eachLine { line, num ->
          if ( line ==~ /\\page(\s+\w+){0,1}/ ) {
            ++badPages
            logger.error "Bad page directive in $file at line $num"
          }
        }
      }

      if ( badPages ) {
        fail(
          resource.message(
            'webDoxy.badPageDirective',
            [badPages] as Object[]
          )
        )
      }
    }
  }
}

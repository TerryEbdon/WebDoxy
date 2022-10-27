package net.ebdon.webdoxy;

import java.time.temporal.IsoFields;
import groovy.ant.AntBuilder          // AntBuilder has moved.
import groovy.cli.commons.CliBuilder  // CliBuilder has moved.
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

class WebDoxy {
  static final defaultConfigs = [ 'live', 'staged' ];
  static Resource resource = new Resource();

  // static final String msgDebug = 'debug';
  // static final String msgInfo  = 'info';
  // static final String msgWarn  = 'warn';
  // static final String msgError = 'error';

  def projects = []; ///< List of projects that the commansd(s) will apply to
  Boolean doxygenInitialised = false;
  def buildConfig;
  static AntBuilder ant = new AntBuilder();
  final def cliOptions;

  public static main( args ) {
    ant.echo level: 'info', "args: $args"

    Resource resource = new Resource()
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
    ant.echo level: 'info', "CliBuilder.arguments: ${options.arguments()}"
    if (options) {
      ant.echo level: 'info', "Working..."
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
      ant.echo "WebDoxy run completed in ${(after-before)/1000} seconds"
    }
  }

  WebDoxy( options ) {
    checkInstall()
    final String configFileName = 'config.groovy'

    File configFile = new File( configFileName )

    if ( configFile.exists() ) {
      ant.echo level: 'info', "Using config file: ${configFile.absolutePath}"
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

  void stubs() {

    ant.echo level: 'info', "Adding stubs to projects: ${projects.join(', ')}"
    ant.echo level: 'info', "Stubs are: ${cliOptions.arguments()}"

    projects.each { projectName ->
      Project project = new Project( projectName, buildConfig )
      cliOptions.arguments().each { pageName ->
        project.createStub pageName
      }
    }
  }

  void addDiaryPage() {
    projects.each { projectName ->
      ant.echo level: 'info', "Adding journal page to: $projectName"

      def pageDate = targetDate

      int numPagesWanted = cliOptions.number ?: 1
      ant.echo level: 'info', "Generating $numPagesWanted journal pages."
      ant.echo level: Resource.msgInfo, "Max pages allowed: $maxPages"

      if ( numPagesWanted > 0 && numPagesWanted <= maxPages ) {
        new JournalProject( projectName, buildConfig ).with {
          numPagesWanted.times {
            ant.echo "Generating page for ${pageDate}"
            createPage( pageDate++ )
          }
        }
      } else {
        ant.fail(
          new Resource().message(
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
      ant.echo level: 'warn', 'Date not specified, defaulting to today.'
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
    projects.each { projectName ->
      ant.echo level: 'info', "Adding weekly page to: $projectName"
      def pageDate = targetDate

      ant.echo level: 'info', 'Creating a weekly page'
      final zonedDate = pageDate.toZonedDateTime()
      final pageYear = zonedDate.get( IsoFields.WEEK_BASED_YEAR )
      final pageWeek = zonedDate.get( IsoFields.WEEK_OF_WEEK_BASED_YEAR )
      ant.echo level: 'info',
        "Creating a weekly page for year $pageYear, week $pageWeek"

      ant.echo level: 'info', "Max pages allowed: $maxPages"

      int numPagesWanted = cliOptions.number ?: 1
      ant.echo level: 'info',
        "Generating $numPagesWanted journal pages with increment of $dateIncrement."

      if ( numPagesWanted > 0 && numPagesWanted <= maxPages ) {
        new WeeklyProject( projectName, buildConfig ).with {
          numPagesWanted.times {
            ant.echo "Generating page for ${pageDate}"
            createPage( pageDate )
            pageDate += dateIncrement
          }
        }
      } else {
        ant.fail(
          new Resource().message(
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
      ant.echo level: 'info', "Creating project $name"
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
      ant.with {
        echo level: 'debug', "buildConfig.doxygen.path $buildConfig.doxygen.path"
        echo level: 'info', "buildConfig.doxygen.ant.classPath $buildConfig.doxygen.ant.classPath"
        echo level: 'debug', "buildConfig.doxygen.ant.className $buildConfig.doxygen.ant.className"

        taskdef(
          name: "doxygen",
          classname: buildConfig.doxygen.ant.className,
          classpath:  buildConfig.doxygen.ant.classPath )
      }
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
    ant.echo level: 'info', "Building $projects"
    projects.each { projectName ->
      buildProject projectName
    }
    ant.echo level: 'info', "Sucess: Built project $projects"
  }

  void buildProject( projectName ) {
    Project project = new Project( projectName, buildConfig )
    def before = System.currentTimeMillis()
    ant.echo level: 'info', "Building project: $project"
    project.cleanOutputFolders()

    ant.doxygen(
        verbose: buildConfig.doxygen.verbose,
        configFilename: project.configFileName,
        doxygenPath: buildConfig.doxygen.path ) {
      //property( name: 'PROJECT_NAME', value: "$config Portfolio" )
      //property( name: 'ENABLED_SECTIONS', value: config )
    }
    def after = System.currentTimeMillis()
    ant.echo "Project built in ${(after-before)/1000} seconds"
  }

  /// Check for bad \\page directives in markdown files.
  /// @todo also check for the \@page variant.
  void validateMarkDown() {
    ant.with {
      echo level: Resource.msgInfo, resource.message( 'webDoxy.validateMarkDown' )
      def scanner = fileScanner {
        fileset( dir: '.' ) {
          include( name: "**/*.md" )
              /// @note ^^ double quotes, not single, as otherwise
              /// Doxygen thinks the regex starts a comment block.
              /// \todo Fix this, maybe with an include file?
          exclude( name: 'doxygen.md' )
        }
      }

      int badPages = 0
      for (file in scanner) {
        ant.echo level: Resource.msgDebug, "Checking file: $file"
        file.eachLine { line, num ->
          if ( line ==~ /\\page(\s+\w+){0,1}/ ) {
            ++badPages
            echo level: 'error', message: "Bad page directive in $file at line $num"
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

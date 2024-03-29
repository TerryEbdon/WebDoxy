package net.ebdon.webdoxy;

import java.text.SimpleDateFormat;
import groovy.ant.AntBuilder;
import java.time.ZonedDateTime;
import groovy.transform.TypeChecked;

/**
 * @file
 * @author  Terry Ebdon
 * @date    JUN-2017
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
 @brief   Class that generates a Doxygen project.
 @author  Terry Ebdon

 @todo Move all string constants into config.groovy
 @todo Move all message strings into Language.properties
 */

// @groovy.transform.ToString( ignoreNulls = true, includes=['name', 'zonedDate'] )
@groovy.transform.ToString( includes=['name', 'zonedDate'] )
@groovy.util.logging.Log4j2('logger')
class Project {
  final String name;
  final def buildConfig;  //!< @see config.groovy
  final Resource resource = new Resource();
  final AntBuilder ant;

  private final Date thePageDate = new Date()

  Project( String pn, bc ) {
    assert pn.length()
    assert bc
    name = pn
    buildConfig = bc
    ant = new AntBuilder()
  }

  void create() {
    logger.info "Creating project $name in $rootFolder"
    createFolders()
    createConfigFile()
    createMainPage()
  }

  void createFolders() {
    logger.debug "Creating source folder: $sourceFolder"
    ant.mkdir dir: sourceFolder
    ant.mkdir dir: imageFolder
    ant.mkdir dir: diaFolder

    logger.info "creating example folders: $exampleFolders"
    exampleFolders.each { ef ->
      logger.debug "Creating example folder: $ef"
      ant.mkdir dir: "$rootFolder/$ef"
    }
  }

  private void createConfigFile() {
    new ProjectConfigFile( this ).create()
  }

  def getAuthor() {
    buildConfig.project.author
  }

  String getMarkdownFileType() {
    buildConfig.markdown.fileType
  }

  void setConfig( configFile, key, value ) {
    configFile << "${key.padRight( 25 )} = ${value}\n"
  }

  String getDisableIndex() {
    'YES'
  }

  String getGenerateTreeView() {
    'YES'
  }

  String getHtmlRequired() {
    'YES'
  }

  String getLatexRequired() {
    'YES'
  }

  String getBrief() {
    "Add brief description of the $name project, here."
  }

  def makeDisplayable( rawName) {
    rawName.replace('@','\\@')
  }

  def getDisplayName() {
    makeDisplayable name
  }

  void createMainPage() {
    logger.info "Creating main page markdown for project $name"
    File mainPageFile = new File( mainMarkDownFileName )
    if ( !mainPageFile.exists() ) {
      mainPageFile << "# $displayName {#mainpage}\n\\todo Create content for this web site.\n"
    } else {
      logger.warn "Main page markdown already exists for project $name"
    }
  }

  def getPageDate() {
    final SimpleDateFormat pageDateFormat =
      new SimpleDateFormat( buildConfig.project.page.dateFormat )
    def pageDate = pageDateFormat.format( thePageDate )
  }

  @TypeChecked
  ZonedDateTime getZonedDate() {
    thePageDate.toZonedDateTime()
  }

  def getStubListPageName() {
    "${buildConfig.project.page.stub.name}"
  }

  void createStub( final pageName ) {
    final Object[] msgArgs = [pageName, name] as Object[]

    File pageFile = new File( markDownFileName( pageName ) )
    if ( !pageFile.exists() ) {
      logger.warn resource.message( 'project.stub.creatingPage', msgArgs )
      pageFile << "\\page $pageName $pageName\n"
      pageFile << "[TOC]\n"
      pageFile << "\\date ${pageDate}\n"
      pageFile << "\\author ${author.name}\n"

      pageFile << '\n\\todo ' << resource.message(
        'project.stub.creatingToDo',
        [stubListPageName, pageName] as Object[]
      ) << '\n\n'
      pageFile << buildConfig.project.page.stub.footer << '\n'
      addToStubList pageName
    } else {
      logger.warn resource.message( 'project.stub.pageAlreadyExists', msgArgs )
    }
  }

  void addToStubList( final pageName ) {
    File stubListFile = new File( markDownFileName( stubListPageName ) )

    if ( !stubListFile.exists() ) {
      stubListFile << "\\page ${stubListPageName} ${stubListPageName}\n\n"
    }

    stubListFile << "\n- \\ref $pageName\n"
  }

  String getMainMarkDownFileName() {
    //  "${sourceFolder}/@${name}_index.md"
    markDownFileName "@${name}_index"
  }

  String markDownFileName( pageName ) {
    "${sourceFolder}/${pageName}.md"
  }

  Boolean exists () {
    new File( configFileName ).exists()
  }

  void cleanOutputFolders() {
    logger.debug "delete folder ${htmlFolder}/${name}"
    //~ ant.delete dir: "${htmlFolder}/${name}"
    //~ ant.mkdir dir: "${htmlFolder}/${name}"
    cleanFolder htmlFolder
    cleanFolder latexFolder
  }

  void cleanFolder( folderName ) {
    logger.info "delete folder ${folderName}"
    ant.delete dir: "${folderName}", verbose: buildConfig.verboseCleanUp
    ant.mkdir dir: "${folderName}"
  }

  def getOutRoot() {
    buildConfig.project.outRoot
  }

  def getExampleFolders() {
    buildConfig.project.parentfolders.examples
  }

  String getDiaFolder() {
    "$rootFolder/$buildConfig.project.parentfolders.dia"
  }

  String getConfigFileName() {
    "${rootFolder}/${name}.${buildConfig.doxygen.configFileType}"
  }

  String getRootFolder() {
    "$buildConfig.project.root/$name"
  }
  String getSourceFolder() {
    "$rootFolder/${buildConfig.project.parentfolders.source}"
  }

  String getHtmlFolder() {
    //~ buildConfig.project.parentfolders.html
    "${htmlRootFolder}/$name"
  }

  String getHtmlRootFolder() {
    //~ buildConfig.project.parentfolders.html
    "${outRoot}/${buildConfig.project.parentfolders.html}"
  }

  String getLatexFolder() {
    //~ buildConfig.project.parentfolders.html
    "${outRoot}/${buildConfig.project.parentfolders.latex}/$name"
  }

  String getImageFolder() {
    "$rootFolder/$buildConfig.project.parentfolders.image"
  }

}

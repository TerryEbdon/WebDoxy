package net.ebdon.webdoxy;

import java.text.SimpleDateFormat;
import java.time.temporal.IsoFields;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
@groovy.util.logging.Log4j2('logger')
class JournalProject extends Project {

  Date pageDate = new Date();

  JournalProject( projectName, buildConfig ) {
    super( projectName, buildConfig )
  }

  def getPageDate() {
    this.pageDate
  }

  String monthTitle( final Date monday, final Date sunday ) {

    final SimpleDateFormat monthFormat = new SimpleDateFormat( 'MMM' )

    final startMonth = monthFormat.format( monday )
    final endMonth   = monthFormat.format( sunday )

    // println "startMonth: $startMonth"
    // println "endMonth:   $endMonth"

    startMonth == endMonth ? startMonth : "$startMonth / $endMonth"
  }

  @TypeChecked
  final String monthTitle( final ZonedDateTime monday, final ZonedDateTime sunday ) {

    logger.debug "monthTitle(): monday=${monday.format( DateTimeFormatter.RFC_1123_DATE_TIME )}"
    logger.debug "monthTitle(): sunday=${sunday.format( DateTimeFormatter.RFC_1123_DATE_TIME )}"
    final String startMonth = monday.format( 'MMM' )
    final String endMonth   = sunday.format( 'MMM' )

    logger.debug "startMonth: $startMonth"
    logger.debug "endMonth:   $endMonth"

    startMonth == endMonth ? startMonth : "$startMonth / $endMonth"
  }

  String getFullFolderPath() {
    "${monthFolderPath}${dayFolder}"
  }

  String getFullPath() {
    "${fullFolderPath}${pageFileName}"
  }

  def createPage( Date date ) {
    pageDate = date

    logger.debug "fullFolderPath: $fullFolderPath"
    logger.debug "Full path: $fullPath"
    ant.mkdir dir: fullFolderPath

    logger.info "Creating page file: $fullPath"

    final File pageFile = new File( fullPath )

    if ( !pageFile.exists() ) {
      final JournalPage page = new JournalPage( this, pageFile )
      page.create()
      addPageToMonth page
    } else {
      logger.warn  message( 'JournalProject.nothingToDo' )
      logger.debug " --> ${pageFile.absolutePath}"
    }
  }

  def addPageToMonth( JournalPage dayPage, boolean subPage = true ) {
    assert dayPage
    if ( buildConfig.project.journal.pages.monthly.required ) {
      final def monthFmtStr = buildConfig.project.journal.pages.monthly.format
      logger.debug "Month format string: ${monthFmtStr}"
      final def SimpleDateFormat monthFormatter = new SimpleDateFormat( monthFmtStr ?: 'MMMM' )
      final def monthFileName = monthFormatter.format( pageDate ) + markdownFileType
      File monthFile = new File( "${monthFolderPath}/${monthFileName}" )

      MonthPage monthPage = new MonthPage( this, monthFile )
      monthPage.create()
      if ( buildConfig.project.journal.pages.monthly.addLinkToNewDayPage ) {
        logger.info "Adding: $dayPage"
        logger.info "    to: $monthPage"
        monthPage.addPageLink dayPage, subPage
      }
    }
  }

  def getPageQuarter() {
    final def quartersPerYear = 4
    final def minWeeksPerQuarter = 13

    new BigDecimal( pageWeek )
      .divide( minWeeksPerQuarter, 0, BigDecimal.ROUND_UP )
      .min( quartersPerYear )
  }

  def getMonthFolderPath() {
    "${yearFolderPath}${monthFolder}"
  }

  def getYearFolderPath() {
    "${sourceFolder}/${yearFolder}"
  }

  def getPageFileName() {
    final SimpleDateFormat fileNameFormat = dateFormatter( 'fileName' )
    final def fileName   = fileNameFormat.format( pageDate ) + markdownFileType
  }

  def getYearFolder() {
    dateFolder 'annual'
  }
  def getMonthFolder() {
    dateFolder 'month'
  }
  def getDayFolder() {
    dateFolder 'day', 'daily'
  }

  /**
    * @param folderReqdProp  annual, month or day -- name of string property.
    * @param formatProp      folderProp + 'ly' name of boolean property.
    * @return        The folder name or an empty string, if not required.
    */
  def dateFolder( String formatProp, String  folderReqdProp = formatProp + 'ly' ) {
    logger.debug "folderProp: $folderReqdProp, formatProp: $formatProp"
    folderRequired( folderReqdProp ) ? dateFormatter( formatProp ).format( pageDate) +'/' : ''
  }

  def folderRequired( reqdPropertyName ) {
    final required = buildConfig.project.journal.folders[ reqdPropertyName ]
    logger.debug "$reqdPropertyName Required: $required"
    required
  }

  SimpleDateFormat dateFormatter( final String name ) {
    logger.debug "Getting journal date format name: $name"
    final String format = buildConfig.project.journal.format[name]
    logger.debug "journal date format $name = $format"
    new SimpleDateFormat( format )
  }
}

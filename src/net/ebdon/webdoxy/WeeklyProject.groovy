package net.ebdon.webdoxy;

import java.text.SimpleDateFormat;
import java.time.temporal.IsoFields;
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
@groovy.util.logging.Log4j2('logger')
class WeeklyProject extends JournalProject {

  WeeklyProject( projectName, buildConfig ) {
    super( projectName, buildConfig )
  }

  int getPageWeek() {
    zonedDate.get( IsoFields.WEEK_OF_WEEK_BASED_YEAR )
  }

  // @TypeChecked
  String getPageTitle() {
    final pageYear = zonedDate.get( IsoFields.WEEK_BASED_YEAR )
    final Date sunday = pageDate + 6

    logger.info "Creating weekly page title for year $pageYear, week $pageWeek"

    final String pageAnchor = "{#y${pageYear}_w${pageWeek}}"
    "# ${monthTitle( pageDate, sunday )} -- $pageYear week $pageWeek $pageAnchor"
  }

  @TypeChecked
  Date startOfWeek( final Date date ) {
    startOfWeek( date.toZonedDateTime() )
  }

  @TypeChecked
  Date startOfWeek( final ZonedDateTime dayInTargetWeek ) {
    final int dayNumber = dayInTargetWeek.dayOfWeek.value // 0=Monday, 7=Sunday
    logger.trace "Day number: $dayNumber"

    // Go backwards from this day number to find the previous Monday
    ZonedDateTime monday = dayInTargetWeek.minusDays( dayNumber - 1 )
    new Date( monday.year - 1900, monday.monthValue - 1, monday.dayOfMonth )
  }

  def getPageYear() {
    new SimpleDateFormat( 'yyyy' ).format( pageDate )
  }

  String getQuarterFolder() {
    "${pageYear}-q${pageQuarter}"
  }

  @Override
  String getFullFolderPath() {
    "${yearFolderPath}${quarterFolder}/"
  }

  @Override
  def getPageFileName() {
    final SimpleDateFormat fileNameFormat = dateFormatter( 'weekFileName' )
    final def fileName   = fileNameFormat.format( pageDate ) + markdownFileType
  }

  @Override
  def createPage( final Date date ) {
    pageDate = startOfWeek( date )

    logger.info "fullFolderPath: $fullFolderPath"
    logger.info "Full path: $fullPath"

    ant.mkdir dir: fullFolderPath
    logger.info "Creating page file: $fullPath"

    File pageFile = new File( fullPath )

    if ( !pageFile.exists() ) {
      final WeekPage page = new WeekPage( this, pageFile )

      page.create()
      addPageToQuarter  page
      addPageToMonth    page, false
    } else {
      logger.warn   resource.message( 'JournalProject.nothingToDo' )
      logger.debug  " --> ${pageFile.absolutePath}"
    }
  }

  @Override
  def addPageToMonth( JournalPage weekPage, boolean subPage = true ) {
    assert weekPage
    if ( buildConfig.project.journal.pages.monthly.required ) {
      logger.info '**** Monthly pages *ARE* required'

      final String monthFmtStr = buildConfig.project.journal.pages.monthly.format
      logger.info "Month format string: ${monthFmtStr}"
      final SimpleDateFormat monthFormatter = new SimpleDateFormat( monthFmtStr ?: 'MMMM' )
      final String monthFileName = monthFormatter.format( pageDate ) + markdownFileType
      File monthFile = new File( "${monthFolderPath}/${monthFileName}" )

      MonthSummaryPage monthSummaryPage = new MonthSummaryPage( this, monthFile )
      monthSummaryPage.create()
    }
  }

  @Override
  def getMonthFolderPath() {
    yearFolderPath
  }

  void addPageToQuarter( JournalPage weekPage ) {
    assert weekPage
    if ( buildConfig.project.journal.pages.quarterly.required ) {
      logger.info 'Quarterly pages ARE required'
      final String fileName = "${pageYear}-q${pageQuarter}${markdownFileType}"

      File file = new File( "${yearFolderPath}/${fileName}" )

      QuarterPage quarterPage = new QuarterPage( this, file )
      quarterPage.create()
      if ( buildConfig.project.journal.pages.quarterly.addLinkToNewWeekPage ) {
        logger.info "Adding: $weekPage"
        logger.info "    to: $quarterPage"
        quarterPage.addPageLink weekPage, true
      }
    } else {
      logger.info 'Quarterly pages are NOT required'
    }
  }
}

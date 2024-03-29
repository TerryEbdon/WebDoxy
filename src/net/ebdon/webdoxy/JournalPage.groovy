package net.ebdon.webdoxy;

import java.text.SimpleDateFormat;
import java.time.temporal.IsoFields;
import java.time.format.DateTimeFormatter;

/**
 * @file
 * @author  Terry Ebdon
 * @date    23-JUN-2017
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
class JournalPage {

  final JournalProject project;
  private final buildConfig;
  private final File pageFile;
  private final Date pageDate = new Date();
  private String anchorDate_; // final?
  private String title_; // final?

  String toString() {
    "${title_}, ${h1Anchor}"
  }

  /**
    * @author Terry Ebdon
    * @date 23-JUN-2017
    * @todo The file shouldn't be passed in.
    */
  JournalPage( JournalProject jp, File file ) {
    project = jp
    pageDate = project.pageDate
    pageFile = file
    buildConfig = project.buildConfig
    init()
    logger.debug 'JournalPage instantiated.'
  }

  void init() {
    final SimpleDateFormat dayAnchorFormatter = project.dateFormatter( 'anchorDay' )
    final SimpleDateFormat shortFormat        = project.dateFormatter( 'shorter' )

    logger.debug "${dayAnchorFormatter.format( pageDate )}"
    anchorDate = dayAnchorFormatter.format( pageDate )
    title      = shortFormat.format( pageDate )
  }

  def getPageDate() {
    this.pageDate
  }

  void setAnchorDate( final String newDate ) {
    anchorDate_ = newDate
    logger.debug "setAnchorDate called with $newDate"
    logger.debug "anchorDate_ is now: ${anchorDate_}"
  }

  String getAnchorDate() {
    logger.trace "getAnchorDate returning ${anchorDate_}"
    anchorDate_
  }

  void setTitle( final String newTitle ) {
    title_ = newTitle
  }

  String getTitle() {
    title_
  }

  def create() {
    assert pageFile
    logger.debug "JournalPage.create() called."
    logger.debug "Page file: $pageFile"

    createSkeleton()
  }

  def createSkeleton() {
    createSkeletonHeader()
    createSkeletonBody()
    createSkeletonFooter()
  }

  def createSkeletonHeader() {
    final DateTimeFormatter commentFormatter =
      DateTimeFormatter.ofPattern( '''EEE' 'yyyy'-'LL'-'dd' is in ISO 8601 week 'w' of year 'YYYY''')
    final String pageDateComment =
      pageDate.toZonedDateTime().format( commentFormatter )

    logger.info pageDateComment
    append "@page ${pageAnchor} $title\n"
    append "<!-- $pageDateComment -->\n"
    append "@anchor ${h1Anchor}"
    append "# $firstHeaderTitle\n"
  }

  def createSkeletonBody() {
    append '\n@todo Add content to journal page.\n'
    append '\n## Interesting web pages'
    append '\n### Tweets\n'
    append tweetTemplate
    append tweetTemplate
    append tweetTemplate
  }

  def createSkeletonFooter() {
    htmlOnly {
      "<a class='btn' href='#${h1Anchor}'>Top of page</a>"
    }
  }

  String getTweetTemplate() {
    /// @todo add filter to
    /// convert \@tweep terry_ebdon into \[\@jack\](https://twitter.com/\@jack)
    ///

    buildConfig.project.journal.pages.tweetTemplate
  }


  def getFirstHeaderTitle() {
    final SimpleDateFormat longFormat = firstHeaderTitleFormat
    addSuffix( longFormat.format( project.pageDate ) )
  }

  def addSuffix( final dateString ) {
    final def suffix = dayNumberSuffix( project.pageDate )
    dateString.replace( '??', "<sup>$suffix</sup>" )
  }

  def getFirstHeaderTitleFormat() {
    project.dateFormatter( 'longer' )
  }

  void append( final String content ) {
    assert pageFile
    if ( this.class.name.contains( 'Month' ) ) {
      logger.debug "Appending to: $pageFile"
      logger.debug "        path: ${pageFile.path}"
      logger.debug "      exists: ${exists()}"
    }
    pageFile << content << '\n'
  }

  def exists() {
    assert pageFile
    pageFile.exists()
  }

  def getPageAnchor() {
    "wip${anchorDate}"
  }

  def getH1Anchor() {
    "h${anchorDate}"
  }

  /**
    * dayNumberSuffix get the English suffix for a day No., i.e. st, nd, rd or th.
    * @param    d The date to get the day suffix for.
    * @return   The English two letter day No. suffix.
    * @author   Terry Ebdon
    * @date     23-JUN-2017
    */
  def dayNumberSuffix( final Date d ) {
    switch ( d.date ) {
      case [1,21,31]: 'st'; break
      case [2,22]:    'nd'; break
      case [3,23]:    'rd'; break
      case 4..20:     'th'; break
      case 24..29:    'th'; break
      case 30:        'th'; break
      default:
        project.ant.fail (
          new Resource().message(
            'journalPage.badDayNum',
            [d.date] as Object[]
          )
        )
  }
}

  def htmlOnly( Closure closure ) {
    append '@htmlonly'
    append closure.call()
    append '@endhtmlonly'
  }

  def addSubPage( final JournalPage dayPage ) {
    assert dayPage
    logger.info "Adding page ${dayPage.title} to page $title"

    addPageLink dayPage, true
  }

  def addPageLink( final JournalPage dayPage, boolean subPage ) {
    final prefix = project.buildConfig.project.journal.pages.monthly.linkPrefix
    final suffix = project.buildConfig.project.journal.pages.monthly.linkSuffix
    final String linkMethod = subPage ? 'subpage' : 'ref'

    append "${prefix}@${linkMethod} ${dayPage.pageAnchor}${suffix}"
  }

  java.time.ZonedDateTime getZonedDate() {
    project.pageDate.toZonedDateTime()
  }

  int getPageWeek() {
    zonedDate.get( IsoFields.WEEK_OF_WEEK_BASED_YEAR )
  }

  def getPageQuarter() {
    project.pageQuarter
  }

  def getPageYear() {
    zonedDate.get( IsoFields.WEEK_BASED_YEAR )
  }

  int getPageMonth() {
    project.pageDate.month + 1
  }

}

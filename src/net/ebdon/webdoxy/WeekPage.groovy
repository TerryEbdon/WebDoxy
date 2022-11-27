package net.ebdon.webdoxy;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import groovy.transform.TypeChecked;

/**
 * @file
 * @author  Terry Ebdon
 * @date    01-JUN-2020
 * @copyright
 *
 * Copyright 2020 Terry Ebdon
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
class WeekPage extends JournalPage {

  final String anchorDateFormat = ''''day'_yyyy_MM_dd'''
  String pageAnchor

  @TypeChecked
  WeekPage( WeeklyProject jp, File file ) {
    super( jp, file )
    logger.debug 'WeekPage instantiated.'
  }

  @TypeChecked
  String getPageTitle() {
    WeeklyProject wp = (WeeklyProject) project
    logger.debug "Getting weekly page title for $project.zonedDate.dayOfWeek $project.zonedDate"
    final ZonedDateTime monday = wp.startOfWeek( project.zonedDate ).toZonedDateTime()
    final ZonedDateTime sunday = monday.plusDays( 6 )
    logger.debug "Monday of project week is     $monday.dayOfWeek $monday, "
    logger.debug "Sunday of project week is     $sunday.dayOfWeek $sunday"

    logger.info "Creating weekly page title for year $pageYear, week $pageWeek"

    pageAnchor = "y${pageYear}_w${pageWeek}"
    final String monthTitle = project.monthTitle( monday, sunday )

    "# $monthTitle -- $pageYear week $pageWeek {#$pageAnchor}"
  }

  @Override
  def createSkeletonHeader() {
    append "${pageTitle}\n\n[TOC]\n"
  }

  @Override
  def createSkeletonBody() {
    ZonedDateTime dayInWeek = zonedDate
    7.times {
      append dayHeader( dayInWeek )
      dayInWeek.plusDays( 1 )
    }
  }

  private String dayHeader( final ZonedDateTime zonedDayInWeek) {
    final SimpleDateFormat pageDateFormat1 =
      new SimpleDateFormat( '## dd EEEE' )

    final SimpleDateFormat pageDateFormat2 =
      new SimpleDateFormat( "{#${anchorDateFormat}}\n" )

    final Date dayInWeek = Date.from( zonedDayInWeek.toInstant() )

    pageDateFormat1.format( dayInWeek ).padRight( 16 ) +
    pageDateFormat2.format( dayInWeek )
  }

  @Override
  def getH1Anchor() {
    new SimpleDateFormat( anchorDateFormat ).format( pageDate )
  }
}
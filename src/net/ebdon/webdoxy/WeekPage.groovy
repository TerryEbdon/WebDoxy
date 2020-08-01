package net.ebdon.webdoxy;

import java.text.SimpleDateFormat;

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

class WeekPage extends JournalPage {

  final String anchorDateFormat = ''''day'_yyyy_MM_dd'''
  String pageAnchor

  WeekPage( JournalProject jp, File file ) {
    super( jp, file )
    project.ant.echo level:'debug', "WeekPage instantiated."
  }

  String getPageTitle() {
    final Date sunday = project.pageDate + 6

    project.ant.echo level: 'info',
      "Creating weekly page title for year $pageYear, week $pageWeek"

    pageAnchor = "y${pageYear}_w${pageWeek}"
    "# ${project.monthTitle( project.pageDate, sunday )} -- $pageYear week $pageWeek {#$pageAnchor}"
  }

  @Override
  def createSkeletonHeader() {
    append "${pageTitle}\n\n[TOC]\n"
  }

  @Override
  def createSkeletonBody() {

    Date dayInWeek = pageDate
    7.times {
      append dayHeader( dayInWeek++ )
    }
  }

  String dayHeader( final Date dayInWeek) {
    final SimpleDateFormat pageDateFormat1 =
      new SimpleDateFormat( "## dd EEEE" )

    final SimpleDateFormat pageDateFormat2 =
      new SimpleDateFormat( "{#${anchorDateFormat}}\n" )

    pageDateFormat1.format( dayInWeek ).padRight( 16 ) +
    pageDateFormat2.format( dayInWeek )
  }

  @Override
  def getH1Anchor() {
    new SimpleDateFormat( anchorDateFormat ).format( pageDate )
  }
}
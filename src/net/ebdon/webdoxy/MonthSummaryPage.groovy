package net.ebdon.webdoxy;
import java.text.SimpleDateFormat;

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

class MonthSummaryPage extends JournalPage {

  MonthSummaryPage( JournalProject jp, File monthFile ) {
    super( jp, monthFile )
    project.ant.echo level: 'debug', 'MonthSummaryPage instantiated'
  }

  def init() {
    final SimpleDateFormat anchorFormat = project.dateFormatter( 'anchorMonth' )
    final SimpleDateFormat titleformatter = project.dateFormatter( 'month' )

    anchorDate = anchorFormat.format( pageDate )
    title      = titleformatter.format( pageDate )
  }

  def create() {
    project.ant.echo level: 'debug', 'MonthSummaryPage.create() called'

    if ( exists() ) {
      project.ant.echo level: 'info', project.message( 'MonthSummaryPage.alreadyExists' )
    } else {
      super.create()
    }
  }

  @Override
  def createSkeletonBody() {
    append '|       |       |'
    append '|  ---: |  :--- |'

    def pageDateFormat =
      new SimpleDateFormat( '''|[EEE dd](\\'ref' 'day'_yyyy_MM_dd) | |''' )
    Date dayInMonth = new Date( pageDate.year, pageDate.month, 1 )

    1.upto( lastDateOfMonth ) { 
      append pageDateFormat.format( dayInMonth++ )
    }
  }

  int getLastDateOfMonth() {
    Calendar cal = Calendar.getInstance()
    cal.setTime( pageDate )
    cal.set(
      Calendar.DAY_OF_MONTH,
      cal.getActualMaximum( Calendar.DAY_OF_MONTH )
    )
    return cal.getTime().date
  }

  def createSkeletonFooter() {
    ;
  }

  def getH1Anchor() {
    "h${anchorDate}"
  }

  def getFirstHeaderTitleFormat() {
      project.dateFormatter( 'longerMonth' )
  }

  def addSuffix( final dateString ) {
    dateString
  }
}
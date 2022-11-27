package net.ebdon.webdoxy;

import java.text.SimpleDateFormat;
import groovy.transform.TypeChecked;

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
class MonthPage extends JournalPage {

  @TypeChecked
  MonthPage( JournalProject jp, File monthFile ) {
    super( jp, monthFile )
    logger.debug 'MonthPage instantiated'
  }

  @Override
  void init() {
    final SimpleDateFormat anchorFormat   = project.dateFormatter( 'anchorMonth' ) ///@todo fix
    final SimpleDateFormat titleformatter = project.dateFormatter( 'month' ) ///@todo fix

    anchorDate = anchorFormat.format( pageDate )
    title      = titleformatter.format( pageDate )
  }

  @TypeChecked
  def create() {
    logger.debug 'MonthPage.create() called'

    if ( exists() ) {
      logger.info project.resource.message( 'MonthPage.alreadyExists' )
    } else {
      super.create()
    }
  }

  def createSkeletonBody() {
    ;
  }

  def createSkeletonFooter() {
    ;
  }

  def getHtmlFileNames() {
    buildConfig.project.journal.pages.monthly.htmlIncludes
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

package net.ebdon.webdoxy;

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

class QuarterPage extends JournalPage {

  QuarterPage( JournalProject jp, File quarterFile ) {
    super( jp, quarterFile )
    project.ant.echo level: 'debug', 'QuarterPage instantiated'
  }

  String getPageTitle() {
    project.ant.echo level: 'info',
      "Creating quarterly page title for year $pageYear, week $pageWeek"

    "# $pageYear Q${pageQuarter} {#$pageAnchor}"
  }

  @Override
  def getPageAnchor() {
    "y${pageYear}_q${pageQuarter}"
  }

  @Override
  def getH1Anchor() {
    pageAnchor
  }

  @Override
  def create() {
    project.ant.echo level: 'info', 'QuarterPge.create() called'

    if ( exists() ) {
      project.ant.echo level: 'info', project.message( 'QuarterPage.alreadyExists' )
    } else {
      super.create()
    }
  }

  @Override
  def createSkeletonHeader() {
    append "${pageTitle}\n" // # 2020 Q1 25 {#y2020_q2}
  }


  @Override
  def createSkeletonBody() {
    ;
  }

  @Override
  def createSkeletonFooter() {
    ;
  }
}
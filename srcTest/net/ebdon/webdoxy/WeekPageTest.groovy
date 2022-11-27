package net.ebdon.webdoxy;

import groovy.test.GroovyTestCase;

/**
 * @file
 * @author      Terry Ebdon
 * @date        November 2022
 * @copyright
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
class WeekPageTest extends GroovyTestCase {

  final private static Map config = [
    project: [
      journal: [
        format: [
          'shorter':   'dd-MMM-yyyy', // @todo Eliminate test dependency on WeeklyProject
          'anchorDay': 'yyyyMMdd'
        ]
      ]
    ]
  ];

  private WeeklyProject weeklyProject;
  private WeekPage weekPage;

  void testGetPageTitle() {
    weeklyProject = new WeeklyProject( 'test project', config )
      //< @todo Eliminate test dependency on WeeklyProject
    weekPage = new WeekPage( weeklyProject, new File('.') )
    final String pageTitle = weekPage.pageTitle
    logger.debug "Got page title as >${pageTitle}<"
    assert pageTitle.length() > 2
  }

  void testCreateSkeletonBody() {
    weeklyProject = new WeeklyProject( 'test project', config )
    File markdownOutput = new File('logs/WeekPageTest.txt')
    markdownOutput.deleteOnExit()
    if ( markdownOutput.exists() ) {
      logger.debug 'Temp markdown file exists pre-test, deleting it.'
      markdownOutput.delete()
    }
    weekPage = new WeekPage( weeklyProject, markdownOutput )

    weekPage.createSkeletonBody()
    assert markdownOutput.exists()
    logger.debug "WeekPage file size: ${markdownOutput.length()}"
    assert 245 == markdownOutput.length()
  }
}

package net.ebdon.webdoxy;

import groovy.test.GroovyTestCase;
import groovy.transform.TypeChecked;

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
class MonthSummaryPageTest extends GroovyTestCase {

  final private static Map config = [
    project: [
      journal: [
        format: [
          month:          'YYYY-MM',
          anchorMonth:    'yyyyMM',
        ]
      ]
    ]
  ];

  @TypeChecked
   void testCreateSkeletonBody() {
    if ( GroovyTestCase.notYetImplemented( this ) ) return
    WeeklyProject weeklyProject = new WeeklyProject( 'test project', config )
    File markdownOutput = new File('logs/MonthSummaryPageTest.txt')
    markdownOutput.deleteOnExit()
    if ( markdownOutput.exists() ) {
      logger.debug 'Temp markdown file exists pre-test, deleting it.'
      markdownOutput.delete()
    }
    final MonthSummaryPage monthSummaryPage =
      new MonthSummaryPage( weeklyProject, markdownOutput )

    monthSummaryPage.createSkeletonBody()
    assert markdownOutput.exists()
    logger.debug "monthSummaryPage file size: ${markdownOutput.length()}"
    assert 245 == markdownOutput.length()
  }
}

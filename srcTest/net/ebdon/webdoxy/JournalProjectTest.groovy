package net.ebdon.webdoxy;

import groovy.test.GroovyTestCase;
import java.text.SimpleDateFormat;
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
class JournalProjectTest extends GroovyTestCase {

  private final static Map config = [
    project: [
      journal: [
        format: new Expando()
      ]
    ]
  ];

  // @Override void setUp() {
  JournalProjectTest() {
    final String regression63 = 'Issue #63 regression'
    config.project.journal.format.metaClass.getAt = { String key ->
      switch (key) {
        case 'shorter':    'dd-MMM-yyyy'; break
        case 'anchorDay':  'yyyyMMdd'   ; break
        case 'anchor.day':
          logger.fatal regression63
          throw new Exception( regression63 )
        default:
          throw new Exception( "Unexpected config key: $key" )
      }
    }
  }

  @TypeChecked
  void testDateFormatter() {
    final JournalProject journalProject = new JournalProject( 'test project', config )
    final SimpleDateFormat sdfAd      = journalProject.dateFormatter( 'anchorDay' )
    final SimpleDateFormat sdfShorter = journalProject.dateFormatter( 'shorter' )

    assert sdfAd && sdfShorter
  }
}

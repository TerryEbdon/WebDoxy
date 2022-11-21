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
class WeeklyProjectTest extends GroovyTestCase {

  final private static Map config = [
    project: [
      journal: [
        format: [
          'anchor.day': 'yyyyMMdd'
        ]
      ]
    ]
  ];

  void testStartOfWeek() {
    final Date monday =
      new WeeklyProject( 'Fred', config ).
        startOfWeek( new Date(2022 - 1900,11 - 1,20,22,38) )
    assert monday.day == 1
  }
}


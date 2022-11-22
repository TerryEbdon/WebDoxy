package net.ebdon.webdoxy;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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

  final private static targetYear             = 2022
  final private static targetDayOfMonth       = 20
  final private static expectMondayDayOfMonth = 14
  final private static yearOffset             = 1900
  
  final private static Map config = [
    project: [
      journal: [
        format: [
          'anchorDay': 'yyyyMMdd'
        ]
      ]
    ]
  ];

  void testStartOfWeek() {
    logger.debug 'testStartOfWeek'
    final Date targetDate = new Date(targetYear - yearOffset,11 - 1,targetDayOfMonth,22,38) 
    logger.debug "Target week contains:     $targetDate"
    final Date monday =
      new WeeklyProject( 'Fred', config ).
        startOfWeek( targetDate)

    logger.debug "Start of containing week: $monday"
    assert monday.day == 1
  }

  void testStartOfWeekZoned() {
    logger.debug 'testStartOfWeekZoned'
    final ZoneId zoneId = ZoneId.of('Etc/UTC')
    final ZonedDateTime targetDate = ZonedDateTime.of(
      targetYear,11,targetDayOfMonth,22,38,0,0, zoneId )
    final String targetDayOfWeek = targetDate.dayOfWeek.toString()[0..2]
    logger.debug "Target week contains:     $targetDayOfWeek $targetDate"

    final Date monday =
      new WeeklyProject( 'Fred', config ).
        startOfWeek( targetDate )
    logger.debug "Start of containing week: $monday"
    assert monday.day  == 1
    assert monday.date == expectMondayDayOfMonth
    assert monday.year == targetYear - yearOffset
  }
}

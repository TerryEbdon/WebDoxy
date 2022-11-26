package net.ebdon.webdoxy;

import groovy.test.GroovyTestCase;
import java.time.ZonedDateTime;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
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

  JournalProjectTest() {
    final String regression63 = 'Issue #63 regression, expected "anchorDay", not "anchor.day"'
    config.project.journal.format.metaClass.getAt = { String key ->
      switch (key) {
        case 'annual'    : 'YYYY';        break
        case 'shorter'   : 'dd-MMM-yyyy'; break
        case 'anchorDay' : 'yyyyMMdd'   ; break
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

  @TypeChecked
  void testDateFormatterAnnual() {
    final JournalProject journalProject = new JournalProject( 'test project', config )
    final SimpleDateFormat annualFormatter = journalProject.dateFormatter( 'annual' )

    logger.debug 'Year | formatted | Passed |         java.util.Date       | week-of-week-based-year'
    logger.debug '-----|-----------|--------|------------------------------|------------------------'

    final int javaYearOffset = 1900
    int numFailed = 0
    1998.upto(2001) { final Number targetYear ->
      final int targetJavaYear = targetYear - javaYearOffset
      final Date targetDate = new Date( targetJavaYear,0,1,20,0,0)
      journalProject.pageDate = targetDate
      final String formattedDate = annualFormatter.format( targetDate )
      final String weekNumber = targetDate.
        toZonedDateTime().format( DateTimeFormatter.ofPattern( 'w' ) )

      assert weekNumber in ['1','52','53']
      final String expectedYear = weekNumber == '1' ? "$targetYear" : "${targetYear - 1}"
      final String passed = expectedYear == formattedDate ? 'Pass' : 'FAIL'
      if ( passed != 'Pass' ) {
        ++numFailed
      }
      logger.debug sprintf( '%-5s|    %-4s   |  %4s  | %s | %2s',
        targetYear, formattedDate, passed, targetDate, weekNumber )
    }
    logger.debug "$numFailed errors detected"
    if ( numFailed ) {
      final String message = 'Issue #32 regression'
      logger.fatal message
      throw new Exception( message )
    }
  }
}

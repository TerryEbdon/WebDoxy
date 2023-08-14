package net.ebdon.webdoxy;

import groovy.test.GroovyTestCase;
import groovy.mock.interceptor.MockFor;
import java.time.ZonedDateTime;
import java.time.LocalDate;

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
class WebDoxyTest extends GroovyTestCase {

  final private static Map config = [
    datePattern:'yyyy-MM-dd',
    project: [
      journal: [
        format: [
          'anchorDay': 'yyyyMMdd'
        ]
      ],
    ]
  ];

  private MockFor resourceMock;
  private MockFor configSlurperMock;
  private MockFor journalProjectMock;

  private Expando options;

  @Override
  void setUp() {
    super.setUp()
    options = new Expando(
      project  : false,
      create   : false,
      journal  : false,
      week     : false,
      validate : false,
      generate : false,
      toc      : false,
      backup   : false,
      stub     : false,
      arguments: { ['Fred'] },
    )

    resourceMock      = new MockFor( Resource )
    journalProjectMock = new MockFor( JournalProject )

    resetConfigSlurper()
    resourceMock.demand.message { final String key, final Object[] args ->
      final String returnVal = "Resource.message() called with key $key & args $args"
      logger.info returnVal
      assert key == 'backup.copyDriveOffline'
      returnVal
    }
  }

  void resetConfigSlurper() {
    logger.debug 'Resetting config slurper'
    configSlurperMock = new MockFor( ConfigSlurper )
    configSlurperMock.demand.parse { final URL url ->
      assert url.file ==~ '.*config.groovy$'
      logger.debug 'configSlurperMock.demand.parse called'
      config
    }
  }

  void testGetTargetDateDefault() {
    logger.debug 'Start of testGetTargetDateDefault()'
    configSlurperMock.use {
      WebDoxy build = new WebDoxy( options )
      final Date parsedDate = build.targetDate
      logger.debug "target date returned as: $parsedDate"
      assert parsedDate
    }
    logger.debug 'End of testGetTargetDateDefault()'
  }

  void testGetTargetDateFromOption() {
    logger.debug 'Start of testGetTargetDateFromOption()'
    configSlurperMock.use {
      options.date = '1999-01-01'
      WebDoxy build = new WebDoxy( options )
      final Date parsedDate = build.targetDate
      logger.debug "target date returned as: $parsedDate"
      assert parsedDate
      assert 99 == parsedDate.year
      assert  0 == parsedDate.month
      assert  1 == parsedDate.date
      assert  5 == parsedDate.day
    }
    logger.debug 'End of testGetTargetDateFromOption()'
  }

  void testAddDiaryPageWithDate() {
    logger.debug 'Start of testAddDiaryPageWithDate()'
    if ( GroovyTestCase.notYetImplemented( this ) ) {
      logger.fatal '** TEST NOT YET IMPLEMENTED **'
      return
    }
    assert false
    logger.debug 'End of testAddDiaryPageWithDate()'
  }

  void testAddDiaryPageDateRange() {
    logger.debug 'Start of testAddDiaryPageDateRange()'
    final int numPagesWanted = 7
    final ZonedDateTime today     = ZonedDateTime.now()
    final ZonedDateTime yesterday = today.minusDays( 1 )

    Map<Integer,ZonedDateTime> expectedPageDates = [:]
    1.upto( numPagesWanted ) { dayNumber ->
      expectedPageDates[ dayNumber ] = yesterday.plusDays( dayNumber )
    }
    assert expectedPageDates.size() == numPagesWanted

    options.number = numPagesWanted
    buildPages( today, expectedPageDates )

    logger.debug 'End of testAddDiaryPageDateRange()'
  }

  private void buildPages(
      final ZonedDateTime firstPageDateWanted = ZonedDateTime.now(),
      final Map<Integer,ZonedDateTime> expectedPageDates = [1: firstPageDateWanted] ) {

    Map<Integer,LocalDate> expectedLocalDates = [:]

    expectedPageDates.each { key, zonedDate ->
      expectedLocalDates[ key ] = zonedDate.toLocalDate()
    }

    assert expectedPageDates.size() == expectedLocalDates.size()
    final int expectedPageCount = expectedLocalDates.size()

    int currentPageNum = 0

    logger.info "Expecting $expectedPageCount pages starting with $firstPageDateWanted"

    journalProjectMock.demand.with {
      'with'(1) { Closure closure -> closure() }
      createPage(0) { final ZonedDateTime zdtPageDate ->
        logger.debug "journalProjectMock.createPage called for zoned date: $date"
      }

      createPage(0) { final Date date ->
        logger.debug "journalProjectMock.createPage called for date: $date"
      }
    }

    configSlurperMock.use {
      journalProjectMock.use {
        new WebDoxy( options ) {
          void createPage( final ZonedDateTime dateOfPageToCreate ) {
            ++currentPageNum
            assert currentPageNum <= expectedPageDates.size()
            logger.debug "Fake createPage No. $currentPageNum with ZonedDateTime: $dateOfPageToCreate"
            assert expectedLocalDates[ currentPageNum ] == dateOfPageToCreate.toLocalDate()
          }
        }.addDiaryPage()
      }
    }
  }

  void testAddDiaryPageDefaultDate() {
    logger.debug 'Start of testAddDiaryPageDefaultDate()'
    buildPages()
    logger.debug 'End of testAddDiaryPageDefaultDate()'
  }

  void testValidateMarkDownProject() {
    logger.debug 'Start of validateMarkDownProject()'

    [true,false].each { projectMissing ->
      resetConfigSlurper()
      final String projectName = theProjectName( projectMissing )
      logger.debug projectName

      MockFor projectMock = createProjectMock( projectMissing, projectName )
      configSlurperMock.use {
        projectMock.use {
          try {
            WebDoxy wd = new WebDoxy( options )
            wd.metaClass.validateMarkDownProjectFiles { final Project project ->
              logger.debug 'In stub for validateMarkDownProjectFiles()'
              void
            }
            wd.validateMarkDownProject( projectName )
          } catch ( Exception ex ) {
            logger.debug '=' * 20
            logger.debug ex.class.name
            logger.debug ex.message
            logger.debug '=' * 20
            assert ex.message.contains( projectName )
            assert projectMissing
          }
        }
      }
    }
    logger.debug 'End of validateMarkDownProject()'
  }

  final theProjectName( final Boolean projectMissing ) {
    ">>project that does${projectMissing ? ' not' : ''} exist<<";
  }

  final MockFor createProjectMock( final Boolean projectMissing, final String projectName ) {
    logger.debug "** Testing with project.exists() returning ${!projectMissing}"
    MockFor projectMock = new MockFor( Project )
    projectMock.demand.getName(1) { projectName }
    projectMock.demand.exists(1) { !projectMissing }
    projectMock
  }
}

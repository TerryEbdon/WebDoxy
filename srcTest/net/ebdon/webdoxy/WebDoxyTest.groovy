package net.ebdon.webdoxy;

import groovy.test.GroovyTestCase;
import groovy.mock.interceptor.MockFor;
import groovy.ant.AntBuilder;

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
      ]
    ]
  ];

  private MockFor resourceMock;
  private MockFor configSlurperMock;

  Expando options;

  @Override
  void setUp() {
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
      arguments: {['Fred']},
    )

    resourceMock      = new MockFor( Resource )
    configSlurperMock = new MockFor( ConfigSlurper )

    configSlurperMock.demand.parse { final URL url ->
      assert url.file ==~ '.*config.groovy$'
      logger.debug 'configSlurperMock.demand.parse called'
      config
    }

    resourceMock.demand.message { final String key, final Object[] args ->
      final String returnVal = "Resource.message() called with key $key & args $args"
      logger.info returnVal
      assert key == 'backup.copyDriveOffline'
      returnVal
    }
  }

  void testGetTargetDateDefault() {
    logger.debug 'Start of testGetTargetDateDefault()'
    configSlurperMock.use {
      WebDoxy build = new WebDoxy( options )
      final Date parsedDate = build.getTargetDate()
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
      final Date parsedDate = build.getTargetDate()
      logger.debug "target date returned as: $parsedDate"
      assert parsedDate
      assert 99 == parsedDate.year
      assert  0 == parsedDate.month
      assert  1 == parsedDate.date
      assert  5 == parsedDate.day
    }
    logger.debug 'End of testGetTargetDateFromOption()'
  }
}

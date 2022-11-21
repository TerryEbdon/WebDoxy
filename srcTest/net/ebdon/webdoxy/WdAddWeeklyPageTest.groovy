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
class WdAddWeeklyPageTest extends GroovyTestCase {

  final private static Map config = [
    doxygen: [
      path: '.',
      ant: [
        classPath: '.',
        className: 'dummy doxygen class name'
      ]
    ],
    project: [
      journal: [
        format: [
          'anchorDay': 'yyyyMMdd'
        ]
      ]
    ]
  ];

  private MockFor antMock;
  private MockFor resourceMock;
  private MockFor configSlurperMock;
  private MockFor weeklyProjectMock;

  void testAddWeeklyPage() {
    logger.trace 'Start of testAddWeeklyPage()'
    final Expando options = new Expando(
      project  : false,
      create   : false,
      journal  : false,
      week     : { logger.debug 'week: true'; true },
      validate : false,
      generate : false,
      toc      : false,
      backup   : false,
      stub     : false,
      arguments: { ['Fred'] }
    )

    antMock           = new MockFor( AntBuilder )
    resourceMock      = new MockFor( Resource )
    configSlurperMock = new MockFor( ConfigSlurper )
    weeklyProjectMock = new MockFor( WeeklyProject )

    configSlurperMock.demand.parse { final URL url ->
      assert url.file ==~ '.*config.groovy$'
      logger.trace 'configSlurperMock.demand.parse called'
      config
    }

    antMock.demand.taskdef { Map<String,String> args ->
      assert args.name == 'doxygen' && args.classpath == '.'
    }

    resourceMock.demand.message { final String key, final Object[] args ->
      final String returnVal = "Resource.message() called with key $key & args $args"
      logger.info returnVal
      assert key == 'backup.copyDriveOffline'
      returnVal
    }

    weeklyProjectMock.demand.with {
      createPage { final Date date ->
        logger.debug "WeeklyProjectMock.createPage called for date: $date"
      }
    }

    configSlurperMock.use {
      weeklyProjectMock.use {
        antMock.use {
          WebDoxy build = new WebDoxy( options )
          build.addWeeklyPage()
        }
      }
    }
    logger.trace 'End of testAddWeeklyPage()'
  }
}

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
class ProjectTest extends GroovyTestCase {
  final private static String pageDateFormat = 'dd-MMM-yyyy'
  final private static Map config = [
    project: [
      page: [
        dateFormat: pageDateFormat
      ]
    ]
  ];

  @TypeChecked
  void testPageDate() {
    final Project project = new Project( 'Test Project', config )
    assert project.thePageDate
    assert project.pageDate
    assert project.zonedDate
    assert project.pageDate == project.zonedDate.format( pageDateFormat )
  }
}

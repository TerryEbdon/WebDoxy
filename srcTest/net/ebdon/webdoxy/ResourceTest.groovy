package net.ebdon.webdoxy;

import groovy.test.GroovyTestCase;
import org.apache.tools.ant.BuildException;
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

class ResourceTest extends GroovyTestCase {

  private final String goodMsgKey = 'backup.alreadyExists'
  private final String badMsgKey  = 'xyzzy'

  @TypeChecked
  void testMessageBadKey() {
    shouldFail( BuildException ) {
      try {
        new Resource().message( badMsgKey )
      } catch ( BuildException bex ) {
        assert bex.message.contains( badMsgKey )
        assert bex.message.contains( 'Can\'t find resource for bundle' )
        throw bex
      }
    }
  }

  @TypeChecked
  void testMessageGoodKey() {
    final String msg = new Resource().message( goodMsgKey )

    assert msg?.length()
    assert !msg?.contains( goodMsgKey )
  }
}

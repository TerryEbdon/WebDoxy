package net.ebdon.webdoxy;

import groovy.test.GroovyTestCase;
import groovy.mock.interceptor.MockFor;
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

@groovy.util.logging.Log4j2('logger')
class ResourceTest extends GroovyTestCase {

  private final String goodMsgKey     = 'backup.alreadyExists';
  private final String goodMsgKeyArgs = 'project.stub.creatingToDo';
  private final String badMsgKey      = 'xyzzy';
  private final String[] args         = ['wibble', 'wobble'];

  private MockFor bundleMock;

  private final ResourceBundle dummyResourceBundle = new ResourceBundle() {
    private int numGetStringCalls = 0

    @TypeChecked
    @Override protected Object handleGetObject(String key) {
      ++numGetStringCalls
      "${numGetStringCalls}: ResourceBundle.getString( $key )".toString()
    }

    @TypeChecked
    @Override Enumeration<String> getKeys() {
      Collections.emptyEnumeration();
    }

    @TypeChecked
    final int getNumGetStringCalls() {
      this.numGetStringCalls
    }
  }

  @TypeChecked
  @Override void setUp() {
    super.setUp()
    logger.trace 'Test setup'
    bundleMock = new MockFor( ResourceBundle )
  }

  @TypeChecked
  void testMessageBadKey() {
    logger.info 'Start of testMessageBadKey()'
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
    logger.info 'Start of testMessageGoodKey()'
    final String msg = new Resource().message( goodMsgKey )

    assert msg?.length()
    assert !msg.contains( goodMsgKey )
  }

  @TypeChecked
  void testMessageGoodKeyArgs() {
    logger.info 'Start of testMessageGoodKeyArgs()'
    final Resource res = new Resource()
    final String msg = res.message( goodMsgKeyArgs, args )

    assert msg?.length()
    assert !msg.contains( goodMsgKey )
    args.each { final String arg ->
      assert msg.contains( arg )
    }
  }

  @TypeChecked
  void testMessageBadKeyArgs() {
    logger.info 'Start of testMessageBadKeyArgs()'
    shouldFail( BuildException ) {
      try {
        new Resource().message( badMsgKey, args )
      } catch ( final BuildException bex ) {
        assert bex.message.contains( badMsgKey )
        assert bex.message.contains( 'Can\'t find resource for bundle' )
        throw bex
      }
    }
  }

  void testLoadMissingBundle() {
    final String expectedBundleName = 'Language'
    logger.info 'Start of testLoadMissingBundle()'

    bundleMock.demand.getBundle { final String name ->
      logger.info "getBundle called with name: $name"
      assert expectedBundleName == 'Language'
      throw new MissingResourceException(
        expectedBundleName, this.class.name, name
      )
    }

    bundleMock.use {
      final Resource res = new Resource()
      shouldFail( BuildException ) {
        try {
          res.message( goodMsgKey )
        } catch ( final BuildException bex ) {
          assert bex.message.contains( expectedBundleName )
          throw bex
        }
      }
      assert dummyResourceBundle.numGetStringCalls == 0
    }
  }

  void testLoadBundle() {
    logger.info 'Start of testLoadBundle()'

    bundleMock.demand.getBundle { final String name ->
      logger.info "getBundle called with name: $name"
      assert name == 'Language'
      dummyResourceBundle
    }

    bundleMock.use {
      final Resource res = new Resource()
      1.upto(2) { // 1: not loaded, 2: already loaded
        res.message( goodMsgKey )
        assert dummyResourceBundle.numGetStringCalls == it
      }
    }
  }
}

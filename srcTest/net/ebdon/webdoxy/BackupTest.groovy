package net.ebdon.webdoxy;

import groovy.test.GroovyTestCase;
import groovy.mock.interceptor.MockFor;
import groovy.ant.AntBuilder;
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
class BackupTest extends GroovyTestCase {

  final private static Map config = [
    backup: [
      excludesFile  : 'backupExcludes.txt',
      copyRoot      : 'the copy root',
      copyFolderRoot: 'the folder root'
    ]
  ];

  private MockFor antMock;
  private MockFor resourceMock;
  private MockFor configSlurperMock;

  void testBackup() {
    antMock           = new MockFor( AntBuilder )
    resourceMock      = new MockFor( Resource )
    configSlurperMock = new MockFor( ConfigSlurper)

    resourceMock.demand.message { final String key, final Object[] args ->
      final String returnVal = "Resource.message() called with key $key & args $args"
      logger.info returnVal
      assert key == 'backup.copyDriveOffline'
      returnVal
    }

    antMock.demand.'with'(1) { Closure closure ->
      logger.info 'Calling closure'
      closure()
      logger.info 'closure invoked'
    }

    configSlurperMock.demand.parse { final URL url ->
      assert url.file ==~ '.*config.groovy$'
      config
    }

    antMock.use {
      resourceMock.use {
        configSlurperMock.use {
          final Backup bu = new Backup() {
            boolean filesetCalled = false
            private boolean folderCreated = false
            boolean zipped = false

            @TypeChecked
            final void mkdir( final Map<String,String> args ) {
              logger.info "mkdir called with args: $args"
              folderCreated = true
            }
            @TypeChecked
            final void zip( final Map<String,String> args, final Closure closure ) {
              logger.info "Backup.zip called with $args"
              assert folderCreated
              assert args.destfile.contains('.zip')
              assert closure
              assert !filesetCalled
              zipped = true
              closure()
              logger.info "Back from zip closure, filesetCalled: $filesetCalled"
              assert filesetCalled
            }
            final void fileset( final Map<String,String> args ) {
              logger.info "fileset() called with args $args"
              assert args.dir == '.'
              assert args.excludesfile == BackupTest.config.backup.excludesFile
              filesetCalled = true
            }
            @TypeChecked
            final void fail( final String failMsg ) {
              logger.info "fail() called with $failMsg"
              throw new BuildException( failMsg )
            }
          }
          shouldFail( BuildException ) {
            try {
              bu.run()
            } catch ( final BuildException bex ) {
              logger.info "Caught build exception: $bex"
              assert bex.message.contains( 'backup.copyDriveOffline' )
              assert bu.zipped && bu.filesetCalled
              throw bex
            }
          }
        }
      }
    }
    logger.info 'testBackup() completed'
  }
}

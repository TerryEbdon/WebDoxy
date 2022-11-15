package net.ebdon.webdoxy;

import groovy.ant.AntBuilder;
import java.text.SimpleDateFormat;
/**
 * @file
 * @author  Terry Ebdon
 * @date    June 2017
 * @copyright
 *
 * Copyright 2017 Terry Ebdon
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

/**
@brief Groovy script to backup the web-doxy folder.
@author Terry Ebdon
@version 0.1
@todo Add command line options.
@todo Move all string constants into config.groovy
@todo Move all message strings into Language.properties

@brief Backup all source files into a date/time based zip in a year/year-month specific folder
*/
class Backup {

  def config;
  def ant = new AntBuilder();

  public static main( args ) {
    new Backup().run()
  }

  Backup() {
    config = new ConfigSlurper().
      parse( new File( 'config.groovy' ).
      toURI().toURL() )
  }

  public void run() {
    final SimpleDateFormat sdfTimestamp = new SimpleDateFormat('yyyy-MM-dd_HHmm')
    final SimpleDateFormat sdfSuffix    = new SimpleDateFormat('yyyy/yyyy-MM')
    final Date backupDate               = new Date()
    final String backupTimestamp        = sdfTimestamp.format( backupDate )
    final String backupFolderSuffix     = sdfSuffix.format( backupDate )
    final String backupFolder           = "backup/$backupFolderSuffix"
    final String backupCopyRoot         = config.backup.copyRoot
    final String backupCopyFolderRoot   = config.backup.copyFolderRoot
    ant.with {
      echo level: 'debug', "Timestamp: $backupTimestamp"
      echo level: 'debug', "Folder: $backupFolder"
      mkdir dir: backupFolder

      def pathBits = new File('.').absolutePath.split('\\\\')
      assert pathBits.length > 1
      final String baseDir = pathBits[ pathBits.length == 2 ? -1 : -2 ]
      String zipFile = "$backupFolder/${backupTimestamp}_${baseDir}.zip"
      echo level: 'debug', zipFile
      if ( !new File( zipFile ).exists() ) {
        zip( destfile: zipFile ) {
          fileset(
            dir: '.',
            excludesfile: config.backup.excludesFile
          )
        }

        if ( new File( backupCopyRoot ).exists() ) {
          final String backupCopyFolder = backupCopyFolderRoot +
              baseDir + '/' + backupFolderSuffix
          mkdir dir: backupCopyFolder
          copy file: zipFile, todir: backupCopyFolder
        } else {
          final String failureReason =
            new Resource().message(
              'backup.copyDriveOffline',
              [backupCopyRoot] as Object[]
            )
          echo level: 'debug', "Failing with: $failureReason"
          fail failureReason
        }
      } else {
        new Resource().with {
          echo level: msgWarn, message( 'backup.alreadyExists' )
        }
      }
    }
  }
}

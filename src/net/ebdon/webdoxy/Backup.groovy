package net.ebdon.webdoxy;

import groovy.ant.AntBuilder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

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

  final config;
  final AntBuilder ant = new AntBuilder();

  Backup() {
    config = new ConfigSlurper().
      parse( new File( 'config.groovy' ).
      toURI().toURL() )
  }

  void run() {
    final DateTimeFormatter fmtTimestamp = DateTimeFormatter.ofPattern('yyyy-MM-dd_HHmm')
    final DateTimeFormatter fmtSuffix    = DateTimeFormatter.ofPattern('yyyy/yyyy-MM')
    final ZoneId zoneId                  = ZoneId.of('Etc/UTC')
    final ZonedDateTime backupDate       = ZonedDateTime.now(zoneId)

    final String backupTimestamp         = backupDate.format( fmtTimestamp )
    final String backupFolderSuffix      = backupDate.format( fmtSuffix )
    final String backupFolder            = "backup/$backupFolderSuffix"
    final String backupCopyRoot          = config.backup.copyRoot
    final String backupCopyFolderRoot    = config.backup.copyFolderRoot

    ant.with {
      echo level: Resource.msgDebug, "Timestamp: $backupTimestamp"
      echo level: Resource.msgDebug, "Folder: $backupFolder"
      mkdir dir: backupFolder
      final String currentFolder = '.'
      final List<String> pathBits = new File(currentFolder).absolutePath.split('\\\\')
      assert pathBits.size > 1
      final String baseDir = pathBits[ pathBits.size == 2 ? -1 : -2 ]
      String zipFile = "$backupFolder/${backupTimestamp}_${baseDir}.zip"
      echo level: Resource.msgDebug, zipFile
      if ( !new File( zipFile ).exists() ) {
        zip( destfile: zipFile ) {
          fileset(
            dir: currentFolder,
            excludesfile: config.backup.excludesFile,
            excludes: '**/*.7z, **/*.zip, **/*.rar, **/*.tar'
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
          echo level: Resource.msgDebug, "Failing with: $failureReason"
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

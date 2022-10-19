package net.ebdon.webdoxy;
import groovy.ant.AntBuilder;
import java.text.MessageFormat;

/**
 * @file
 * @author  Terry Ebdon
 * @date    OCT-2022
 * @copyright
 *
 * Copyright 2022 Terry Ebdon
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
 @brief   Localisaton class that wraps a resource bundle
 @author  Terry Ebdon
 */

  class Resource {
    AntBuilder ant;
    ResourceBundle bundle;

  Resource() {
    ant = new AntBuilder()
  }

  String message( final String msgId ) {
    loadBundle()
    try {
      ant.echo level: 'debug', "Getting string for key $msgId without args"
      bundle.getString( msgId )
    } catch ( java.util.MissingResourceException ex ) {
      ant.echo '.'
      ant.echo '---------------------------------------------'
      ant.fail "Couldn't load resource / message: ${ex.message}"
    }
  }

  private void loadBundle() {
    try {
      ant.echo level: 'debug', "Checking resource bundle."
      if (!bundle) {
        ant.echo level: 'debug', 'Bundle not loaded yet... getting it.'
        bundle = ResourceBundle.getBundle( "resources.Language" )
      } else {
        ant.echo level: 'debug', "Nothing to do.. resource bundle was already loaded."
      }
    } catch ( java.util.MissingResourceException ex ) {
      ant.echo '.'
      ant.echo '---------------------------------------------'
      ant.fail "Failed to load resource bundle: ${ex.message}"
    }
    ant.echo level: 'debug', "Resource bundle looks good."
  }
}

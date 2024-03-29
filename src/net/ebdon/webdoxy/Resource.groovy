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

@groovy.util.logging.Log4j2('logger')
class Resource {
  static final String msgDebug = 'debug';
  static final String msgInfo  = 'info';
  static final String msgWarn  = 'warn';
  static final String msgError = 'error';

  private final AntBuilder  ant;
  private ResourceBundle    bundle;

  Resource() {
    ant = new AntBuilder()
  }

  String message( final String msgId ) {
    logger.trace "Getting string for key $msgId without args"
    loadBundle()
    try {
      bundle.getString( msgId )
    } catch ( java.util.MissingResourceException ex ) {
      ant.fail "Couldn't load resource / message: ${ex.message}"
    }
  }

  String message( final String msgId, Object[] msgArgs ) {
    loadBundle()
    String rawMsg
    try {
      logger.trace "Getting string for key $msgId with args"
      rawMsg = bundle.getString( msgId )
      logger.trace "Unformatted message is: $rawMsg"
    } catch ( java.util.MissingResourceException ex ) {
      ant.fail "Couldn't load resource / message: ${ex.message}"
    }

    MessageFormat formatter = new MessageFormat('')
    logger.trace "Formatting $msgId with args: ${msgArgs}"
    formatter.locale = Locale.default
    formatter.applyPattern( rawMsg )
    formatter.format( msgArgs )
  }

  private void loadBundle() {
    try {
      logger.trace 'Checking resource bundle.'
      if (!bundle) {
        logger.trace 'Bundle not loaded yet, getting it.'
        bundle = ResourceBundle.getBundle( 'Language' )
      } else {
        logger.trace 'Nothing to do, resource bundle was already loaded.'
      }
    } catch ( java.util.MissingResourceException ex ) {
      ant.fail "Failed to load resource bundle: ${ex.message}"
    }
    logger.trace 'Resource bundle looks good.'
  }
}

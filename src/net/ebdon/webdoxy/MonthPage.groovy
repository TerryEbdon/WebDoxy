package net.ebdon.webdoxy;
import java.text.SimpleDateFormat;

/**
 * @file
 * @author  Terry Ebdon
 * @date    23-JUN-2017
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

class MonthPage extends JournalPage {

	MonthPage( JournalProject jp, File monthFile ) {
		super( jp, monthFile )
		project.ant.echo level: 'debug', 'MonthProject instantiated'
	}

	def init() {
		final SimpleDateFormat anchorFormat = project.dateFormatter( 'anchorMonth' ) ///@todo fix
		final SimpleDateFormat titleformatter    = project.dateFormatter( 'month' ) ///@todo fix

		anchorDate = anchorFormat.format( pageDate )
		title      = titleformatter.format( pageDate )
	}

	def addSubPage( final JournalPage dayPage ) {
		assert dayPage
		project.ant.echo level: 'info', "Adding page ${dayPage.title} to month page $title"

		final prefix = project.buildConfig.project.journal.pages.monthly.linkPrefix
		final suffix = project.buildConfig.project.journal.pages.monthly.linkSuffix

		append "${prefix}@subpage ${dayPage.pageAnchor}${suffix}"
	}

	def create() {
		project.ant.echo level: 'debug', 'MonthPage.create() called'

		if ( exists() ) {
			project.ant.echo level: 'info', project.message( 'MonthPage.alreadyExists' )
		} else {
			super.create()
		}
	}

	def createSkeletonBody() {
		;
	}

	def createSkeletonFooter() {
		;
	}

	def getHtmlFileNames() {
		buildConfig.project.journal.pages.monthly.htmlIncludes
	}

	def getH1Anchor() {
		"h${anchorDate}"
	}

	def getFirstHeaderTitleFormat() {
		 project.dateFormatter( 'longerMonth' )
	}

	def addSuffix( final dateString ) {
		dateString
	}
}

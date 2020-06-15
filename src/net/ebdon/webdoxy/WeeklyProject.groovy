package net.ebdon.webdoxy;

import java.text.SimpleDateFormat;
import java.time.temporal.IsoFields;

/**
 * @file
 * @author	Terry Ebdon
 * @date	JUN-2017
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

class WeeklyProject extends JournalProject {

	WeeklyProject( projectName, buildConfig ) {
		super( projectName, buildConfig )
		// final SimpleDateFormat anchorFormat = dateFormatter( 'anchor.day' )
	}

	java.time.ZonedDateTime getZonedDate() {
		pageDate.toZonedDateTime()
	}

	int getPageWeek() {
		zonedDate.get( IsoFields.WEEK_OF_WEEK_BASED_YEAR )
	}

	String getPageTitle() {
		// final zonedDate = pageDate.toZonedDateTime()
		final pageYear = zonedDate.get( IsoFields.WEEK_BASED_YEAR )
		// final pageWeek = zonedDate.get( IsoFields.WEEK_OF_WEEK_BASED_YEAR )
		final Date sunday = pageDate + 6

		ant.echo level: 'info',
			"Creating weekly page title for year $pageYear, week $pageWeek"

		final pageAnchor = "{#y${pageYear}_w${pageWeek}}"
		"# ${monthTitle( pageDate, sunday )} -- $pageYear week $pageWeek $pageAnchor"
	}

	Date startOfWeek( final Date date ) {
		date - date.toZonedDateTime().dayOfWeek.value + 1 // Monday of target week
	}

	def getPageYear() {
		new SimpleDateFormat( 'yyyy' ).format( pageDate )
	}

	String getQuarterFolder() {
		// dateFolder 'quarter'
		// println pageWeek.class.name()
		"${pageYear}-q${pageQuarter}"
	}

	def getPageQuarter() {
		final def quartersPerYear = 4
		final def minWeeksPerQuarter = 13

		new BigDecimal( pageWeek )
			.divide( minWeeksPerQuarter, 0, BigDecimal.ROUND_UP )
			.min( quartersPerYear )
	}

	@Override
	String getFullFolderPath() {
		"${yearFolderPath}${quarterFolder}/"
	}

	@Override
	def getPageFileName() {
		final SimpleDateFormat fileNameFormat = dateFormatter( 'weekFileName' )
		final def fileName   = fileNameFormat.format( pageDate ) + markdownFileType
	}

	@Override
	def createPage( final Date date ) {

		pageDate = startOfWeek( date )

		ant.with {
			echo level: 'info', "fullFolderPath: $fullFolderPath"
			echo level: 'info', "Full path: $fullPath"

			mkdir dir: fullFolderPath
			echo level: 'info', "Creating page file: $fullPath"
		}

		File pageFile = new File( fullPath )

		if ( !pageFile.exists() ) {

			def page = new WeekPage( this, pageFile )
			
      page.create()
			addPageToMonth page
		} else {
			ant.echo level: 'warn',  message( 'JournalProject.nothingToDo' )
			ant.echo level: 'debug', " --> ${pageFile.absolutePath}"
		}
	}

  @Override
  def getMonthFolderPath() {
    yearFolderPath
  }

}

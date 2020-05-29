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

class JournalProject extends Project {

	private Date pageDate = new Date();

	JournalProject( projectName, buildConfig ) {
		super( projectName, buildConfig )
		final SimpleDateFormat anchorFormat = dateFormatter( 'anchor.day' )
	}

	def getPageDate() {
		this.pageDate
	}

	String monthTitle( final Date monday, final Date sunday ) {

		final SimpleDateFormat monthFormat =	new SimpleDateFormat( 'MMM' )

		final startMonth = monthFormat.format( monday )
		final endMonth	 = monthFormat.format( sunday )

		// println "startMonth: $startMonth"
		// println "endMonth:   $endMonth"

		startMonth == endMonth ? startMonth : "$startMonth / $endMonth"
	}

	String getPageTitle() {
		final zonedDate = pageDate.toZonedDateTime()
		final pageYear = zonedDate.get( IsoFields.WEEK_BASED_YEAR )
		final pageWeek = zonedDate.get( IsoFields.WEEK_OF_WEEK_BASED_YEAR )
		final Date sunday = pageDate + 6

		ant.echo level: 'info',
			"Creating weekly page title for year $pageYear, week $pageWeek"

		final pageAnchor = "{#y${pageYear}_w${pageWeek}}"
		"# ${monthTitle( pageDate, sunday )} -- $pageYear week $pageWeek $pageAnchor"
	}

	def createWeekPage ( final Date date ) {

		pageDate = date - date.toZonedDateTime().dayOfWeek.value + 1 // Monday of target week
		
		println pageTitle
		println '\n[TOC]\n'

		final SimpleDateFormat pageDateFormat =
			new SimpleDateFormat( '''## dd EEE {#'day'_yyyy_MM_dd}\n''' )
			// new SimpleDateFormat( buildConfig.project.page.dateFormat )

		7.times {
			println pageDateFormat.format( pageDate++ )
		}
	}

	def createPage( Date date ) {
		pageDate = date

		final fullFolderPath = "${monthFolderPath}${dayFolder}"
		final fullPath       = "${fullFolderPath}${pageFileName}"

		ant.with {
			echo level: 'debug', "fullFolderPath: $fullFolderPath"
			echo level: 'debug', "Full path: $fullPath"

			mkdir dir: fullFolderPath
			echo level: 'info', "Creating page file: $fullPath"
		}

		File pageFile = new File( fullPath )

		if ( !pageFile.exists() ) {
			def page = new JournalPage( this, pageFile )
			page.create()
			addPageToMonth page
		} else {
			ant.echo level: 'warn',  message( 'JournalProject.nothingToDo' )
			ant.echo level: 'debug', " --> ${pageFile.absolutePath}"
		}
	}


	def addPageToMonth( JournalPage dayPage ) {
		assert dayPage
		if ( buildConfig.project.journal.pages.monthly.required ) {
			final def monthFmtStr = buildConfig.project.journal.pages.monthly.format
			ant.echo level: 'debug', "Month format string: ${monthFmtStr}"
			final def SimpleDateFormat monthFormatter = new SimpleDateFormat( monthFmtStr ?: 'MMMM' )
			final def monthFileName = monthFormatter.format( pageDate ) + markdownFileType
			File monthFile = new File( "${monthFolderPath}/${monthFileName}" )

			MonthPage monthPage = new MonthPage( this, monthFile )
			monthPage.create()
			if ( buildConfig.project.journal.pages.monthly.addLinkToNewDayPage ) {
				ant.echo level: 'info', "Adding: $dayPage"
				ant.echo level: 'info', "    to: $monthPage"
				monthPage.addSubPage dayPage
			}
		}
	}

	def getMonthFolderPath() {
		"${sourceFolder}/${yearFolder}${monthFolder}"
	}

	def getPageFileName() {
		final SimpleDateFormat fileNameFormat = dateFormatter( 'fileName' )
		final def fileName   = fileNameFormat.format( pageDate ) + markdownFileType
	}
	def getYearFolder() {
		dateFolder 'annual'
	}
	def getMonthFolder() {
		dateFolder 'month'
	}
	def getDayFolder() {
		dateFolder 'day', 'daily'
	}

	/**
	 * @param folderReqdProp	annual, month or day -- name of string property.
	 * @param formatProp    	folderProp + 'ly' name of boolean property.
	 * @return        The folder name or an empty string, if not required.
	 */
	def dateFolder( String formatProp, String  folderReqdProp = formatProp + 'ly' ) {
		ant.echo level:'debug', "folderProp: $folderReqdProp, formatProp: $formatProp"
		folderRequired( folderReqdProp ) ? dateFormatter( formatProp ).format( pageDate) +'/' : ''
	}

	def folderRequired( reqdPropertyName ) {
		final required = buildConfig.project.journal.folders[ reqdPropertyName ]
		ant.echo level: 'debug', "$reqdPropertyName Required: $required"
		required
	}

	SimpleDateFormat dateFormatter( name ) {
		ant.echo level: 'debug', "Getting journal date format name: $name"
		final format = buildConfig.project.journal.format[name]
		ant.echo level: 'debug', "journal date format $name = $format"
		new SimpleDateFormat( format )
	}
}

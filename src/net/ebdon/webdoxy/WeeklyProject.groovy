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

	Date startOfWeek( final Date date ) {
		date - date.toZonedDateTime().dayOfWeek.value + 1 // Monday of target week
	}

	@Override
	def createPage( final Date date ) {

		pageDate = startOfWeek( date )

		println pageTitle
		println '\n[TOC]\n'

		final SimpleDateFormat pageDateFormat =
			new SimpleDateFormat( '''## dd EEE {#'day'_yyyy_MM_dd}\n''' )
			// new SimpleDateFormat( buildConfig.project.page.dateFormat )

		7.times {
			println pageDateFormat.format( pageDate++ )
		}
	}
}

/**
 * @file
 * @author	Terry Ebdon
 * @date	23-JUN-2017
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

package net.ebdon.webdoxy;
import java.text.SimpleDateFormat

class JournalPage {

	final JournalProject project
	final buildConfig
	private File pageFile
	private Date pageDate = new Date()
	private String anchorDate_ // final?
	private String title_ // final?

	/**
	 * @author Terry Ebdon
	 * @date 23-JUN-2017
	 * @todo The file shouldn't be passed in.
	 */
	JournalPage( JournalProject jp, File file ) {
		project = jp
		pageFile = file
		pageDate = project.pageDate
		buildConfig = project.buildConfig
		init()
		project.ant.echo level:'info', "JournalPage instantiated, file: ${file.name}"
	}

	def init() {
		final SimpleDateFormat dayAnchorFormatter = project.dateFormatter( 'anchorDay' )
		final SimpleDateFormat shortFormat        = project.dateFormatter( 'shorter' )

		project.ant.echo level: 'info', "${dayAnchorFormatter.format( pageDate )}"
		anchorDate = dayAnchorFormatter.format( pageDate )
		title      = shortFormat.format( pageDate )
	}

	public String toString() {
		final String className = this.class.simpleName.padLeft(11)
		final String displayAnchor = anchorDate.padRight(8)
		"${className} project: ${project.name} pageDate: $pageDate anchorDate: $displayAnchor title: $title"
	}

	def getPageDate() {
		this.pageDate
	}
	def setAnchorDate( final String newDate ) {
		anchorDate_ = newDate
		project.ant.echo level:'debug', "setAnchorDate called with $newDate"
		project.ant.echo level:'debug', "anchorDate_ is now: ${anchorDate_}"
	}

	def getAnchorDate() {
		// project.ant.echo level:'debug', "getAnchorDate returning ${anchorDate_}"
		anchorDate_
	}

	def setTitle( final newTitle ) {
		title_ = newTitle
	}

	def getTitle() {
		title_
	}
	def create() {
		assert pageFile
		project.ant.echo level: 'debug', "JournalPage.create() called."
		project.ant.echo level: 'debug', "Page file: $pageFile"

		createSkeleton()
	}

	def createSkeleton() {
		createSkeletonHeader()
		createSkeletonBody()
		createSkeletonFooter()
	}

	def createSkeletonHeader() {
		append "@page ${pageAnchor} $title\n"
		append "@anchor ${h1Anchor}"
		addHtmlHeaderFragments()
		append "# $firstHeaderTitle\n"
	}

	def createSkeletonBody() {
		append "\n@todo Add content to journal page.\n"
		append "\n## Interesting web pages"
		append "\n### Tweets\n"
		append tweetTemplate
		append tweetTemplate
		append tweetTemplate
	}

	def addHtmlHeaderFragments() {
		def htmlFilesToInclude = htmlFileNames
		if ( htmlFilesToInclude.size() ) {
			project.ant.echo level: 'info', "Including: ${htmlFilesToInclude} into ${title}"
			htmlFilesToInclude.each { fileName ->
				addHtml fileName
			}
		} else {
			noHtmlIncludesWarning()
		}
	}

	def noHtmlIncludesWarning() {
		final msg = "No WebDoxy HTML includes configured for ${title}"
		project.ant.echo level: 'warn', msg
		appendComment msg
	}

	def getHtmlFileNames() {
		buildConfig.project.journal.pages.daily.htmlIncludes
	}

	def addHtml( fileName ) {
		append "@htmlinclude $fileName"
	}

	def createSkeletonFooter() {
		htmlOnly {
			"<a class='btn' href='#${h1Anchor}'>Top of page</a>"
		}
	}

	def getTweetTemplate() {
		/// @todo add filter to
		/// convert \@tweep terry_ebdon into [\\@jack](https://twitter.com/\@jack)
		///

		buildConfig.project.journal.pages.tweetTemplate
	}


	def getFirstHeaderTitle() {
		final SimpleDateFormat longFormat = firstHeaderTitleFormat
		addSuffix( longFormat.format( project.pageDate ) )
	}

	def addSuffix( final dateString ) {
		dateString.replace( '??', pageDateSuffix )
	}

	def getPageDateSuffix() {
		if ( buildConfig.project.journal.pages.useHtmlDateSuffix ) {
			dayNumberHtmlSuffix( project.pageDate )
		} else {
			dayNumberSuperSuffix( project.pageDate )
		}
	}

	def dayNumberHtmlSuffix( date ) {
		"<sup>${dayNumberSuffix( date )}</sup>"
	}

	def getFirstHeaderTitleFormat() {
		project.dateFormatter( 'longer' )
	}

	def appendComment( msg ) {
		append "<!-- $msg -->"
	}

	def append( final content ) {
		assert pageFile
		if ( this.class.name.contains( 'Month' ) ) {
			project.ant.echo level: 'debug', "Appending to: $pageFile"
			project.ant.echo level: 'debug', "        path: ${pageFile.path}"
			project.ant.echo level: 'debug', "      exists: ${exists()}"
		}
		pageFile << content << '\n'
	}

	def exists() {
		assert pageFile
		pageFile.exists()
	}

	def getPageAnchor() {
		"wip${anchorDate}"
	}

	def getH1Anchor() {
		"h${anchorDate}"
	}

	/**
	 * dayNumberSuffix get the English suffix for a day No., i.e. st, nd, rd or th.
	 * @param  final Date          d The date to get the day suffix for.
	 * @return       The English two letter day No. suffix.
	 * @author 	Terry Ebdon
	 * @date	23-JUN-2017
	 *
	 * @todo Allow for localisation
	 */
	def dayNumberSuffix( final Date d ) {
		switch ( d.date ) {
			case [1,21,31]:	"st"; break
			case 	[2,22]:	"nd"; break
			case 	[3,23]:	"rd"; break
			case	 4..30:	"th"; break
			default:
				project.ant.fail "Unexpected day No. ${d.date}"
		}
	}

	/*
	 * @todo Allow for localisation
	 * @todo Address code duplication
	 */
	def dayNumberSuperSuffix( final Date d ) {
		switch ( d.date ) {
			case [1,21,31]:	"ˢᵗ"; break
			case 	[2,22]:	"ⁿᵈ"; break
			case 	[3,23]:	"ʳᵈ"; break
			case	 4..30:	"ᵗʰ"; break
			default:
				project.ant.fail "Unexpected day No. ${d.date}"
		}
	}
	def htmlOnly( Closure closure ) {
		append "@htmlonly"
		append closure.call()
		append "@endhtmlonly"
	}
}

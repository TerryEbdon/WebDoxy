/**
 * @file
 * @author  Terry Ebdon
 */

 package net.ebdon.webdoxy;

import java.text.SimpleDateFormat;

class JournalProject extends Project {

    private final Date pageDate = new Date()

    JournalProject( projectName, buildConfig ) {
		super( projectName, buildConfig )
    	final SimpleDateFormat anchorFormat = dateFormatter( 'anchor.day' )
	}

    def createPage() {
        // final pageFolder     = "${yearFolder}${monthFolder}${dayFolder}"
        final fullFolderPath = "${monthFolderPath}${dayFolder}"
        // final pageFilePath   = "${pageFolder}${pageFileName}"
        // final fullPath       = "$sourceFolder/$pageFilePath"
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
            ant.echo level: 'warn', "Nothing to do --- Diary page already exists."
        }
    }

    def addPageToMonth( JournalPage dayPage ) {
        if ( buildConfig.project.journal.pages.monthly.required ) {
            final def monthFmtStr = buildConfig.project.journal.pages.monthly.format
            ant.echo level: 'debug', "Month format string: ${monthFmtStr}"
            final def SimpleDateFormat monthFormatter = new SimpleDateFormat( monthFmtStr ?: 'MMMM' )
            final def monthFileName = monthFormatter.format( pageDate ) + markdownFileType
            File monthFile = new File( "${monthFolderPath}/${monthFileName}" )

            MonthPage monthPage = new MonthPage( this, monthFile )
            monthPage.create()
            if ( buildConfig.project.journal.pages.monthly.addLinkToNewDayPage ) {
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
     * [dateFolder description]
     * @param  String folderReqdProp    annual, month or day -- name of string property.
     * @param  String formatProp    = folderProp + 'ly' name of boolean property.
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

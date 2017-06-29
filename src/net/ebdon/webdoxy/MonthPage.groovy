/**
 * @file
 * @author  Terry Ebdon
 * @date    23-JUN-2017
 */

package net.ebdon.webdoxy;
import java.text.SimpleDateFormat

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
            project.ant.echo level: 'info', "Monthly already page exists."
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

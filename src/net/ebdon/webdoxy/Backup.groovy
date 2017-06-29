package net.ebdon.webdoxy;

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

	def config
	def ant = new AntBuilder()

	public static main( args ) {
		new Backup().run()
	}

	Backup() {
		config = new ConfigSlurper().
			parse( new File( 'config.groovy' ).
			toURI().toURL() )
	}

	public void run() {
		ant.with {
			final String backupTimestamp    = new Date().format('yyyy-MM-dd_HHmm')
			final String backupFolderSuffix = "${new Date().format('yyyy/yyyy-MM')}"
			final String backupFolder       = "backup/$backupFolderSuffix"
			final String backupCopyRoot     = 'h:/'
			echo level: 'debug', "Timestamp:\t $backupTimestamp"
			echo level: 'debug', "Folder:\t $backupFolder"
			mkdir dir: backupFolder

			def pathBits = new File('.').absolutePath.split('\\\\')
			assert pathBits.length > 1
			final String baseDir = pathBits[ pathBits.length == 2 ? -1 : -2 ]
			String zipFile = "$backupFolder/${backupTimestamp}_${baseDir}.zip"
			echo level: 'debug', zipFile
			if ( !new File( zipFile ).exists() ) {
				zip( destfile: zipFile ) {
					fileset(
						dir: '.',
						excludesfile: config.backup.excludesFile
					)
				}
				if ( new File( backupCopyRoot ).exists() ) {
					final String backupCopyFolder = "${backupCopyRoot}Backups/web sites/$backupFolderSuffix"
					mkdir dir: backupCopyFolder
					copy file: zipFile, todir: backupCopyFolder
				} else {
					echo level: 'warn', "Backup drive is off-line!"
				}
			} else {
				echo level: 'warn', "Backup skipped, as zip already exists"
			}
		}
	}
}

/**
@file
@brief Class that generates a Table-of-Contents HTML project, for post-proccessing with Doxygen.
@author Terry Ebdon
@version 0.1
@todo Move all string constants into config.groovy
@todo Move all message strings into Language.properties
*/

package net.ebdon.webdoxy;

class TocProject extends Project {

	TocProject( buildConfig ) {
		super( buildConfig.project.toc.name, buildConfig )
	}

	void create() {
		deleteFile configFileName
		super.create()
	}

	String getBrief() {
		assert buildConfig
		buildConfig.project.toc.brief
	}

	def getLatexRequired() {
		"NO"
	}

	def getGenerateTreeView() {
		"NO"
	}

	/**
	 * Create main, index, page for the TOC project.
	 * @author Terry Ebdon
	 */
	void createMainPage() {
		assert ant
		deleteFile mainMarkDownFileName

		File tocFile = new File( mainMarkDownFileName )
		ant.with {
			echo level: 'info', "Scanning folder tree: $htmlRootFolder"
			def scanner = fileScanner {
				fileset( dir: htmlRootFolder ) {
					include( name: '**/index.html' )
					exclude( name: "**/${buildConfig.project.toc.name}/index.html" )
				}
			}

			tocFile << "# Known Projects {#mainpage}\n"
			for ( file in scanner ) {
				def projectName = file.parentFile.name
				echo level: 'info', "Indexing project $projectName"
				tocFile << " - [${makeDisplayable( projectName)}](../$projectName/index.html)\n"
			}
		}
	}

	/**
	 * Wrapper for the ant delete task.
	 * @param  filePath file to delete.
	 * @return          Nothing.
	 */
	private void deleteFile( filePath ) {
		File file = new File( filePath )
		if ( file.exists() ) {
			ant.echo level: 'warn', "Deleting file $filePath"
			file.delete()
		}
	}
}

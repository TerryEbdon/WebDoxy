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
package net.ebdon.webdoxy;

/**
@brief Class that generates a Table-of-Contents HTML project, for post-proccessing with Doxygen.
@author Terry Ebdon
@version 0.1
@todo Move all string constants into config.groovy
@todo Move all message strings into Language.properties
*/
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

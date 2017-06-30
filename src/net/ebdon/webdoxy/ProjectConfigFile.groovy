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

class ProjectConfigFile {
	final Project project
	File configFile
	AntBuilder ant

	ProjectConfigFile( Project p ) {
		assert p
		assert p.ant

		project = p
		ant = project.ant
	}

	void create() {
		configFile = new File( project.configFileName )
		if ( !configFile.exists() ) {
			ant.echo level: 'debug', "Creating configuration file ${project.configFileName}"
			project.with {
				set '@INCLUDE',			buildConfig.project.baseDoxyFile
				set 'PROJECT_NAME',		name
				set 'PROJECT_BRIEF',	"\"${brief}\""
				set 'INPUT',			sourceFolder
				set 'DIAFILE_DIRS',		diaFolder
				set 'ENABLED_SECTIONS',	name

				set 'OUTPUT_DIRECTORY',	outRoot
				set 'HTML_OUTPUT',		"${buildConfig.project.parentfolders.html}/$name"
				set 'LATEX_OUTPUT',		"${buildConfig.project.parentfolders.latex}/$name"
				add 'IMAGE_PATH',		imageFolder

				set 'GENERATE_HTML',	htmlRequired
				set 'GENERATE_LATEX',	latexRequired
				set 'DISABLE_INDEX',	disableIndex
				set 'GENERATE_TREEVIEW',generateTreeView

				exampleFolders.each { ef ->
					add 'EXAMPLE_PATH', "$rootFolder/$ef"
				}
			}
		} else {
			ant.echo level: 'warn', "Configuration file for project ${project.name} already exists."
		}
	}

	void set( key, value ) {
		assert configFile
		configFile << "${key.padRight( 25 )} = ${value}\n"
	}

	void add( key, value ) {
		assert configFile
		configFile << "${key.padRight( 24 )} += ${value}\n"
	}
}

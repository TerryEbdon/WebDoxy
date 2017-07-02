/**
@file
@author Terry Ebdon
@brief This file configures the build process.
*/

verboseCleanUp    = false
defaultProjects   = ['@Work-in-Progress']
markdown.fileType = '.md'

backup {
	excludesFile = "backupExcludes.txt"
}

project {
	toc {
		name             = 'TableOfContents'
		brief            = 'Directory of known, documented, projects that have been built.'

		disableIndex     = "NO"		// Overide the generic project setting.
		generateTreeView = "YES"	// Overide the generic project setting
	}

	baseDoxyFile     = "Doxyfile."
	root             = 'projects'
	disableIndex     = "YES"		// Ignored for the toc project
	generateTreeView = "YES"		// Ignored for the toc project
	outRoot          = 'output'

	parentfolders {
		source   = "source"
		html     = "html"
		latex    = "latex"
		image    = 'images'
		dia      = 'dia'
		examples = [ 'static-html', 'include' ]
	}

	journal {
		pages {
			monthly {
				required            = true
				format              = 'YYYY-MM'
				addLinkToNewDayPage = true
				linkPrefix          = '- '		// List of days.
				linkSuffix          = ' -- <!-- add summary here -->'
			}
			annual {
				required = false
				format   = ''
				addLinkToNewMonthPage = true
			}
			tweetTemplate =
				'<!-- \n> tweet -- [Tweet](url) via [\\@????](https://twitter.com/@????)\n>\n -->'
		}

		folders {
			annually = true
			monthly	 = true
			daily    = false
		}
		format {
			annual      = 'YYYY'
			month       = 'YYYY-MM'
			day         = 'dd'
			shorter     = 'dd-MMM-yyyy'
			fileName    = 'yyyy-MM-dd'
			longer      = 'EEEE d?? MMMM, yyyy'
			anchorDay   = 'yyyyMMdd'
			anchorMonth = 'yyyyMM'
			longerMonth = "MMMM, YYYY"
		}
	}
}

doxygen {
	verbose        = true
	configFileType = 'cfg'
	folder         = 'c:/portable/Doxygen-1.8.13'
	path           = "$folder/doxygen"

	ant {
		classPath = "$folder/ant_doxygen.jar"
		className = "org.doxygen.tools.DoxygenTask"
	}
}



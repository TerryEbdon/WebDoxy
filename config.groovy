/**
@file
@author Terry Ebdon
@brief This file configures the build process.
*/

verboseCleanUp    = false
defaultProjects   = ['@Work-in-Progress']
markdown.fileType = '.md'
datePattern       =  'yyyy-MM-dd'
backup {
	excludesFile 	= 'backupExcludes.txt'
	copyRoot			= 'b:/'
	copyFolderRoot= "${copyRoot}Backups/projects/"
}

project {
	author {
		name = 'unknown author'
		page = '@ref AboutMe'
	}
	toc {
		name             = 'TableOfContents'
		brief            = 'Directory of known, documented, projects that have been built.'

		disableIndex     = "NO"		// Override the generic project setting.
		generateTreeView = "YES"	// Override the generic project setting
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

	page {
		dateFormat = 'dd-MMM-yyyy'
		stub {
			name = 'stub'
			footer = '\\mycopyFooter'
		}
	}

	journal {
		pages {
			useHtmlDateSuffix = true
			daily {
				htmlIncludes = [] //['DailyScript_1', 'DailyScript_2']
			}

			monthly {
				htmlIncludes        = []      // [ 'ReverseMonthList.html' ]
				required            = true
				format              = 'YYYY-MM'
				addLinkToNewDayPage = true
				linkPrefix          = '- '		// List of days.
				linkSuffix          = ' -- <!-- add summary here -->'
			}
      quarterly {
        required              = true
        addLinkToNewWeekPage  = true
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
			annually  = true
			quarterly = true
			monthly	  = true
			daily     = false
		}
		format {
			annual        = 'YYYY'
			quarter       = 'YYYY-QQQ'
			month         = 'YYYY-MM'
			day           = 'dd'
			shorter       = 'dd-MMM-yyyy'
			fileName      = 'yyyy-MM-dd'
			weekFileName  = "yyyy-'w'ww" // 2020-w18.md
			longer        = 'EEEE d?? MMMM, yyyy'
			anchorDay     = 'yyyyMMdd'
			anchorMonth   = 'yyyyMM'
			longerMonth   = "MMMM, YYYY"
		}
	}
}

doxygen {
	verbose        = true
	configFileType = 'cfg'
	folder         = 'c:/portable/Doxygen-1.8.18'
	//~ folder         = 'c:/portable/Doxygen' // 1.8.11
	path           = "$folder/doxygen"

	ant {
		classPath = "$folder/ant_doxygen.jar"
		className = "org.doxygen.tools.DoxygenTask"
	}
}

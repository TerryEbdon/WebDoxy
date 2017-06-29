/**
\file
\author Terry Ebdon
\brief This file configures the build process.
*/

verboseCleanUp    = false
defaultProjects   = ['@Work-in-Progress']
markdown.fileType = '.md'

backup {
	excludesFile = "backupExcludes.txt"
}

project {
	toc {
		name  = 'TableOfContents'
		brief = 'Directory of known, documented, projects that have been built.'
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
				linkPrefix          = ' - '
				linkSuffix          = ' -- '
			}
			annual {
				required = false
				format = ''
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
			longer      = 'EEEE dd?? MMMM, yyyy'
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

baseSections = "live wibble"
sites {
	build {
		enabled_sections = 'build'
	}
	live {
		ENABLED_SECTIONS = baseSections
		//RECURSIVE = 'NO'
	}

	draft {
		ENABLED_SECTIONS = "$baseSections test wip draft staged"
	}

	blog {
		input = 'blog'
		ENABLED_SECTIONS = "$baseSections blog"
	}
}

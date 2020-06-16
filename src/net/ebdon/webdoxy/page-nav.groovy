new AntBuilder().with {
    def scanner = fileScanner {
        fileset(
            dir: 'C:/@drives/SD-CARD/test/web-doxy/projects/DevDiary - saved/source'
        ) {
            filename( regex: '[0-9]{4}-[0-9]{2}-[0-9]{2}\\.md' )
        }
    }

    int idx = scanner.findIndexOf{
//        it.name=='2017-06-20.md'
//        it.name=='2020-02-26.md'
        it.name=='2017-06-30.md'
    }

    def fileColl = scanner.collect()
    println "$idx, ${fileColl.size()}"

    if ( idx ) { // Not the first page
        println "prev page: wip${fileColl[idx-1].name[0..-4].replaceAll('-','')}"
    } else {
        println "$idx ??"
    }

    if ( idx < fileColl.size() - 1 ) { // Not the last page
        println "next page: wip${fileColl[idx+1].name[0..-4].replaceAll('-','')}"
    } else {
        println "$idx ??"
    }
}

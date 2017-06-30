package net.ebdon.webdoxy
def f = new File('.')
println "File: ${f.absolutePath}"
//def rb = ResourceBundle.getBundle( "resources.Language" )
def rb = ResourceBundle.getBundle( "Language" )
def msg = rb.getString( 'hello')
println msg

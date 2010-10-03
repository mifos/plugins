import org.mifos.spi.ParseResultDto

println "Groovy Plugin START."

// TODO: see old AudiBankTsv plugin (deleted in c637ae92bf7762)
println "input: " + input
println "first line: " + new BufferedReader(input).readLine()
println "parent: " + parent
println "parent.getDisplayName(): " + parent.displayName
println "parent.getUserReferenceDto(): " + parent.userReferenceDto
parseResultDto = new ParseResultDto([], [])

println "Groovy Plugin END."

import org.mifos.spi.ParseResultDto

println "Groovy Plugin START."

println "input: " + input
println "parent: " + parent
println "parent.getDisplayName(): " + parent.displayName
println "parent.getUserReferenceDto(): " + parent.userReferenceDto
parseResultDto = new ParseResultDto([], [])

println "Groovy Plugin END."

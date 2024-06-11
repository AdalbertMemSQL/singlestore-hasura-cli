package com.singlestore.hasura.cli

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import picocli.CommandLine
import picocli.CommandLine.*
import java.io.File
import kotlin.system.exitProcess


@Command(
    name = "SingleStore NDC CLI",
    subcommands = [HelpCommand::class],
    description = ["SingleStore Hasura V3 Connector Config CLI"]
)
class CLI {
    private val mapper = jacksonObjectMapper()

    // Example:
    // update jdbc:singlestore://localhost:3306/test?user=root&password=myPassword --databases test,db
    @Command(
        name = "update",
        description = ["Introspect SingleStore instance and emit updated information"],
        sortSynopsis = false,
        sortOptions = false
    )
    fun update(
        @Parameters(
            arity = "1",
            paramLabel = "<jdbcUrl>",
            description = ["JDBC URL to connect to the SingleStore instance"]
        )
        jdbcUrl: String,
        @Option(
            names = ["-o", "--outfile"],
            defaultValue = "configuration.json",
            description = ["The name of the output file to write the schema information to, defaults to configuration.json"]
        )
        outfile: String,
        @Option(
            names = ["-d", "--databases"],
            description = ["Comma-separated list of databases to introspect"]
        )
        databases: String?,
    ) {

        val configGenerator = SingleStoreConfigGenerator

        val config = configGenerator.getConfig(
            jdbcUrl = jdbcUrl,
            databases = databases?.split(",") ?: emptyList()
        )

        mapper.writerWithDefaultPrettyPrinter().writeValue(File(outfile), config)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cli = CommandLine(CLI())
            val exitCode = cli.execute(*args)
            exitProcess(exitCode)
        }
    }
}

fun main(args: Array<String>) {
    val cli = CommandLine(CLI())
    val exitCode = cli.execute(*args)
    exitProcess(exitCode)
}

package com.singlestore.hasura.cli

data class ConnectorConfiguration(
    val jdbcUrl: String,
    val tables: List<TableSchemaRow> = emptyList(),
)

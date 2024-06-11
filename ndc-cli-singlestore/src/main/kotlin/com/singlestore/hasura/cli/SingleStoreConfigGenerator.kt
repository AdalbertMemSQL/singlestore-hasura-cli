package com.singlestore.hasura.cli

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hasura.ndc.common.ColumnSchemaRow
import io.hasura.ndc.common.ConnectorConfiguration
import io.hasura.ndc.common.TableSchemaRow
import io.hasura.ndc.common.TableType
import java.sql.DriverManager

object SingleStoreConfigGenerator {
    private val mapper = jacksonObjectMapper()

    private fun escapeStringLiteral(str: String): String {
        return "'" + str.replace("'", "\\'").replace("\\", "\\\\") + "'";
    }

    fun getConfig(
        jdbcUrl: String,
        databases: List<String>
    ): ConnectorConfiguration {
        val sql = """
            SELECT
                CONCAT('`', REPLACE(tables.TABLE_SCHEMA, '`', '``'), '`', 
                    '.', 
                    '`', REPLACE(tables.TABLE_NAME, '`', '``'), '`') AS TABLE_NAME,
                tables.TABLE_TYPE,
                tables.table_COMMENT as DESCRIPTION,
                cols.COLUMNS,
                pk.PKS
            FROM (
                SELECT *
                FROM INFORMATION_SCHEMA.TABLES tables
                WHERE TABLE_TYPE IN ('BASE TABLE', 'VIEW')
                ${if (databases.isEmpty()) 
                    "AND tables.TABLE_SCHEMA NOT IN ('cluster', 'information_schema', 'memsql')" else 
                    "AND tables.TABLE_SCHEMA IN (${databases.joinToString { escapeStringLiteral(it) }})"}
            ) tables
            LEFT OUTER JOIN (
                SELECT
                    columns.TABLE_SCHEMA,
                    columns.TABLE_NAME,
                    JSON_AGG(JSON_BUILD_OBJECT(
                        'name', columns.column_name,
                        'description', columns.column_comment,
                        'type', columns.data_type,
                        'numeric_scale', columns.numeric_scale,
                        'nullable', if (columns.is_nullable = 'yes', true, false),
                        'auto_increment', if(columns.extra = 'auto_increment', true, false),
                        'is_primarykey', if(columns.COLUMN_KEY = 'PRI', true, false)
                    )) as COLUMNS
                FROM INFORMATION_SCHEMA.COLUMNS columns
                GROUP BY columns.TABLE_SCHEMA, columns.TABLE_NAME
            ) AS cols ON cols.TABLE_SCHEMA = tables.TABLE_SCHEMA AND cols.TABLE_NAME = tables.TABLE_NAME
            LEFT OUTER JOIN (
                SELECT
                    statistics.TABLE_SCHEMA,
                    statistics.TABLE_NAME,
                    JSON_AGG(statistics.COLUMN_NAME) as PKS
                FROM INFORMATION_SCHEMA.STATISTICS statistics
                WHERE statistics.INDEX_NAME = 'PRIMARY'
                GROUP BY statistics.TABLE_SCHEMA, statistics.TABLE_NAME
                ORDER BY statistics.SEQ_IN_INDEX
            ) AS pk ON pk.TABLE_SCHEMA = tables.TABLE_SCHEMA AND pk.TABLE_NAME = tables.TABLE_NAME
        """.trimIndent()

        val tables: MutableList<TableSchemaRow> = mutableListOf()
        DriverManager.getConnection(jdbcUrl).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { res ->
                    while (res.next()) {
                        tables.add(TableSchemaRow(
                            tableName = res.getString("TABLE_NAME"),
                            tableType = when (val tableType = res.getString("TABLE_TYPE")) {
                                "BASE TABLE" -> TableType.TABLE
                                "VIEW" -> TableType.VIEW
                                else -> throw Exception("Unknown table type: $tableType")
                            },
                            description = res.getString("DESCRIPTION"),
                            columns = res.getString("COLUMNS").let { mapper.readValue<List<ColumnSchemaRow>>(it) },
                            pks = res.getString("PKS").let { mapper.readValue<List<String>?>(it) },
                            fks = null
                        ))
                    }
                }
            }
        }

        return ConnectorConfiguration(
            jdbcUrl = jdbcUrl,
            jdbcProperties = emptyMap(),
            tables = tables,
            functions = emptyList()
        )
    }
}

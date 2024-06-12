package com.singlestore.hasura.cli

data class TableSchemaRow(
    val tableName: String,
    val tableType: TableType,
    val description: String?,
    val columns: List<ColumnSchemaRow>,
    val pks: List<String>?,
)

data class ColumnSchemaRow(
    val name: String,
    val description: String?,
    val type: String,
    val numeric_scale: Int?,
    val nullable: Boolean,
    val auto_increment: Boolean,
    val is_primarykey: Boolean?
)

enum class TableType {
    TABLE,
    VIEW
}

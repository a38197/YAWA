package pt.isel.pdm.yawa.dao.contentprovider.sql

import pt.isel.pdm.yawa.dao.contentprovider.dao.AColumns
import pt.isel.pdm.yawa.dao.contentprovider.dao.DbColumn
import pt.isel.pdm.yawa.dao.contentprovider.dao.DbTable
import pt.isel.pdm.yawa.dao.contentprovider.dao.DbType

private const val CREATE_TABLE = " create table "
private const val WHERE = " where "

fun createTable(table: DbTable):String{
    val colString = createColumns(table.columns)
    return "$CREATE_TABLE ${table.name} ($colString)"
}

const val COMMA = " , "

private fun createColumns(columns: AColumns):String{
    return columnsString(columns, ::createColumn)
}

private fun columnsString(columns: AColumns, map:(DbColumn)->String): String {
    val builder = StringBuilder()
    var comma = ""
    for (c in columns.getColumns()){
        builder.append(comma)
        builder.append(map(c))
        comma = COMMA

    }
    return builder.toString()
}

const val PRIMARY_KEY = " PRIMARY KEY "
private fun createColumn(col: DbColumn):String{
    val key = if(col.key) PRIMARY_KEY else ""
    return " ${col.name} ${getSqlType(col.type)} $key "
}

private fun getSqlType(type: DbType):String{
    return when(type){
        DbType.Text -> "TEXT"
        DbType.Integer -> "INTEGER"
    }
}

private const val DROP_TABLE = " drop table "
private const val IF_EXISTS = " if exists "
fun dropTable(table: DbTable) : String{
    return " $DROP_TABLE $IF_EXISTS ${table.name} "
}

fun getSelection(columns: AColumns):String{
    return columnsString(columns, DbColumn::name)
}

private const val CONST_START = " ( "
private const val CONST_END = " ) "
fun getConstraintSelection(const:MultipleConstraint):String{
    val builder = StringBuilder().append(CONST_START)
    var oper = ""
    for (c in const.const){
        builder.append(oper)
                .append(c.getSelectionString())
        oper = const.oper.sql
    }
    return builder.append(CONST_END).toString()
}

fun getConstraintSelection(const:ValueConstraint):String{
    return " ${const.column.name} ${const.const.sql} ? "
}



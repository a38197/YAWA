package pt.isel.pdm.yawa.dao.contentprovider.sql

import pt.isel.pdm.yawa.dao.contentprovider.dao.DbColumn
import java.util.*

/**
 * Created by nuno on 11/12/16.
 */

interface IConstraint{
    fun getSelectionString():String
    fun getSectionArgs():Array<out String>
}

class MultipleConstraint(val oper:ConstraintOperation, val const:Array<out IConstraint>) : IConstraint{

    override fun getSelectionString():String = getConstraintSelection(this)
    override fun getSectionArgs(): Array<out String> {
        val list = LinkedList<String>()
        for(c in const){
            for(arg in c.getSectionArgs()){
                list.add(arg)
            }
        }
        return list.toArray(Array(list.size,{""}))
    }
}

enum class ConstraintOperation(val sql:String) {
    And(" and "), Or(" or ");
}

class ValueConstraint(val column: DbColumn, val values:Any, val const:ConstraintValue) : IConstraint{
    override fun getSelectionString():String = getConstraintSelection(this)
    override fun getSectionArgs(): Array<out String> = arrayOf(const.value(values))
}

enum class ConstraintValue(val sql:String, val value:(Any)->String){
    Equals(" = ", Any::toString), Greater(" > ", Any::toString), GreaterOrEquals(" >= ", Any::toString),
    Lesser(" < ", Any::toString), LesserOrEquals(" <= ", Any::toString), Different(" <> ", Any::toString),
    Like(" like", {"$it%"});
}

object ConstraintBuilder{
    fun value(col: DbColumn, value:Any, constraint:ConstraintValue) = ValueConstraint(col,value,constraint)
    fun constraint(operation:ConstraintOperation, const:Array<out IConstraint>) = MultipleConstraint(operation,const)
}
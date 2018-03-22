package pt.isel.pdm.yawa.model.exception

open class YawaException : Exception{

    constructor() : super()
    constructor(msg: String) : super(msg)
    constructor(msg: String, error: Throwable) : super(msg, error)
}

class NoCitiesFoundException(msg: String) : YawaException(msg) {

}
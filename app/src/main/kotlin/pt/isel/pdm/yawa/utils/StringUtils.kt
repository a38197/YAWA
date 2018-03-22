package pt.isel.pdm.yawa.utils

class StringUtils {

    companion object{
        fun replaceSpaces(str: String): String {
            var newStr = ""

            for (char in str) {
                if (char.equals(' ')) {
                    newStr += "%20"
                } else
                    newStr += char
            }

            return newStr
        }
    }
}
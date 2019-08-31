package util


// val str1: String? = null
// val str2 = "   "
fun isNullOrEmpty(str: String?): Boolean {
    if (str != null && str.trim().isNotEmpty())
        return false
    return true
}


// val str1: String? = null
// val str2 = ""
fun isNullOrEmpty2(str: String?): Boolean {
    if (str != null && str.isNotEmpty())
        return false
    return true
}
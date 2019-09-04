package util

import org.apache.commons.validator.routines.UrlValidator
import org.json.XML
import scraper.CHROME_69_MOBILE_BROWSER
import java.awt.image.BufferedImage
import java.net.URL
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.imageio.ImageIO


//checks if a string is null or empty; however "" as well as "   " is considered empty
// val str1: String? = null
// val str2 = "   "
fun isNullOrEmpty(str: String?): Boolean {
    if (str != null && str.trim().isNotEmpty())
        return false
    return true
}

//checks if a string is null or empty; however only "" is considered empty
// val str1: String? = null
// val str2 = ""
fun isNullOrEmpty2(str: String?): Boolean {
    if (str != null && str.isNotEmpty())
        return false
    return true
}

//Removes duplicates from a List (containing strings) by using Java 8 Lambdas
//returns a List of String without duplicates
fun removeDuplicatesFromStringList(listWithDuplicates:List<String>): MutableList<String> {
    return listWithDuplicates.stream().distinct().collect(Collectors.toList())
}

//checks if url is valid
fun isUrlValid(url:String):Boolean {
    // Get an UrlValidator using default schemes
    val defaultValidator = UrlValidator()
    return defaultValidator.isValid(url)
}

//checks if the extension of the image's url is valid | .gif or .bmp are invalid extensions
fun isImageValid(imageUrl:String, invalidImageFileExtensionPattern:String): Boolean {
    val pattern = Pattern.compile(invalidImageFileExtensionPattern)
    val matcher = pattern.matcher(imageUrl)
    return !matcher.matches()
}

//checks if an image is of a certain valid minimum height or width
fun isImageOfValidSize(imageUrl:String,minimumImageHeight:Int, minimumImageWidth:Int): Boolean {

    val connection = URL(imageUrl).openConnection()
    connection.setRequestProperty(
        "User-Agent",
        CHROME_69_MOBILE_BROWSER
    )
    connection.connect()

    val image: BufferedImage? = try { ImageIO.read(connection.getInputStream()) } catch (e: Exception) { null }
    var imageH = image?.height ?: -1
    var imageW = image?.width ?: -1

    println("The image is  ${imageH}px high and ${imageW}px wide ")

    return !(imageH < minimumImageHeight || imageW < minimumImageWidth)

}

//returns a valid json object
fun formatJsonToValidJSonObject(json:String): String {
    // trims out any string before the first '{' and last '}'
    return json.substring(json.indexOf('{'), json.lastIndexOf('}') + 1)
}

//converts a json to xml
fun convertJSONtoXML(json:String ): String {

    val formatedJson = formatJsonToValidJSonObject(json)
    // LOGGER.info("This is the formated json output : " + formatedJson)
    val jsonFileObject = org.json.JSONObject(formatedJson)
    val xml = XML.toString(jsonFileObject)
    //  LOGGER.info("This is the xml output : " + xml)
    return xml

}
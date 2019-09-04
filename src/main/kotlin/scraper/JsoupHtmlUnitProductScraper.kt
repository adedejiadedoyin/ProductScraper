package scraper

import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import model.Product
import org.apache.commons.validator.routines.UrlValidator
import org.json.JSONObject
import org.json.XML
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import util.isNullOrEmpty
import java.awt.image.BufferedImage
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.imageio.ImageIO


// this class uses HtmlUnit to get the html page as a string
// while Jsoup is used to traverse the documents in order to get the necessary data
class JsoupHtmlUnitProductScraper (var url:String) {


    private val LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    private lateinit var product: Product
    private var htmlDocument: Document

    init {
        val webClient = WebClient(BrowserVersion.CHROME)
        webClient.options.setUseInsecureSSL(true)

        // continues even when there was an error executing javascripts on the html page
        webClient.options.isThrowExceptionOnScriptError = false;

        webClient.options.isThrowExceptionOnFailingStatusCode = false;

        var htmlString  = webClient.getPage<HtmlPage>(url).asXml()

        //LOGGER.info("This is the html page : " + htmlString)
        htmlDocument = Jsoup.parse(htmlString)
        LOGGER.level = Level.INFO
    }

    fun getProduct(): Product {
        product = Product()
        product.name = fetchTitle()
        // product.price = fetchPrice()
        //  product.description = fetchDescription()
        //  product.image = fetchImages()
        product.url = fetchUrl()
        fetchDataFromJsonLdSchema()
        fetchValidImagesFromPage()
        return product
    }


    /*
  ------------------------  PUBLIC METHODS -------------------------------------------------------------
   */

    //Fetches Title of item/product
    fun fetchTitle(): String {

        var title: String = ""
        var condition = true
        var position = 1
        while (condition) {

            when (position) {
                1 -> {
                    title = fetchTitleFromMetaTitle()
                    if (isNullOrEmpty(title)) // if it can't be found , increment position in order to move to the next algorithm
                        position++
                    else
                        condition = false
                }

                2 -> {
                    title = fetchTitleFromMetaFacebookOG()
                    if (isNullOrEmpty(title))
                        position++
                    else
                        condition = false
                }

                3 -> {
                    title = fetchTitleFromMetaTwitter()
                    if (isNullOrEmpty(title))
                        position++
                    else
                        condition = false
                }

                4 -> {
                    title = fetchTitleFromTitleTag()
                    if (isNullOrEmpty(title))
                        position++
                    else
                        condition = false
                }

                else -> {
                    condition = false
                }

            } // end when
        } // end while loop
        return title
    }


    //Fetches Url of item/product
    fun fetchUrl(): String {
        return url
    }

    //Fetches image of product/item from facebook og & twitter
    fun fetchImage(): String {

        var imageUrl: String = ""
        var condition = true
        var position = 1
        while (condition) {

            when (position) {
                1 -> {
                    imageUrl = fetchImageFromMetaFacebookOG()
                    if (isNullOrEmpty(imageUrl)) // if it can't be found , increment position in order to move to the next algorithm
                        position++
                    else
                        condition = false
                }

                2 -> {
                    imageUrl = fetchImageFromMetaTwitter()
                    if (isNullOrEmpty(imageUrl))
                        position++
                    else
                        condition = false
                }

                else -> {
                    condition = false
                }

            } // end when
        } // end while loop
        return imageUrl
    }

    // fetches all valid image url from <img> tags found in the html page, scraping out all gifs, small size pictures
    fun fetchValidImagesFromPage(): MutableList<String> {
        val imageUrlList = fetchImagesFromPage()
        val validImageUrlList = mutableListOf<String>()
        var validImageUrlListWithoutDuplicates: MutableList<String>
        for (imageUrl in imageUrlList) {

            if (isImageValid(imageUrl) && isUrlValid(imageUrl)){ // checks if the imageUrl is valid ie it is not a GIF or BMP
                validImageUrlList.add(imageUrl)
                println("This is a valid image : " + imageUrl)
            }

        }

        validImageUrlListWithoutDuplicates = removeDuplicatesFromList(validImageUrlList)

        for (imageUrl in validImageUrlListWithoutDuplicates) {
            println("Without duplicates: This is a valid image : " + imageUrl)
            isImageOfValidSize(imageUrl)
        }


        println("There are ${imageUrlList.size} total images extracted from this url")
        println("There are ${validImageUrlList.size} valid images ")
        println("There are ${validImageUrlListWithoutDuplicates.size} valid images after removing duplicates ")
        return validImageUrlListWithoutDuplicates
    }





    /*
    ------------------------  PRIVATE METHODS -------------------------------------------------------------
     */


    private fun fetchTitleFromMetaTitle(): String {
        val title = htmlDocument.select(META_TITLE_SELECTOR).attr("content")
        LOGGER.info( "Product Title from MetaTitle : " + title)
        return title
    }

    private fun fetchTitleFromMetaFacebookOG(): String {
        val title = htmlDocument.select(OG_TITLE_SELECTOR).attr("content")
        LOGGER.info("Product Title from MetaFacebookOG : " + title)
        return title
    }

    private fun fetchTitleFromMetaTwitter(): String {
        val title = htmlDocument.select(TWITTER_TITLE_SELECTOR).attr("content")
        LOGGER.info("Product Title from MetaTwitter : " + title)
        return title
    }

    private fun fetchTitleFromTitleTag(): String {
        val title = htmlDocument.title()// htmlDocument.select("title").get(0).text()
        LOGGER.info("Product Title from TitleTag : " + title)
        return title
    }

    private fun fetchImageFromMetaFacebookOG(): String {
        val image = htmlDocument.select(OG_IMAGE_SELECTOR).attr("content")
        LOGGER.info("Product Image from MetaFacebookOG : " + image)
        return image
    }

    private fun fetchImageFromMetaTwitter(): String {
        val image = htmlDocument.select(TWITTER_IMAGE_SELECTOR).attr("content")
        LOGGER.info("Product Image from MetaTwitter : " + image)
        return image
    }

    // fetch all links to images
    private fun fetchImagesFromPage(): MutableList<String> {
        val images = htmlDocument.select("img")
        val imageUrlList = mutableListOf<String>()
        for (image in images) {

            if (image.hasAttr("data-src")){
                println("This is a data-src image : " + image.attr("abs:data-src"))
                imageUrlList.add(image.attr("abs:data-src"))
            }
            //This gets the absolute url to the image using "abs:src" rather than just using "src"
            println("This is the absolute url of image : " + image.attr("abs:src"))
            imageUrlList.add(image.attr("abs:src"))

        }

        return imageUrlList
    }



    //Removes duplicates from a List using Java 8 Lambdas
    private fun removeDuplicatesFromList(listWithDuplicates:List<String>): MutableList<String> {
        return listWithDuplicates.stream().distinct().collect(Collectors.toList())
    }


    //checks if url is valid
    private fun isUrlValid(url:String):Boolean {
        // Get an UrlValidator using default schemes
        val defaultValidator = UrlValidator()
        return defaultValidator.isValid(url)
    }


    //checks if the extension of the image's url is valid | .gif or .bmp are invalid extensions
    private fun isImageValid(imageUrl:String): Boolean {
        val pattern = Pattern.compile(INVALID_IMAGE_FILE_EXTENSION_PATTERN)
        val matcher = pattern.matcher(imageUrl)
        return !matcher.matches()
    }

    private fun isImageOfValidSize(imageUrl:String): Boolean {

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

        return !(imageH < MINIMUM_IMAGE_HEIGHT || imageW < MINIMUM_IMAGE_WIDTH)

    }

    private fun fetchtMicrodataSchema(): Elements {

        val element = htmlDocument.getElementsByAttributeValueContaining("itemtype","http://schema.org/")
        return element

    }

    private fun fetchRDFaSchema(): Elements {

        val element = htmlDocument.getElementsByAttributeValueMatching("vocab","http://schema.org/")
        return element

    }

    // returns a list of all JsonLd files from this html page
    private fun fetchJsonLd(): MutableList<String> {
        val elements = htmlDocument.select(JSONLD_SELECTOR) //htmlDocument.getElementsByTag("script").attr("type", "application/ld+json")
        val jsonList = mutableListOf<String>()

        for (jsonld in elements) {
            jsonList.add(jsonld.data())
            // LOGGER.info("This is the raw json output : " + jsonld.data())
        }

        return jsonList

    }

    // returns a list of acceptable JsonLd files from this html page; the acceptable ones are of Product, ItemPage
    private fun fetchFilteredJsonLd(): MutableList<String> {
        val elements = htmlDocument.select(JSONLD_SELECTOR) //htmlDocument.getElementsByTag("script").attr("type", "application/ld+json")
        val jsonList = mutableListOf<String>()

        for (jsonldElement in elements) {

            if (getSchemaEntityTypeFromJsonLd(jsonldElement.data())  == PRODUCT_SCHEMA_TYPE || getSchemaEntityTypeFromJsonLd(jsonldElement.data()) ==ITEMPAGE_SCHEMA_TYPE){
                jsonList.add(jsonldElement.data())
                // LOGGER.info("This is the raw json output : " + jsonldElement.data())
            }

        }

        return jsonList

    }

    // returns the @type of a Schema eg Product,ItemPage etc
    private fun getSchemaEntityTypeFromJsonLd(jsonObject:String):String{
        val formattedJson = formatJsonToValidJSonObject(jsonObject)
        val jsonResult = JSONObject(formattedJson)
        // LOGGER.info("The Schema type for this json is : " + jsonResult.get("@type"))
        return jsonResult.get("@type") as String

    }

    private fun fetchDataFromJsonLdSchema(){

        for (jsonld in fetchFilteredJsonLd()) {
            processJsonLd(jsonld)
        }
    }


    private fun processJsonLd(json:String){
        val xml = convertJSONtoXML(json)
        var imageUrl:String = extractFromXMLByCssQuery(xml,"image").text()

        if (isNullOrEmpty(imageUrl))
            imageUrl= extractFromXMLByCssQuery(xml,"image > contentUrl").text()


        LOGGER.info("The imageUrl of this product is  : " + imageUrl)

    }

    private fun formatJsonToValidJSonObject(json:String): String {
        // trims out any string before the first '{' and last '}'
        return json.substring(json.indexOf('{'), json.lastIndexOf('}') + 1)
    }

    private fun convertJSONtoXML(json:String ): String {

        val formatedJson = formatJsonToValidJSonObject(json)
        // LOGGER.info("This is the formated json output : " + formatedJson)
        val jsonFileObject = org.json.JSONObject(formatedJson)
        val xml = XML.toString(jsonFileObject)
        //  LOGGER.info("This is the xml output : " + xml)
        return xml

    }

    private fun extractFromXMLByCssQuery(xml:String,cssQuery:String):Elements{
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        return doc.select(cssQuery)
    }

}
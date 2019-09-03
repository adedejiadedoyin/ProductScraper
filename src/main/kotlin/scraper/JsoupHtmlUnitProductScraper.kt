package scraper

import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import model.Product
import org.apache.commons.io.FileUtils
import org.apache.commons.validator.routines.UrlValidator
import org.json.JSONObject
import org.json.XML
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import util.isNullOrEmpty
import java.io.File
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern
import java.util.stream.Collectors


// this class uses HtmlUnit to process the url and get the html page as a string
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
        fetchDataFromJsonLdSchema(htmlDocument)
        fetchValidImagesFromPage(htmlDocument)
        return product
    }


    /*
    FETCH TITLE OF PRODUCT/ITEM
     */
    private fun fetchTitle(): String {

        var title: String = ""
        var condition = true
        var position = 1
        while (condition) {

            when (position) {
                1 -> {
                    title = fetchTitleFromMetaTitle(htmlDocument)
                    if (isNullOrEmpty(title)) // if it can't be found , increment position in order to move to the next algorithm
                        position++
                    else
                        condition = false
                }

                2 -> {
                    title = fetchTitleFromMetaFacebookOG(htmlDocument)
                    if (isNullOrEmpty(title))
                        position++
                    else
                        condition = false
                }

                3 -> {
                    title = fetchTitleFromMetaTwitter(htmlDocument)
                    if (isNullOrEmpty(title))
                        position++
                    else
                        condition = false
                }

                4 -> {
                    title = fetchTitleFromTitleTag(htmlDocument)
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

    private fun fetchTitleFromMetaTitle(htmlDocument:Document): String {
        val title = htmlDocument.select("meta[name=title]").attr("content")
        LOGGER.info( "Product Title from MetaTitle : " + title)
        return title
    }

    private fun fetchTitleFromMetaFacebookOG(htmlDocument:Document): String {
        val title = htmlDocument.select("meta[property=og:title]").attr("content")
        LOGGER.info("Product Title from MetaFacebookOG : " + title)
        return title
    }

    private fun fetchTitleFromMetaTwitter(htmlDocument:Document): String {
        val title = htmlDocument.select("meta[name=twitter:title]").attr("content")
        LOGGER.info("Product Title from MetaTwitter : " + title)
        return title
    }

    private fun fetchTitleFromTitleTag(htmlDocument:Document): String {
        val title = htmlDocument.title()// htmlDocument.select("title").get(0).text()
        LOGGER.info("Product Title from TitleTag : " + title)
        return title
    }


    /*
        FETCH URL OF PRODUCT/ITEM
         */
    private fun fetchUrl(): String {
        return url
    }


    /*
       FETCH IMAGE OF PRODUCT/ITEM
        */
    private fun fetchImage(): String {

        var imageUrl: String = ""
        var condition = true
        var position = 1
        while (condition) {

            when (position) {
                1 -> {
                    imageUrl = fetchImageFromMetaFacebookOG(htmlDocument)
                    if (isNullOrEmpty(imageUrl)) // if it can't be found , increment position in order to move to the next algorithm
                        position++
                    else
                        condition = false
                }

                2 -> {
                    imageUrl = fetchImageFromMetaTwitter(htmlDocument)
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

    private fun fetchImageFromMetaFacebookOG(htmlDocument:Document): String {
        val image = htmlDocument.select("meta[property=og:image]").attr("content")
        LOGGER.info("Product Image from MetaFacebookOG : " + image)
        return image
    }

    private fun fetchImageFromMetaTwitter(htmlDocument:Document): String {
        val image = htmlDocument.select("meta[name=twitter:image]").attr("content")
        LOGGER.info("Product Image from MetaTwitter : " + image)
        return image
    }

    // fetch all links to images
    private fun fetchImagesFromPage(htmlDocument:Document): MutableList<String> {
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


    // fetch all valid links to images
    // scrape out all gifs, small size pictures
    private fun fetchValidImagesFromPage(htmlDocument:Document): MutableList<String> {
        val imageUrlList = fetchImagesFromPage(htmlDocument)
        val validImageUrlList = mutableListOf<String>()
        var validImageUrlListWithoutDuplicates: MutableList<String>
        for (imageUrl in imageUrlList) {

            if (isImageValid(imageUrl) && isUrlValid(imageUrl)){ // checks if the imageUrl is valid ie it is not a GIF or BMP
                validImageUrlList.add(imageUrl);
                println("This is a valid image : " + imageUrl)
            }

        }

        validImageUrlListWithoutDuplicates = removeDuplicatesFromList(validImageUrlList)

        for (imageUrl in validImageUrlListWithoutDuplicates) {
            println("Without duplicates: This is a valid image : " + imageUrl)
        }


        println("There are ${imageUrlList.size} total images extracted from this url")
        println("There are ${validImageUrlList.size} valid images ")
        println("There are ${validImageUrlListWithoutDuplicates.size} valid images after removing duplicates ")
        return validImageUrlListWithoutDuplicates
    }


    //Removes duplicates from a List using Java 8 Lambdas
    private fun removeDuplicatesFromList(listWithDuplicates:List<String>): MutableList<String> {
        return listWithDuplicates.stream().distinct().collect(Collectors.toList());
    }


    //checks if url is valid
    private fun isUrlValid(url:String):Boolean {
        // Get an UrlValidator using default schemes
        val defaultValidator = UrlValidator()
        return defaultValidator.isValid(url)
    }


    //checks if the extension of the image's url is valid | .gif or .bmp are invalid extensions
    private fun isImageValid(imageUrl:String): Boolean {
        val pattern = Pattern.compile(INVALID_IMAGE_PATTERN)
        val matcher = pattern.matcher(imageUrl);
        return !matcher.matches();
    }

    private fun isImageOfValidSize(imageUrl:String){
        val destinationOfDownloadedImage = File("image.jpg")
        FileUtils.copyURLToFile( URL(imageUrl),destinationOfDownloadedImage, CONNECT_TIMEOUT, READ_TIMEOUT);

    }

    public fun fetchtMicrodataSchema(htmlDocument:Document): Elements {

        val element = htmlDocument.getElementsByAttributeValueContaining("itemtype","http://schema.org/")
        return element

    }

    public fun fetchRDFaSchema(htmlDocument:Document): Elements {

        val element = htmlDocument.getElementsByAttributeValueMatching("vocab","http://schema.org/")
        return element

    }

    // returns a list of all JsonLd files from this html page
    public fun fetchJsonLd(htmlDocument:Document): MutableList<String> {
        val elements = htmlDocument.select("script[type=application/ld+json]") //htmlDocument.getElementsByTag("script").attr("type", "application/ld+json")
        val jsonList = mutableListOf<String>()

        for (jsonld in elements) {
            jsonList.add(jsonld.data())
            // LOGGER.info("This is the raw json output : " + jsonld.data())
        }

        return jsonList

    }

    // returns a list of acceptable JsonLd files from this html page; the acceptable ones are of Product, ItemPage
    public fun fetchFilteredJsonLd(htmlDocument:Document): MutableList<String> {
        val elements = htmlDocument.select("script[type=application/ld+json]") //htmlDocument.getElementsByTag("script").attr("type", "application/ld+json")
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

    private fun fetchDataFromJsonLdSchema(htmlDocument:Document){

        for (jsonld in fetchFilteredJsonLd(htmlDocument)) {
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
        val jsonFileObject = org.json.JSONObject(formatedJson) ;
        val xml = XML.toString(jsonFileObject)
        //  LOGGER.info("This is the xml output : " + xml)
        return xml

    }

    private fun extractFromXMLByCssQuery(xml:String,cssQuery:String):Elements{
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        return doc.select(cssQuery)
    }


}
package scraper

import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException
import com.gargoylesoftware.htmlunit.WebClient
import java.util.logging.Level.SEVERE
import java.io.IOException
import jdk.nashorn.internal.objects.NativeArray.forEach
import java.util.logging.Level
import java.util.logging.Logger


class HtmlUnitProductScraper {


    fun main() {

        HtmlUnitProductScraper().handleURlFromBrowserUsingHtmlUnit()
    }


    fun handleURlFromBrowserUsingHtmlUnit() {

        try {
            val webClient = WebClient(BrowserVersion.CHROME)
            webClient.getOptions().setUseInsecureSSL(true)
            // webClient.getOptions().setJavaScriptEnabled(true);
            // webClient.getOptions().setCssEnabled(true);
            val Url =
                "https://www.amazon.co.uk/gp/product/B01N28AN95?pf_rd_p=330fbd82-d4fe-42e5-9c16-d4b886747c64&pf_rd_r=1525W53PBPHBYY7HAGG2"
            val Url2 = "https://www.konga.com/product/scanfrost-scanfrost-gas-cooker-4-burners-grey-ck5400ng-4096530"
            val Url3 = "https://www.jumia.com.ng/senwei-4.5-kva-key-starter-generator-sv6200e2-30981680.html"
            val Url5 = "https://www.next.ie/en/g23360s37"

            // Fetching the HTML from a given URL
            val page = webClient.getPage(Url5)
            // webClient.waitForBackgroundJavaScript(10000);
            // webClient.waitForBackgroundJavaScriptStartingBefore(10000);


            // 1. Fetch the title from the website
            val title = page.getTitleText()
            println("this is the product's title: $title")


            System.out.println("this is the product's full URI: " + page.getBaseURI())
            System.out.println("this is the product's base URI: " + page.getUrl().getHost())


            // 2. Fetch the images from the website
            val images = page.getElementsByTagName("img")
            images.forEach { image -> System.out.println("image: " + image.getAttribute("src")) } //final Iterator<E> nodesIterator = inputs.iterator();


        } catch (ex: IOException) {
            Logger.getLogger(HtmlUnitProductScraper::class.java!!.getName()).log(Level.SEVERE, null, ex)
        } catch (ex: FailingHttpStatusCodeException) {
            Logger.getLogger(HtmlUnitProductScraper::class.java!!.getName()).log(Level.SEVERE, null, ex)
        }

    }

}
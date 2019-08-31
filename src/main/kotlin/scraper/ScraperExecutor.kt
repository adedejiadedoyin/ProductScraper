package scraper

fun main(){

    val url3= "https://www.amazon.co.uk/gp/product/B01N28AN95?pf_rd_p=330fbd82-d4fe-42e5-9c16-d4b886747c64&pf_rd_r=0EG7W3HNS2YHQ0XQFC4D"
    val url2= "https://www.konga.com/product/scanfrost-scanfrost-gas-cooker-4-burners-grey-ck5400ng-4096530"
    val url= "https://www.jumia.com.ng/senwei-4.5-kva-key-starter-generator-sv6200e2-30981680.html"
    val url4="https://www.columbiasportswear.ie/IE/p/mens-inner-limits-jacket-1714181.html"
    val url5="https://www.next.ie/en/g23360s37"
    val url6="https://www.asos.com/the-north-face/the-north-face-vault-backpack-28-litres-in-black/prd/10253008"

    JsoupProductScraper(url6).getProduct()
}
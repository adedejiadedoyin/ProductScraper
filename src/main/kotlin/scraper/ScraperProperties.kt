package scraper

//Properties for valid images
const val INVALID_IMAGE_PATTERN = "([^\\s]+(\\.(?i)(gif|bmp|png))$)"
const val VALID_IMAGE_HEIGHT = 200
const val VALID_IMAGE_WIDTH = 200

const val JSONLD_SELECTOR="script[type=application/ld+json]"

const val PRODUCT_SCHEMA_TYPE="Product"
const val ITEMPAGE_SCHEMA_TYPE="ItemPage"

//UserAgent browsers' String
const val CHROME_69_MOBILE_BROWSER = "Mozilla/5.0 (Linux; Android 6.0.1; SM-G532M Build/MMB29T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36"
const val CHROME_76_DESKTOP_BROWSER = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36"

//CSS Query Selectors for Jsoup
const val META_TITLE_SELECTOR = "meta[name=title]"
const val OG_TITLE_SELECTOR = "meta[property=og:title]"
const val TWITTER_TITLE_SELECTOR = "meta[name=twitter:title]"
const val META_IMAGE_SELECTOR = "meta[name=title]"
const val OG_IMAGE_SELECTOR = "meta[property=og:image]"
const val TWITTER_IMAGE_SELECTOR = "meta[name=twitter:image]"

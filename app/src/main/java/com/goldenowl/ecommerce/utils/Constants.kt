package com.goldenowl.ecommerce.utils

import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.Delivery

object Constants {
    const val MIN_AGE = 365
    const val AUTO_SCROLL: Long = 3500
    const val LOAD_MORE_QUANTITY: Long = 2
    const val ERR_NOT_LOGIN = "[Firestore] User not found!"

    const val KEY_ORDER = "KEY_ORDER"
    const val HISTORY_SIZE = 12

    const val KEY_NEW = "KEY_NEW"
    const val KEY_SALE = "KEY_SALE"
    const val KEY_SEARCH = "KEY_SEARCH"
    const val CHANNEL_CHANGE_PASSWORD_ID = "CHANNEL_CHANGE_PASSWORD_ID"
    const val UPLOAD_QUALITY: Int = 80
    const val UPLOAD_MAX_SIZE: Long = 1_048_576

    const val KEY_PRODUCT = "KEY_PRODUCT"
    const val KEY_CATEGORY = "KEY_CATEGORY"
    const val KEY_POSITION = "KEY_POSITION"
    const val KEY_ADDRESS = "KEY_ADDRESS"
    const val KEY_PRODUCT_ID = "KEY_PRODUCT_ID"

    const val PRODUCTS_COLLECTION = "products"
    const val USER_ORDER_COLLECTION = "user_orders"
    const val REVIEW_COLLECTION = "reviews"
    const val APP_DATA_COLLECTION = "appdata"
    const val PROMOTIONS_COLLECTION = "promotions"
    const val GRID_VIEW = 1
    const val LIST_VIEW = 0
    const val SPAN_COUNT_ONE = 1
    const val SPAN_COUNT_TWO = 2

    const val TYPE_MATER_CARD = 0
    const val TYPE_VISA = 1

    const val DEFAULT_CARD = "default_card"
    const val DEFAULT_ADDRESS = "default_address"

    val sortMap = mapOf(
        SortType.POPULAR to R.string.sort_by_popular,
        SortType.NEWEST to R.string.sort_by_newest,
        SortType.PRICE_INCREASE to R.string.sort_by_price_low_2_high,
        SortType.PRICE_DECREASE to R.string.sort_by_price_high_2_low,
        SortType.REVIEW to R.string.sort_by_customer_review
    )

    val colorMap = mapOf(
        "red" to R.color.red_dark,
        "black" to R.color.black_light,
        "grey" to R.color.grey_text,
    )

    val listSize = listOf("L", "M", "S", "XL", "XS")
    val listDelivery = listOf<Delivery>(
        Delivery("fedex", " 2-3 days", R.drawable.ic_fedex, 15),
        Delivery("usps", " 2-3 days", R.drawable.ic_usps, 10),
        Delivery("dhl", " 2-3 days", R.drawable.ic_dhl, 20),
    )
    val listCountry = listOf(
        "Afghanistan",
        "Albania",
        "Algeria",
        "Andorra",
        "Angola",
        "Antigua & Deps",
        "Argentina",
        "Armenia",
        "Australia",
        "Austria",
        "Azerbaijan",
        "Bahamas",
        "Bahrain",
        "Bangladesh",
        "Barbados",
        "Belarus",
        "Belgium",
        "Belize",
        "Benin",
        "Bhutan",
        "Bolivia",
        "Bosnia Herzegovina",
        "Botswana",
        "Brazil",
        "Brunei",
        "Bulgaria",
        "Burkina",
        "Burundi",
        "Cambodia",
        "Cameroon",
        "Canada",
        "Cape Verde",
        "Central African Rep",
        "Chad",
        "Chile",
        "China",
        "Colombia",
        "Comoros",
        "Congo",
        "Congo {Democratic Rep}",
        "Costa Rica",
        "Croatia",
        "Cuba",
        "Cyprus",
        "Czech Republic",
        "Denmark",
        "Djibouti",
        "Dominica",
        "Dominican Republic",
        "East Timor",
        "Ecuador",
        "Egypt",
        "El Salvador",
        "Equatorial Guinea",
        "Eritrea",
        "Estonia",
        "Ethiopia",
        "Fiji",
        "Finland",
        "France",
        "Gabon",
        "Gambia",
        "Georgia",
        "Germany",
        "Ghana",
        "Greece",
        "Grenada",
        "Guatemala",
        "Guinea",
        "Guinea-Bissau",
        "Guyana",
        "Haiti",
        "Honduras",
        "Hungary",
        "Iceland",
        "India",
        "Indonesia",
        "Iran",
        "Iraq",
        "Ireland {Republic}",
        "Israel",
        "Italy",
        "Ivory Coast",
        "Jamaica",
        "Japan",
        "Jordan",
        "Kazakhstan",
        "Kenya",
        "Kiribati",
        "Korea North",
        "Korea South",
        "Kosovo",
        "Kuwait",
        "Kyrgyzstan",
        "Laos",
        "Latvia",
        "Lebanon",
        "Lesotho",
        "Liberia",
        "Libya",
        "Liechtenstein",
        "Lithuania",
        "Luxembourg",
        "Macedonia",
        "Madagascar",
        "Malawi",
        "Malaysia",
        "Maldives",
        "Mali",
        "Malta",
        "Marshall Islands",
        "Mauritania",
        "Mauritius",
        "Metaverse Multiverse",
        "Mexico",
        "Micronesia",
        "Moldova",
        "Monaco",
        "Mongolia",
        "Montenegro",
        "Morocco",
        "Mozambique",
        "Myanmar, {Burma}",
        "Namibia",
        "Nauru",
        "Nepal",
        "Netherlands",
        "New Zealand",
        "Nicaragua",
        "Niger",
        "Nigeria",
        "Norway",
        "Oman",
        "Pakistan",
        "Palau",
        "Panama",
        "Papua New Guinea",
        "Paraguay",
        "Peru",
        "Philippines",
        "Poland",
        "Portugal",
        "Qatar",
        "Romania",
        "Russian Federation",
        "Rwanda",
        "St Kitts & Nevis",
        "St Lucia",
        "Saint Vincent & the Grenadines",
        "Samoa",
        "San Marino",
        "Sao Tome & Principe",
        "Saudi Arabia",
        "Senegal",
        "Serbia",
        "Seychelles",
        "Sierra Leone",
        "Singapore",
        "Slovakia",
        "Slovenia",
        "Solomon Islands",
        "Somalia",
        "South Africa",
        "South Sudan",
        "Spain",
        "Sri Lanka",
        "Sudan",
        "Suriname",
        "Swaziland",
        "Sweden",
        "Switzerland",
        "Syria",
        "Taiwan",
        "Tajikistan",
        "Tanzania",
        "Thailand",
        "Togo",
        "Tonga",
        "Trinidad & Tobago",
        "Tunisia",
        "Turkey",
        "Turkmenistan",
        "Tuvalu",
        "Uganda",
        "Ukraine",
        "United Arab Emirates",
        "United Kingdom",
        "United States",
        "Uruguay",
        "Uzbekistan",
        "Vanuatu",
        "Vatican City",
        "Venezuela",
        "Vietnam",
        "Yemen",
        "Zambia",
        "Zimbabwe"
    )

}
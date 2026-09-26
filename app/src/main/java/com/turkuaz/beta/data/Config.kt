package com.turkuaz.beta.data

/**
 * MVP: sabit sunucu adresi. 10.0.2.2, Android emulator'den host makinenin
 * localhost'una erisim icin ozel bir takma addir (fiziksel cihazda test
 * icin bilgisayarinizin LAN IP'sini yazin, ör. "http://192.168.1.20:8000").
 * TODO (production): bunu bir ayarlar ekranindan degistirilebilir yapin.
 */
object Config {
    var BASE_URL: String = "https://turkuaz-core2-production.up.railway.app"
}

package com.example.event_validator_android

val LOG_KEY                                                             = "event_validator.LOG"
val RGX_WEBSITE                                                         = "^(http://www.|https://www.|http://|https://)?[a-z0-9]+([-.]{1}[a-z0-9]+)*.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$"

// For Intents
// -- Correct QR
val INT_QRDATA                                                          = "com.example.event_validator.QRDATA"
val INT_QRSECRETS                                                       = "com.example.event_validator.QRSECRETS"

// -- Wrong QR
val INT_BARCODE                                                         = "com.example.event_validator.BARCODE"


// For UI
// -- Type of Cells
enum class cellType{
    SECTION, BASIC, LEFTDETAIL
}

// -- Date Format
val SDF_INITIAL_FORMAT                                                  = "yyyy/MM/dd"
val SDF_PRINTING_FORMAT_DEFAULT                                         = "EEEE, dd MMMM yyyy"
val SDF_PRINTING_FORMAT_JA                                              = "EEEE, MM月dd日yyyy年"
package com.example.event_validator_android

data class QRData constructor(
    var e                       :String                 = "",                        // eventName
    var d                       :String                 = "",                        // date
    var w                       :String                 = "",                        // website
    var v                       :Double                 = 0.0,
    var l                       :Int                    = 0,
    var g                       :Int                    = 0,
    var p                       :Int                    = 0,
    var y                       :String                 = "",
    var k                       :Int                    = 0,
    var h                       :String                 = ""
) {

}

data class QRSecrets constructor(
    var p                       :Int                    = 0,
    var g                       :Int                    = 0,
    var y                       :Int                    = 0,
    var k                       :Int                    = 0,
    var s                       :Int                    = 0
) {

}
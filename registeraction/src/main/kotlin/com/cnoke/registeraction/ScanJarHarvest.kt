package com.cnoke.registeraction

class ScanJarHarvest {
    var harvestList = ArrayList<Harvest>()
    class Harvest {
        var className = ""
        var interfaceName: String? = null
        var isInitClass = false
    }
}
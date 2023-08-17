package cn.crazykid.qqrobot.entity

class DivingFishMaimaiMusicData {
    var id: Long? = null
    var title: String? = null
    var type: String? = null
    var ds: List<Double?>? = null
    var level: List<String?>? = null
    var cids: List<Int?>? = null
    var charts: List<Chart?>? = null
    var basic_info: BasicInfo? = null
}

open class Chart {
    var notes: List<Int?>? = null
    var charter: String? = null
}

open class BasicInfo {
    var title: String? = null
    var artist: String? = null
    var genre: String? = null
    var bpm: Int? = null
    var release_date: String? = null
    var from: String? = null
    var is_new: Boolean? = null
}


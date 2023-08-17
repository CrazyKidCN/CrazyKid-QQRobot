package cn.crazykid.qqrobot.entity

/**
 * @author CrazyKid
 * @date 2023/7/27 10:48
 */

class DivingFishMaimaiPlayerData {
    var nickname: String? = null
    var rating: Int? = null
    var additional_rating: Int? = null
    var username: String? = null
    var charts: Charts? = null
}

class Charts {
    var dx: MutableList<MaimaiPlayScore>? = null
    var sd: MutableList<MaimaiPlayScore>? = null
}

open class MaimaiPlayScore {
    var achievements: Double? = null
    var ds: Double? = null
    var fc: String? = null
    var fs: String? = null
    var level: String? = null
    var level_index: Int? = null
    var level_label: String? = null
    var ra: Int? = null
    var rate: String? = null
    var song_id: Int? = null
    var title: String? = null
    var type: String? = null
}

fun List<MaimaiPlayScore>.fillEmpty(target: Int): List<MaimaiPlayScore> {
    val emptyMaimaiPlayRecord = MaimaiPlayScore()
    emptyMaimaiPlayRecord.achievements = 0.0
    emptyMaimaiPlayRecord.ds = 0.0
    emptyMaimaiPlayRecord.fc = ""
    emptyMaimaiPlayRecord.fs = ""
    emptyMaimaiPlayRecord.level = ""
    emptyMaimaiPlayRecord.level_index = 0
    emptyMaimaiPlayRecord.level_label = ""
    emptyMaimaiPlayRecord.ra = 0
    emptyMaimaiPlayRecord.rate = ""
    emptyMaimaiPlayRecord.song_id = -1
    emptyMaimaiPlayRecord.title = ""
    emptyMaimaiPlayRecord.type = ""

    val result = toMutableList()
    for (i in 1..(target - size)) {
        result.add(emptyMaimaiPlayRecord)
    }
    return result
}



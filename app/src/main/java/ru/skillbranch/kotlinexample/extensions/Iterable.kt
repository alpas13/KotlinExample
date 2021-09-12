package ru.skillbranch.kotlinexample.extensions

fun List<String>.dropLastUntil(conditions: String): List<String> {
    var counter = 0
    var notMatchConditions = true
    val tempList = mutableListOf<String>()

    while (counter < this.size && notMatchConditions) {
        if (this[counter] != conditions) {
            tempList.add(this[counter])
            counter++
        } else notMatchConditions = false
    }

    return tempList
}
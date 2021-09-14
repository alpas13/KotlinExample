package ru.skillbranch.kotlinexample.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T> {
    val tempList = mutableListOf<T>()
    var i = 0
    while (!predicate(this[i])) {
        tempList.add(this[i])
        i++
    }
    return tempList.map { it }
}

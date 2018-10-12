import kotlinx.coroutines.experimental.delay

suspend fun Calculate(cell: Cell) {
    val numbers = gson.fromJson<List<Int>>(cell.line, List::class.java)
    var answer:Int = 0
    numbers.forEach {
        answer = answer + it
    }
    cell.line = answer.toString()
    cell.lastUnit = userID!!
    cell.isReturning = true
    delay(4000L)
    Call(cell)
}
import kotlinx.coroutines.experimental.delay

suspend fun Calculate(cell: Cell) {
    val numbers = gson.fromJson<List<Int>>(cell.line, List::class.java)
    var answer:Int = 0
    numbers.forEach {
        answer = answer + it
    }
    println("Call calculated:"+answer.toString())
    cell.line = answer.toString()
    cell.lastUnit = userID!!
    cell.isReturning = true
    Call(cell)
}
import kotlinx.coroutines.experimental.io.readUTF8Line
import kotlinx.coroutines.experimental.io.writeStringUtf8

suspend fun Call(cell: Cell) {
    val cellJSON = gson.toJson(cell)
    output!!.writeStringUtf8(cellJSON+"\r\n")
}

suspend fun Recive() {
    while (true) {
        val responseLine = input!!.readUTF8Line()
        if (responseLine == null) {
            println("Null Call") //throw Exception("Socket Closed")
        } else {
            val response = gson.fromJson<Cell>(responseLine, Cell::class.java)
            if (!response.isReturning) {
                Calculate(response)
            } else println("Answer ${response.ID} - ${response.line}")
        }
    }
}
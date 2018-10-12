import kotlinx.coroutines.experimental.io.readUTF8Line
import kotlinx.coroutines.experimental.io.writeStringUtf8

suspend fun Call(cell: Cell) {
    val cellJSON = gson.toJson(cell)
    output!!.writeStringUtf8(cellJSON+"\r\n")
}

suspend fun Recive() {
    while (true) {
        val responseLine = input!!.readUTF8Line()
        val response = client.gson.fromJson<Cell>(responseLine, Cell::class.java)
        if (!response.isReturning) {
            Calculate(response)
        } else println(response.line)
    }
}
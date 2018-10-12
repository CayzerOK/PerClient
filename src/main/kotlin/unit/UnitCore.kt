package unit

import com.google.gson.Gson
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.network.util.ioCoroutineDispatcher
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.io.ByteReadChannel
import kotlinx.coroutines.experimental.io.ByteWriteChannel
import kotlinx.coroutines.experimental.io.readUTF8Line
import kotlinx.coroutines.experimental.io.writeStringUtf8
import java.net.InetSocketAddress
import java.time.LocalDateTime


var gson = Gson()
var unitID = 0

data class Cell(
        var time:LocalDateTime,
        var lastUnit:Int,
        var origin:Int,
        var line:String,
        var isReturning: Boolean)


fun main(args: Array<String>) {
    runBlocking{
        val socket = aSocket(ActorSelectorManager(ioCoroutineDispatcher)).tcp().connect(InetSocketAddress("127.0.0.1", 2323))
        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)
        unitID = input.readInt()
        println("ID - " + unitID)
        try {
            Resive(input,output)

        } catch (e: Throwable) {
            e.printStackTrace()
            socket.close()
        }
    }
}



suspend fun Answer(output: ByteWriteChannel, cell: Cell) {
    cell.isReturning = true
    val cellJSON = gson.toJson(cell)
    output.writeStringUtf8("$cellJSON\r\n")
}

suspend fun Resive(input:ByteReadChannel,output: ByteWriteChannel) {
    while (true) {
        val responseLine = input.readUTF8Line()
        val response = gson.fromJson<Cell>(responseLine, Cell::class.java)
        println(response)
        val numbers = gson.fromJson<List<Int>>(response.line, List::class.java)
        var answer:Int = 0
        numbers.forEach {
            print("$answer + $it = ")
            answer = answer + it
            println(answer)
        }
        delay(10000L)
        response.line = answer.toString()
        response.lastUnit = unitID
        Answer(output, response)
    }
}

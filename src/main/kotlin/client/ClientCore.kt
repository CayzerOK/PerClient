package client

import com.google.gson.Gson
import io.ktor.network.selector.ActorSelectorManager
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
import java.util.*

var gson = Gson()
var userID:Int? = null

data class Cell(
        var ID:LocalDateTime,
        var lastUnit:Int,
        var origin:Int,
        var line:String,
        var isReturning: Boolean)

fun main(args: Array<String>) {
    runBlocking{
        val socket = aSocket(ActorSelectorManager(ioCoroutineDispatcher)).tcp().connect(InetSocketAddress("127.0.0.1", 2323))
        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)
        userID = input.readInt()
        println("UserID is "+ userID)
        try {

            val connect = launch { Connect(output) }
            val response = launch { Response(input) }
            connect.join()
            response.join()

        } catch (e: Throwable) {
            e.printStackTrace()
            socket.close()
        }
    }
}



suspend fun Connect(output: ByteWriteChannel) {
    data class Numbers(
            val n:List<Int>
    )
    while(true) {
        readLine()
        val data = mutableListOf<Int>()
        val number = Random().nextInt(5)
        for(index in 0..number) {
            data.add(Random().nextInt(10))
        }
        println(data)
        val numbers = gson.toJson(data)
        val cell = Cell(LocalDateTime.now(), userID!!, userID!!, numbers,false)
        val cellJSON = gson.toJson(cell)
        println(cellJSON)
        output.writeStringUtf8("$cellJSON\r\n")
    }
}

suspend fun Response(input:ByteReadChannel) {
    while (true) {
        val responseLine = input.readUTF8Line()
        val response = gson.fromJson<Cell>(responseLine, Cell::class.java)
        println(response.line)
    }
}

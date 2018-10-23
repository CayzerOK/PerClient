import com.google.gson.Gson
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.network.util.ioCoroutineDispatcher
import kotlinx.coroutines.experimental.io.ByteReadChannel
import kotlinx.coroutines.experimental.io.ByteWriteChannel
import kotlinx.coroutines.experimental.io.readUTF8Line
import kotlinx.coroutines.experimental.io.writeStringUtf8
import kotlinx.coroutines.experimental.*
import java.net.InetSocketAddress
import java.util.*
import java.net.InetAddress
import java.net.URL


var gson = Gson()
var userID:UUID? = null
var socket:Socket? = null
var input:ByteReadChannel? = null
var output:ByteWriteChannel? = null



data class Cell(
        var ID: Long,
        var lastUnit:UUID,
        var origin:UUID,
        var line:String,
        var isReturning: Boolean)

fun main(args: Array<String>) {
    print("Enter Server-IP: ")
    val adress = readLine()
    runBlocking{
        socket = aSocket(ActorSelectorManager(ioCoroutineDispatcher)).tcp().connect(InetSocketAddress(adress, 8080 ))
        input = socket!!.openReadChannel()
        output = socket!!.openWriteChannel(autoFlush = true)
        val JsonID = input!!.readUTF8Line()
        userID = gson.fromJson(JsonID, UUID::class.java)
        println("UserID accepted: $userID")
        try {
            val response = launch { Recive() }
            val loop = launch { MainLoop() }
            response.join()
            loop.join()
        } catch (e: Exception) {
            println(e.message)
            socket!!.close()
        }
    }
}


suspend fun MainLoop() {
    while (true) {
        val terminal = readLine()
        if (terminal != "") {
            output!!.writeStringUtf8(terminal+"\r\n")
        } else {
            val data = mutableListOf<Int>()
            val number = Random().nextInt(5)
            for (index in 0..number) {
                data.add(Random().nextInt(10))
            }
            val numbers = gson.toJson(data)
            val cell = Cell(System.currentTimeMillis(), userID!!, userID!!, numbers, false)
            println("${cell.ID} - ${cell.line}")
            Call(cell)
        }
    }
}
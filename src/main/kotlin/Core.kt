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
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.net.InetSocketAddress
import java.util.*

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
    runBlocking{
        socket = aSocket(ActorSelectorManager(ioCoroutineDispatcher)).tcp().connect(InetSocketAddress("127.0.0.1", 2323))
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
        } catch (e: Throwable) {
            e.printStackTrace()
            socket!!.close()
        }
    }
}


suspend fun MainLoop() {
    while (true) {
        val terminal = readLine()
        if (terminal == "/stop") {
            socket!!.close()
        } else {
            val data = mutableListOf<Int>()
            val number = Random().nextInt(5)
            for (index in 0..number) {
                data.add(Random().nextInt(10))
            }
            println(data)
            val numbers = gson.toJson(data)
            val cell = Cell(System.currentTimeMillis(), userID!!, userID!!, numbers, false)
            val cellJSON = gson.toJson(cell)
            println(cellJSON)
            Call(cell)
        }
    }
}
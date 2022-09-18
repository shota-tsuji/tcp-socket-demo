package ktor

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.net.BindException
import java.net.InetSocketAddress
import kotlin.test.assertEquals

class EchoTest {

    companion object {
        private val echoServer = EchoServer()

        @JvmStatic
        @BeforeAll
        fun setup() {
            Thread {
                echoServer.start()
            }.start()
        }
    }

    @Test
    fun serverTest() {
        Thread.sleep(100)
        runBlocking {
            val socket =
                aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(InetSocketAddress("127.0.0.1", 2224))
            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)

            output.writeFully("hello\r\n".toByteArray())
            assertEquals("hello", input.readUTF8Line())
        }
    }

    @Test
    fun clientTest() {
        assertDoesNotThrow { EchoClient().start() }
    }

    @Test
    fun startingTwiceServerCreateBindingExceptionTest() {
        // Wait for server-setup.
        Thread.sleep(100)
        assertThrows(BindException::class.java) { EchoServer().start() }
    }
}

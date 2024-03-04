package nl.tudelft.ipv8.jvm.demo

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver.Companion.IN_MEMORY
import kotlinx.coroutines.*
import mu.KotlinLogging
import nl.tudelft.ipv8.*
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import nl.tudelft.ipv8.attestation.trustchain.TrustChainSettings
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainSQLiteStore
import nl.tudelft.ipv8.keyvault.JavaCryptoProvider
import nl.tudelft.ipv8.messaging.EndpointAggregator
import nl.tudelft.ipv8.messaging.udp.UdpEndpoint
import nl.tudelft.ipv8.peerdiscovery.DiscoveryCommunity
import nl.tudelft.ipv8.peerdiscovery.strategy.PeriodicSimilarity
import nl.tudelft.ipv8.peerdiscovery.strategy.RandomChurn
import nl.tudelft.ipv8.peerdiscovery.strategy.RandomWalk
import nl.tudelft.ipv8.sqldelight.Database
import java.net.InetAddress
import java.util.*
import kotlin.math.roundToInt
import nl.tudelft.ipv8.jvm.demo.util.CreateDaoHelper
import nl.tudelft.ipv8.jvm.demo.CoinCommunity

import java.util.Scanner


class Application {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val logger = KotlinLogging.logger {}
    
    // Create dao helper
    private val daoCreateHelper = CreateDaoHelper()
    private val coinCommunity = CoinCommunity()
   
    fun run() {
        startIpv8()
    }

    private fun createTrustChainCommunity(): OverlayConfiguration<TrustChainCommunity> {
        val settings = TrustChainSettings()
        val driver: SqlDriver = JdbcSqliteDriver(IN_MEMORY)
        Database.Schema.create(driver)
        val database = Database(driver)
        val store = TrustChainSQLiteStore(database)
        val randomWalk = RandomWalk.Factory(timeout = 3.0, peers = 20)
        return OverlayConfiguration(
            TrustChainCommunity.Factory(settings, store),
            listOf(randomWalk)
        )
    }

    private fun startIpv8() {
        val myKey = JavaCryptoProvider.generateKey()
        val myPeer = Peer(myKey)
        val udpEndpoint = UdpEndpoint(8090, InetAddress.getByName("0.0.0.0"))
        val endpoint = EndpointAggregator(udpEndpoint, null)

        val config = IPv8Configuration(
            overlays = listOf(
                createTrustChainCommunity(),
            ), walkerInterval = 1.0
        )

        val ipv8 = IPv8(endpoint, config, myPeer)
        ipv8.start()


        scope.launch {
            
            //setup ipv8 instance for daoHelpers 
            daoCreateHelper.ipv8Instance = ipv8
            coinCommunity.ipv8Instance = ipv8
            coinCommunity.myPeer = myPeer
            // TODO : Instantiate the Wallet Manager
            
            while (true) {
                print("DA:")

                // for ((_, overlay) in ipv8.overlays) {
                    // printPeersInfo(overlay)
                // }
                // logger.info("===")
                // delay(5000)
            }
        }

        // while (ipv8.isStarted()) {
        //     while (true) {
        //         print("command> ") // Prompt for command
        //         val command = readLine() // Read command from standard input
        //         if (command != null) {
        //             if (command == "exit") {
        //                 println("Exiting command line...")
        //                 break
        //             }
        //             // Process the command
        //             println("You entered: $command")
        //             // Add more command processing logic here
        //         }
        //     }
        //     // Thread.sleep(1000)
        // }

        val scanner = Scanner(System.`in`)
        val commandLineThread = Thread {
            try {
                while (true) {
                    print("command> ")
                    while (!scanner.hasNextLine()) {
                        Thread.sleep(100) // Sleep a bit waiting for input
                    }
                    val command = scanner.nextLine()
                    if (command == "exit") {
                        println("Exiting command line...")
                        break
                    }
                    println("You entered: $command")
                }
            } catch (e: Exception) {
                println("An error occurred: ${e.message}")
            }
        }
    
        commandLineThread.start()
        commandLineThread.join()
    }

    private fun printPeersInfo(overlay: Overlay) {
        val peers = overlay.getPeers()
        logger.info(overlay::class.simpleName + ": ${peers.size} peers")
        for (peer in peers) {
            val avgPing = peer.getAveragePing()
            val lastRequest = peer.lastRequest
            val lastResponse = peer.lastResponse

            val lastRequestStr = if (lastRequest != null)
                "" + ((Date().time - lastRequest.time) / 1000.0).roundToInt() + " s" else "?"

            val lastResponseStr = if (lastResponse != null)
                "" + ((Date().time - lastResponse.time) / 1000.0).roundToInt() + " s" else "?"

            val avgPingStr = if (!avgPing.isNaN()) "" + (avgPing * 1000).roundToInt() + " ms" else "? ms"
            logger.info("${peer.mid} (S: ${lastRequestStr}, R: ${lastResponseStr}, ${avgPingStr})")
        }
    }
    

    // private fun createDao(overlay: Overlay){
    //     val newDAO =
    //     getCoinCommunity().createBitcoinGenesisWallet(
    //         currentEntranceFee,
    //         currentThreshold,
    //         requireContext()
    //     )
    //     val walletManager = WalletManagerAndroid.getInstance()
    //     walletManager.addNewNonceKey(newDAO.getData().SW_UNIQUE_ID, requireContext())

    // }
}

fun main() {
    val app = Application()
    app.run()
}

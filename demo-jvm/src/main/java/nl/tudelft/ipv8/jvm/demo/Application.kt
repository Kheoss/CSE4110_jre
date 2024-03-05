package nl.tudelft.ipv8.jvm.demo

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver.Companion.IN_MEMORY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import nl.tudelft.ipv8.*
import nl.tudelft.ipv8.jvm.demo.coin.*
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


import nl.tudelft.ipv8.jvm.demo.util.SimulatedContext
import nl.tudelft.ipv8.jvm.demo.util.CreateDaoHelper
import nl.tudelft.ipv8.jvm.demo.CoinCommunity


import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.io.File

import nl.tudelft.ipv8.jvm.demo.util.WalletService

import nl.tudelft.ipv8.jvm.demo.sharedWallet.SWJoinBlockTransactionData

import nl.tudelft.ipv8.attestation.trustchain.store.UserInfo

class Application {

    private val cacheDir = File("cacheDir")

    private val scope = CoroutineScope(Dispatchers.Default)
    private val logger = KotlinLogging.logger {}

    private val simContext = SimulatedContext()
    
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

        WalletService.createGlobalWallet(cacheDir ?: throw Error("CacheDir not found"))

        
        scope.launch {
            daoCreateHelper.ipv8Instance = ipv8
            coinCommunity.ipv8Instance = ipv8
            coinCommunity.myPeer = myPeer       
            delay(15000)
           

                printAllSharedWallets()
                delay(5000)

                logger.error("Users: " + getUsers(ipv8.getOverlay()!!).size)
                delay(2000)
                logger.error("ADD BTC")
                addBTC(WalletManager.getInstance()!!.protocolAddress().toString())
                logger.error("Wait 50 seconds")
                
                delay(5000)
                logger.error("CREATE A WALLET")
                createDao(myPeer)
                delay(5000)
                // printAllSharedWallets()

                 while (true) {
                printAllSharedWallets()
                delay(1000)

                logger.error("Users: " + getUsers(ipv8.getOverlay()!!).size)
                delay(3000)
            }
           
        }

        while (ipv8.isStarted()) {
            Thread.sleep(1000)
        }
       
    }


    fun getUsers(trustChainCommunity: TrustChainCommunity): List<UserInfo> {
        return trustChainCommunity.database.getUsers()
    }
    /**
     * Add bitcoin to the wallet
     * @param address - The address where I have to send the BTC to.
     * @return Boolean - if the transaction was successful
     */
    private fun addBTC(address: String): Boolean {
        val executor: ExecutorService =
            Executors.newCachedThreadPool(Executors.defaultThreadFactory())
        val future: Future<Boolean>

        val url = "https://taproot.tribler.org/addBTC?address=$address"

        future =
            executor.submit(
                object : Callable<Boolean> {
                    override fun call(): Boolean {
                        val connection = URL(url).openConnection() as HttpURLConnection

                        try {
                            // If it fails, check if there is enough balance available on the server
                            // Otherwise reset the bitcoin network on the server (there is only 15k BTC available).
                            // Also check if the Python server is still running!
                            logger.error("Coin", url)
                            logger.error("Coin", connection.responseMessage)
                            return connection.responseCode == 200
                        } finally {
                            connection.disconnect()
                        }
                    }
                }
            )

        return try {
            future.get(10, TimeUnit.SECONDS)
        } catch (e: Exception) {
            false
        }
    }

    private fun printAllSharedWallets(){
        val wallets = coinCommunity.discoverSharedWallets()
        
        logger.error("Available wallets: " + wallets.size);

        for(wallet in wallets){
            val blockData = SWJoinBlockTransactionData(wallet.transaction).getData()
            logger.error(blockData.SW_UNIQUE_ID);
        }
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
    

    private fun createDao(myPeer: Peer){
        val newDAO =
        daoCreateHelper.createBitcoinGenesisWallet(
            myPeer,
            50000,
            50,
            simContext
        )
        WalletManager.getInstance()!!.addNewNonceKey(newDAO.getData().SW_UNIQUE_ID, simContext)
    }
}

fun main() {
    val app = Application()
    app.run()
}

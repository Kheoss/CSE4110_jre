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


import nl.tudelft.ipv8.jvm.demo.sharedWallet.SWJoinBlockTransactionData

class Application {
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
        
        scope.launch {
            daoCreateHelper.ipv8Instance = ipv8
            coinCommunity.ipv8Instance = ipv8
            coinCommunity.myPeer = myPeer       
            delay(15000)
            // while (true) {
                printAllSharedWallets()
                delay(5000)
                logger.error("CREATE A WALLET")
                createDao(myPeer)
                delay(5000)
                printAllSharedWallets()
            // }

        }

        while (ipv8.isStarted()) {
            Thread.sleep(1000)
        }
       
    }

    private fun printAllSharedWallets(){
        val wallets = coinCommunity.fetchLatestJoinedSharedWalletBlocks()
        
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

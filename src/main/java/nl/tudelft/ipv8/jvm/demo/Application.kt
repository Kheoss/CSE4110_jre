package nl.tudelft.ipv8.jvm.demo

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver.Companion.IN_MEMORY
import com.beust.klaxon.Klaxon

import mu.KotlinLogging
import nl.tudelft.ipv8.*
import nl.tudelft.ipv8.attestation.trustchain.CallbackInterface

import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import nl.tudelft.ipv8.attestation.trustchain.TrustChainSettings
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainSQLiteStore
import nl.tudelft.ipv8.attestation.trustchain.store.UserInfo
import nl.tudelft.ipv8.jvm.demo.coin.BitcoinNetworkOptions
import nl.tudelft.ipv8.jvm.demo.coin.WalletManagerConfiguration
import nl.tudelft.ipv8.jvm.demo.sharedWallet.SWJoinBlockTransactionData
import nl.tudelft.ipv8.jvm.demo.sharedWallet.SWResponseSignatureBlockTD
import nl.tudelft.ipv8.jvm.demo.sharedWallet.SWSignatureAskBlockTD
import nl.tudelft.ipv8.jvm.demo.sharedWallet.SWTransferFundsAskTransactionData
import nl.tudelft.ipv8.jvm.demo.util.*
import nl.tudelft.ipv8.keyvault.JavaCryptoProvider
import nl.tudelft.ipv8.messaging.EndpointAggregator
import nl.tudelft.ipv8.messaging.udp.UdpEndpoint
import nl.tudelft.ipv8.peerdiscovery.DiscoveryCommunity
import nl.tudelft.ipv8.peerdiscovery.strategy.PeriodicSimilarity
import nl.tudelft.ipv8.peerdiscovery.strategy.RandomChurn
import nl.tudelft.ipv8.peerdiscovery.strategy.RandomWalk
import nl.tudelft.ipv8.sqldelight.Database
import nl.tudelft.trustchain.currencyii.coin.WalletManager
import java.io.File
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URI
import java.net.URL
import java.util.*
import java.util.concurrent.*
import kotlin.math.roundToInt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Application: CallbackInterface {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val logger = KotlinLogging.logger {}
    private val simContext = SimulatedContext()
   
    
    private val commandListener: CommandListener = CommandListener(URI("ws://localhost:7071"), this)

    private var proposals: ArrayList<TrustChainBlock> = ArrayList()

    override fun onCallbackEvent() {
        commandListener!!.send(
                        Klaxon().toJsonString(
                            Message(
                                Operation.SYNC_COMPLETE.op, Klaxon().toJsonString(
                                    ParamsDAOIdResponse("sync")
                                )
                            )
                        )
                    ) 
    }

    fun run() {
        commandListener.connect()
        startIpv8()
    }

  

    protected fun getIpv8(): IPv8 {
        return IPv8Network.getInstance()
    }

    private fun getCoinCommunity(): CoinCommunity {
        return getIpv8().getOverlay()
            ?: throw IllegalStateException("CoinCommunity is not configured")
    }

    private fun getTrustChainCommunity(): TrustChainCommunity {
        return IPv8Network.getInstance().getOverlay()
            ?: throw IllegalStateException("TrustChainCommunity is not configured")
    }

    fun interpretCommand(command: String) {
        val message: Message? = Klaxon().parse<Message>(command);
        if (message != null) {
            when(Operation.fromInt(message.operation)) {
                Operation.PRINT_ALL_WALLETS -> logger.error ("joined wallets: " + getCoinCommunity().fetchLatestJoinedSharedWalletBlocks().map { SWJoinBlockTransactionData(it.transaction).getData().SW_UNIQUE_ID  })
                Operation.PRINT_USER_COUNT -> logger.error("Users: " + getCoinCommunity().getUsers().size)
                Operation.CREATE_DAO -> {
                    val newDAO = getCoinCommunity().createBitcoinGenesisWallet(50000, 100, simContext)
                    WalletManager.getInstance().addNewNonceKey(newDAO.getData().SW_UNIQUE_ID, simContext)
                    val id: String = newDAO.getData().SW_UNIQUE_ID
                    commandListener.send(
                        Klaxon().toJsonString(
                            Message(
                                Operation.SEND_NEW_DAO_ID.op, Klaxon().toJsonString(
                                    ParamsDAOIdResponse(id)
                                )
                            )
                        )
                    )
                }
                Operation.JOIN_WALLET -> {
                    val id: String  = Klaxon().parse<ParamsDAOIdResponse>(message.params)!!.id
                    val wallet: TrustChainBlock? = getCoinCommunity().getSharedWalletBySWID(id)

                    if(wallet != null) {
                        joinSharedWallet(wallet)
                     
                    }
                }
                Operation.START_SIMULATION -> {
                    val id: String  = Klaxon().parse<ParamsDAOIdResponse>(message.params)!!.id
                    getCoinCommunity().broadCastNotification(id)         
                }
                else -> {
                    println("no such command")
                }
            }
        }

        else {
            println("Message could not be parsed")
        }
    }


    private fun joinSharedWallet(block: TrustChainBlock) {
        val mostRecentSWBlock =
            getCoinCommunity().fetchLatestSharedWalletBlock(block.calculateHash())
                ?: block

        // Add a proposal to trust chain to join a shared wallet
            try {
                getCoinCommunity().proposeJoinWallet(
                    mostRecentSWBlock
                ).getData()
            } catch (t: Throwable) {
                Log.e("Coin", "Join wallet proposal failed. ${t.message ?: "No further information"}.")
                return
            }

        // // Wait and collect signatures
        // var signatures: List<SWResponseSignatureBlockTD>? = null
        // while (signatures == null) {
        //     Thread.sleep(1000)
        //     signatures = collectJoinWalletResponses(proposeBlockData)
        // }

        // // Create a new shared wallet using the signatures of the others.
        // // Broadcast the new shared bitcoin wallet on trust chain.
        // try {
        //     getCoinCommunity().joinBitcoinWallet(
        //         mostRecentSWBlock.transaction,
        //         proposeBlockData,
        //         signatures,
        //         simContext
        //     )
            
        //     // Add new nonceKey after joining a DAO
        //     WalletManager.getInstance()
        //         .addNewNonceKey(proposeBlockData.SW_UNIQUE_ID, simContext)
        // } catch (t: Throwable) {
        //     Log.e("Coin", "Joining failed. ${t.message ?: "No further information"}.")
        // }

        // Update wallets UI list
    }

    private fun collectJoinWalletResponses(blockData: SWSignatureAskBlockTD): List<SWResponseSignatureBlockTD>? {
        val responses =
            getCoinCommunity().fetchProposalResponses(
                blockData.SW_UNIQUE_ID,
                blockData.SW_UNIQUE_PROPOSAL_ID
            )

        Log.i(
            "Coin",
            "Waiting for signatures. ${responses.size}/${blockData.SW_SIGNATURES_REQUIRED} received!"
        )


        if (responses.size >= blockData.SW_SIGNATURES_REQUIRED) {
            return responses
        }
        return null
    }


    private fun generateWalletManagerInstance(myPeer: Peer) : WalletManager {
        val network = BitcoinNetworkOptions.REG_TEST
        val seed = WalletManager.generateRandomDeterministicSeed(network)
        val config = WalletManagerConfiguration(
            network,
            // SerializedDeterministicKey(seed, seed.creationTime),
            seed,
            null
        )
//        val walletDir = File("wallet-" + myPeer.publicKey)

        val walletDir = File("/home/kheoss/UniStuff/Blockchain/CSE4110_jre/demo-jvm/src/main/java/nl/tudelft/ipv8/jvm/demo/wallets//wallet-" + myPeer.publicKey)
        walletDir.mkdir()

        val walletManager = if(WalletManager.isInitialized()) WalletManager.getInstance() else WalletManager.createInstance(config, walletDir, config.key, config.addressPrivateKeyPair)

        walletManager.setCommandListenerWalletManager(commandListener);
        getCoinCommunity().setCommandListenerForCoin(commandListener);
        getTrustChainCommunity().setCallback(this);

        return walletManager;
    }
   
    
    private fun addBTC(address: String): Boolean {
        // disableCertificateValidation()
       
        val executor: ExecutorService =
            Executors.newCachedThreadPool(Executors.defaultThreadFactory())
        val future: Future<Boolean>
        val url = "http://127.0.0.1:443/addBTC?address=$address"

        future =
            executor.submit(
                object : Callable<Boolean> {
                    override fun call(): Boolean {
                        val connection = URL(url).openConnection() as HttpURLConnection
                        Log.i("ADD BTC", url)
                        Log.i("ADD BTC", connection.responseMessage)
                        
                        try {
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

    private fun startIpv8() {
        val myKey = JavaCryptoProvider.generateKey()
        IPv8Network.Factory().setPrivateKey(myKey).init();
        generateWalletManagerInstance(getCoinCommunity().myPeer)
        if(!addBTC(WalletManager.getInstance().protocolAddress().toString())){
            Log.i("ADD BTC", "FAILED")
        }

        scope.launch {
            while(true){

                /*
                    See if we get updated
                */
                val sharedWallets = getCoinCommunity().getAllSharedWallets()
                Log.e("SHARED WALLETS", "${sharedWallets.size}")
                val databaseProposals = getCoinCommunity().fetchProposalBlocks()
                updateProposals(databaseProposals)
                crawlProposalsAndUpdateIfNewFound()
                val toVote = filterByNotVoted()
                
                val myPublicKey = getTrustChainCommunity().myPeer.publicKey.keyToBin()
                for (b in toVote) {
                    getCoinCommunity().joinAskBlockReceived(b, myPublicKey, true, simContext)
                }

                delay(1000)
            }
            }

        while (IPv8Network.getInstance().isStarted()) {
            Thread.sleep(1000)
        }
    }

    private fun filterByNotVoted(): List<TrustChainBlock> {
        return proposals.filter { block -> block.type == CoinCommunity.SIGNATURE_ASK_BLOCK }
            .filter { block ->
                val data = SWTransferFundsAskTransactionData(block.transaction).getData()
                // Get favor votes
                val favorVotes =
                    ArrayList(
                        getCoinCommunity()
                            .fetchProposalResponses(data.SW_UNIQUE_ID, data.SW_UNIQUE_PROPOSAL_ID)
                    ).map { it.SW_BITCOIN_PK }
                // Get against votes
                val negativeVotes =
                    ArrayList(
                        getCoinCommunity().fetchNegativeProposalResponses(
                            data.SW_UNIQUE_ID,
                            data.SW_UNIQUE_PROPOSAL_ID
                        )
                    ).map { it.SW_BITCOIN_PK }

                val myPublicBitcoinKey = WalletManager.getInstance().protocolECKey().publicKeyAsHex
                !(favorVotes.contains(myPublicBitcoinKey) ||
                    negativeVotes.contains(
                        myPublicBitcoinKey
                    ))
            }
    }

    private fun updateProposals(newProposals: List<TrustChainBlock>) {
        Log.e("schim", "${newProposals.size}")
        val coinCommunity = getCoinCommunity()
        val proposalIds =
            proposals.map {
                coinCommunity.fetchSignatureRequestProposalId(it)
            }
        val distinctById =
            newProposals.distinctBy {
                coinCommunity.fetchSignatureRequestProposalId(it)
            }

        for (proposal in distinctById) {
            val currentId = coinCommunity.fetchSignatureRequestProposalId(proposal)
            if (!proposalIds.contains(currentId)) {
                proposals.add(proposal)
            }
        }
    }

    private suspend fun crawlProposalsAndUpdateIfNewFound() {
        val allUsers = getCoinCommunity().getPeers()
        Log.i("Coin", "Found ${allUsers.size} peers, crawling")

        for (peer in allUsers) {
            try {
                // TODO: Commented this line out, it causes the app to crash
//                withTimeout(JoinDAOFragment.SW_CRAWLING_TIMEOUT_MILLI) {
                getCoinCommunity().trustChainHelper.crawlChain(peer)
                val crawlResult =
                    getCoinCommunity().trustChainHelper
                        .getChainByUser(peer.publicKey.keyToBin())
                        .filter {
                            (
                                it.type == CoinCommunity.SIGNATURE_ASK_BLOCK ||
                                    it.type == CoinCommunity.TRANSFER_FUNDS_ASK_BLOCK
                                ) && !getCoinCommunity().checkEnoughFavorSignatures(it)
                        }
                Log.i(
                    "Coin",
                    "Crawl result: ${crawlResult.size} proposals found (from ${peer.address})"
                )
            
                if (crawlResult.isNotEmpty()) {
                    updateProposals(crawlResult)
                }
//                }
            } catch (t: Throwable) {
                val message = t.message ?: "no message"
                Log.e("Coin", "Crawling failed for: ${peer.address} message: $message")
            }
        }
    }

    private fun printAllSharedWallets() {
        val wallets = getCoinCommunity().discoverSharedWallets()
        logger.info("Available wallets: " + wallets.size);

        for(wallet in wallets){
            val blockData = SWJoinBlockTransactionData(wallet.transaction).getData()
            logger.info(blockData.SW_UNIQUE_ID);
        }
    }

    private fun getUsers(trustChainCommunity: TrustChainCommunity): List<UserInfo> {
        return trustChainCommunity.database.getUsers()
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
}

fun main() {
    val app = Application()
    app.run()
}

package nl.tudelft.ipv8.jvm.demo.util

import nl.tudelft.ipv8.jvm.demo.util.SimulatedContext
import nl.tudelft.ipv8.jvm.demo.sharedWallet.SWJoinBlockTransactionData
import nl.tudelft.ipv8.jvm.demo.util.taproot.TaprootUtil
import nl.tudelft.ipv8.jvm.demo.TrustChainHelper
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.ipv8.jvm.demo.coin.WalletManagerConfiguration

import nl.tudelft.ipv8.jvm.demo.coin.*
import nl.tudelft.ipv8.IPv8
import nl.tudelft.ipv8.Peer

import java.io.File

class CreateDaoHelper {

    private val network = BitcoinNetworkOptions.REG_TEST
    private val seed = WalletManager.generateRandomDeterministicSeed(network)
    private val config = WalletManagerConfiguration(
                        network,
                        // SerializedDeterministicKey(seed, seed.creationTime),
                        seed,
                        null
                    )
    private val walletDir = File("wallet")

    public val walletManager = if(WalletManager.isInitialized()) WalletManager.getInstance() else WalletManager.createInstance(config, walletDir, config.key, config.addressPrivateKeyPair);


    public var ipv8Instance: IPv8? = null
        get() = field                     
        set(value) { field = value }      
    

   

    private fun getTrustChainCommunity(): TrustChainCommunity {
        return ipv8Instance?.getOverlay()
            ?: throw IllegalStateException("TrustChainCommunity is not configured")
    }

    fun getTrustchain(): TrustChainHelper {
        return TrustChainHelper(getTrustChainCommunity())
    }


     /**
     * 1.1 Create a shared wallet block.
     * The bitcoin transaction may take some time to finish.
     * If the transaction is valid, the result is broadcasted on trust chain.
     * **Throws** exceptions if something goes wrong with creating or broadcasting bitcoin transaction.
     */
    fun createBitcoinGenesisWallet(
        myPeer: Peer,
        entranceFee: Long,
        threshold: Int,
        context: SimulatedContext
    ): SWJoinBlockTransactionData {
        val (_, serializedTransaction) =
            walletManager!!.safeCreationAndSendGenesisWallet(
                Coin.valueOf(entranceFee)
            )

        // Broadcast on trust chain if no errors are thrown in the previous step.
        return broadcastCreatedSharedWallet(
            myPeer,
            serializedTransaction,
            entranceFee,
            threshold,
            context
        )
    }

        /**
     * 1.2 Finishes the last step of creating a genesis shared bitcoin wallet.
     * Posts a self-signed trust chain block containing the shared wallet data.
     */
    fun broadcastCreatedSharedWallet(
        myPeer: Peer,
        transactionSerialized: String,
        entranceFee: Long,
        votingThreshold: Int,
        context: SimulatedContext
    ): SWJoinBlockTransactionData {
        val bitcoinPublicKey = walletManager!!.networkPublicECKeyHex()
        val trustChainPk = myPeer.publicKey.keyToBin()
        val nonceKey = TaprootUtil.generateSchnorrNonce(ECKey().privKeyBytes)
        val noncePoint = nonceKey.second.getEncoded(true).toHex()

        if(walletManager == null) throw IllegalStateException("WalletManager is not configured")

        val blockData =
            SWJoinBlockTransactionData(
                entranceFee,
                transactionSerialized,
                votingThreshold,
                arrayListOf(trustChainPk.toHex()),
                arrayListOf(bitcoinPublicKey),
                arrayListOf(noncePoint)
            )

        walletManager.storeNonceKey(blockData.getData().SW_UNIQUE_ID, context, nonceKey.first.privKeyBytes.toHex())

        getTrustchain().createProposalBlock(blockData.getJsonString(), trustChainPk, blockData.blockType)
        return blockData
    }

}
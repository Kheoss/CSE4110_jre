package nl.tudelft.ipv8.jvm.demo


import nl.tudelft.ipv8.jvm.demo.sharedWallet.SWJoinBlockTransactionData
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import nl.tudelft.ipv8.attestation.trustchain.TrustChainTransaction
import nl.tudelft.ipv8.Overlay
import nl.tudelft.ipv8.util.hexToBytes
import nl.tudelft.ipv8.util.toHex

import nl.tudelft.ipv8.IPv8
import nl.tudelft.ipv8.Peer


class CoinCommunity constructor(trustChain: TrustChainCommunity, myPeer: Peer){
    
    private val trustChain = trustChain;
    private val myPeer = myPeer;
    public var ipv8Instance: IPv8? = null
        get() = field                     
        set(value) { field = value }      

   
    private fun getTrustChainCommunity(): TrustChainCommunity {
        return ipv8Instance?.getOverlay()
            ?: throw IllegalStateException("TrustChainCommunity is not configured")
    }
    /**
        * Discover shared wallets that you can join, return the latest blocks that the user knows of.
    */
    fun discoverSharedWallets(): List<TrustChainBlock> {
        val swBlocks = trustChain.database.getBlocksWithType(JOIN_BLOCK)
        return swBlocks
        .distinctBy { SWJoinBlockTransactionData(it.transaction).getData().SW_UNIQUE_ID }
        .map { fetchLatestSharedWalletBlock(it, swBlocks) ?: it }
    }

     /**
     * Discover shared wallets that you can join, return the latest (known) blocks
     * Fetch the latest block associated with a shared wallet.
     * swBlockHash - the hash of one of the blocks associated with a shared wallet.
     */
    fun fetchLatestSharedWalletBlock(swBlockHash: ByteArray): TrustChainBlock? {
        val swBlock =
            getTrustChainCommunity().database.getBlockWithHash(swBlockHash)
                ?: return null
        val swBlocks = getTrustChainCommunity().database.getBlocksWithType(JOIN_BLOCK)
        return fetchLatestSharedWalletBlock(swBlock, swBlocks)
    }

    /**
     * Fetch the latest shared wallet block, based on a given block 'block'.
     * The unique shared wallet id is used to find the most recent block in
     * the 'sharedWalletBlocks' list.
     */
    private fun fetchLatestSharedWalletBlock(
        block: TrustChainBlock,
        fromBlocks: List<TrustChainBlock>
    ): TrustChainBlock? {
        if (block.type != JOIN_BLOCK) {
            return null
        }
        val walletId = SWJoinBlockTransactionData(block.transaction).getData().SW_UNIQUE_ID

        return fromBlocks
            .filter { it.type == JOIN_BLOCK } // make sure the blocks have the correct type!
            .filter { SWJoinBlockTransactionData(it.transaction).getData().SW_UNIQUE_ID == walletId }
            .maxByOrNull { it.timestamp.time }
    }

    /**
     * Fetch the shared wallet blocks that you are part of, based on your trustchain PK.
     */
    fun fetchLatestJoinedSharedWalletBlocks(): List<TrustChainBlock> {
        return discoverSharedWallets().filter {
            val blockData = SWJoinBlockTransactionData(it.transaction).getData()
            val userTrustchainPks = blockData.SW_TRUSTCHAIN_PKS
            userTrustchainPks.contains(myPeer.publicKey.keyToBin().toHex())
        }
    }


    companion object {

        // Default maximum wait timeout for bitcoin transaction broadcasts in seconds
        const val DEFAULT_BITCOIN_MAX_TIMEOUT: Long = 10

        // Block type for join DAO blocks
        const val JOIN_BLOCK = "v1DAO_JOIN"

        // Block type for transfer funds (from a DAO)
        const val TRANSFER_FINAL_BLOCK = "v1DAO_TRANSFER_FINAL"

        // Block type for basic signature requests
        const val SIGNATURE_ASK_BLOCK = "v1DAO_ASK_SIGNATURE"

        // Block type for transfer funds signature requests
        const val TRANSFER_FUNDS_ASK_BLOCK = "v1DAO_TRANSFER_ASK_SIGNATURE"

        // Block type for responding to a signature request with a (should be valid) signature
        const val SIGNATURE_AGREEMENT_BLOCK = "v1DAO_SIGNATURE_AGREEMENT"

        // Block type for responding with a negative vote to a signature request with a signature
        const val SIGNATURE_AGREEMENT_NEGATIVE_BLOCK = "v1DAO_SIGNATURE_AGREEMENT_NEGATIVE"


    }
}
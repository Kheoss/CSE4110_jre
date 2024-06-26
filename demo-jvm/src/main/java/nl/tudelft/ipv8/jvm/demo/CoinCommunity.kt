package nl.tudelft.ipv8.jvm.demo

import nl.tudelft.ipv8.Community
import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import nl.tudelft.ipv8.attestation.trustchain.TrustChainTransaction
import nl.tudelft.ipv8.attestation.trustchain.store.UserInfo
import nl.tudelft.ipv8.jvm.demo.sharedWallet.*
import nl.tudelft.ipv8.jvm.demo.util.*
import nl.tudelft.ipv8.util.hexToBytes
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.ipv8.jvm.demo.util.NotificationMessage
import nl.tudelft.ipv8.messaging.Packet
import com.beust.klaxon.Klaxon
import kotlin.random.Random


class CoinCommunity : Community() {
    //override val serviceId = "02313685c1912a141279f8248fc8db5899c5df55"
    override val serviceId = "02313685c1912a141279f8248fc8db5899c5df56"

    private fun getTrustChainCommunity(): TrustChainCommunity {
        return IPv8Network.getInstance().getOverlay()
            ?: throw IllegalStateException("TrustChainCommunity is not configured")
    }

    val trustChainHelper: TrustChainHelper by lazy {
        TrustChainHelper(getTrustChainCommunity())
    }

    private val daoCreateHelper = DAOCreateHelper()
    private val daoJoinHelper = DAOJoinHelper()
    private val daoTransferFundsHelper = DAOTransferFundsHelper()

    public var commandListener: CommandListener? = null

    public fun setCommandListenerForCoin(aux: CommandListener){
        commandListener = aux;
    }

    private val receivedMessages = mutableSetOf<String>()

    init {
       messageHandlers[MessageId.MESSAGE_ID] = ::onNotificationMessage
    }


    private fun gossip(msg: NotificationMessage, newTTL: UInt, senderPeer: Peer){
        val randomPeer = getPeers().filter { it != senderPeer }.randomOrNull()
        val packet = serializePacket(MessageId.MESSAGE_ID, NotificationMessage(msg.message, msg.messageId, newTTL))
        if(randomPeer != null)
            send(randomPeer, packet)
    }

    

    private fun onNotificationMessage(packet: Packet) {
        val (peer, payload) = packet.getAuthPayload(NotificationMessage.Deserializer) 
      
        var newTTL = payload.ttl
        if (payload.messageId !in receivedMessages) {
            receivedMessages.add(payload.messageId)
         
           commandListener!!.send(
            Klaxon().toJsonString(
                Message(
                    Operation.NOTIFICATION.op, Klaxon().toJsonString(
                        ParamsDAOIdResponse(payload.messageId)
                    )
                )
            )
        )   
            
                newTTL--
            
        }

        if(payload.ttl > 0u){
            gossip(payload, newTTL, peer)
        }
        
        // Log.i("DemoCommunity", peer.mid + ": " + payload.message)
        // Log.e("DemoCommunity", peer.mid + ": " + payload.message)
        // Log.e("DemoCommunity", peer.mid + ": " + payload.message)
        // Log.e("DemoCommunity", peer.mid + ": " + payload.message)
        // Log.e("DemoCommunity", peer.mid + ": " + payload.message)
    }

    fun generate1KBString(): String {
       
        val charToRepeat = 'a' 
        val sizeInBytes = 1024
       
        return charToRepeat.toString().repeat(sizeInBytes)
    }
    

    fun broadCastNotification(id: String){
        val selectedPeers = getPeers().shuffled().take(1)

        for (peer in selectedPeers) {
            val packet = serializePacket(MessageId.MESSAGE_ID, NotificationMessage(generate1KBString(), id, 16.toUInt()))
            send(peer, packet)
        }
    }

    fun broadCastNotificationWithGossip(id: String){
        val peers = getPeers()
        for (peer in peers) {
            val packet = serializePacket(MessageId.MESSAGE_ID, NotificationMessage(generate1KBString(), id, 16.toUInt()))
            send(peer, packet)
        }
    }

    fun sendVoteFinishedMessage(peer: Peer, message: String) {
        // val packet = serializePacket(MessageId.MESSAGE_ID, NotificationMessage(message))
        // send(peer, packet)
    }

    /**
     * Create a bitcoin genesis wallet and broadcast the result on trust chain.
     * The bitcoin transaction may take some time to finish.
     * @throws - exception if something goes wrong with creating or broadcasting bitcoin transaction.
     * @param entranceFee - Long, the entrance fee for joining the DAO.
     * @param threshold - Int, the percentage of members that need to vote before allowing someone in the DAO.
     */
    fun createBitcoinGenesisWallet(
        entranceFee: Long,
        threshold: Int,
        context: SimulatedContext
    ): SWJoinBlockTransactionData {
        return daoCreateHelper.createBitcoinGenesisWallet(
            myPeer,
            entranceFee,
            threshold,
            context
        )
    }



    /**
     * 2.1 Send a proposal on the trust chain to join a shared wallet and to collect signatures.
     * The proposal is a serialized bitcoin join transaction.
     * **NOTE** the latest walletBlockData should be given, otherwise the serialized transaction is invalid.
     * @param walletBlock - the latest (that you know of) shared wallet block.
     */
    fun proposeJoinWallet(walletBlock: TrustChainBlock): SWSignatureAskTransactionData {
        return daoJoinHelper.proposeJoinWallet(myPeer, walletBlock)
    }

    /**
     * 2.2 Commit the join wallet transaction on the bitcoin blockchain and broadcast the result on trust chain.
     *
     * Note:
     * There should be enough sufficient signatures, based on the multisig wallet data.
     * @throws - exceptions if something goes wrong with creating or broadcasting bitcoin transaction.
     * @param walletBlockData - TrustChainTransaction, describes the wallet that is joined
     * @param blockData - SWSignatureAskBlockTD, the block where the other users are voting on
     * @param responses - the positive responses for your request to join the wallet
     */
    fun joinBitcoinWallet(
        walletBlockData: TrustChainTransaction,
        blockData: SWSignatureAskBlockTD,
        responses: List<SWResponseSignatureBlockTD>,
        context: SimulatedContext
    ) {
        daoJoinHelper.joinBitcoinWallet(
            myPeer,
            walletBlockData,
            blockData,
            responses,
            context
        )
    }

    /**
     * 3.1 Send a proposal block on trustchain to ask for the signatures.
     * Assumed that people agreed to the transfer.
     * @param walletBlock - TrustChainBlock, describes the wallet where the transfer is from
     * @param receiverAddressSerialized - String, the address where the transaction needs to go
     * @param satoshiAmount - Long, the amount that needs to be transferred
     * @return the proposal block
     */
    fun proposeTransferFunds(
        walletBlock: TrustChainBlock,
        receiverAddressSerialized: String,
        satoshiAmount: Long
    ): SWTransferFundsAskTransactionData {
        return daoTransferFundsHelper.proposeTransferFunds(
            myPeer,
            walletBlock,
            receiverAddressSerialized,
            satoshiAmount
        )
    }

    /**
     * 3.2 Transfer funds from an existing shared wallet to a third-party. Broadcast bitcoin transaction.
     * @param walletData - SWJoinBlockTD, the data about the wallet when joining the wallet
     * @param walletBlockData - TrustChainTransaction, describes the wallet where the transfer is from
     * @param blockData - SWTransferFundsAskBlockTD, the block where the other users are voting on
     * @param responses - List<SWResponseSignatureBlockTD>, the list with positive responses on the voting
     * @param receiverAddress - String, the address where the transfer needs to go
     * @param satoshiAmount - Long, the amount that needs to be transferred
     */
    fun transferFunds(
        walletData: SWJoinBlockTD,
        walletBlockData: TrustChainTransaction,
        blockData: SWTransferFundsAskBlockTD,
        responses: List<SWResponseSignatureBlockTD>,
        receiverAddress: String,
        satoshiAmount: Long,
        context: SimulatedContext
    ) {
        daoTransferFundsHelper.transferFunds(
            myPeer,
            walletData,
            walletBlockData,
            blockData,
            responses,
            receiverAddress,
            satoshiAmount,
            context
        )
    }

    /**
     * Discover shared wallets that you can join, return the latest blocks that the user knows of.
     */
    fun discoverSharedWallets(): List<TrustChainBlock> {
        val swBlocks = getTrustChainCommunity().database.getBlocksWithType(JOIN_BLOCK)
        return swBlocks
            .distinctBy { SWJoinBlockTransactionData(it.transaction).getData().SW_UNIQUE_ID }
            .map { fetchLatestSharedWalletBlock(it, swBlocks) ?: it }
    }

    fun getSharedWalletBySWID(id: String): TrustChainBlock? {
        val swBlocks = getTrustChainCommunity().database.getBlocksWithType(JOIN_BLOCK)
        return swBlocks
            .distinctBy { SWJoinBlockTransactionData(it.transaction).getData().SW_UNIQUE_ID }
            .map { fetchLatestSharedWalletBlock(it, swBlocks) ?: it }
            .filter { SWJoinBlockTransactionData(it.transaction).getData().SW_UNIQUE_ID == id }
            .maxByOrNull { it.timestamp.time }
    }

    /**
     * Crawl all shared wallet blocks of users in the trust chain.
     */
    private suspend fun crawlAvailableSharedWallets(peer: Peer): List<TrustChainBlock> {
        try {
            trustChainHelper.crawlChain(peer)

            return trustChainHelper
                .getChainByUser(peer.publicKey.keyToBin())
        } catch (t: Throwable) {
            val message = t.message ?: "No further information"
            // Log.e("Coin", "Crawling failed for: ${peer.publicKey}. $message.")
        }

        return ArrayList()
    }

    private fun addToWalletsList(wallets: ArrayList<TrustChainBlock>, newWallets: List<TrustChainBlock>) {
        val walletsCopy = arrayListOf<TrustChainBlock>()
        walletsCopy.addAll(wallets)
        val walletIds =
            walletsCopy.map {
                SWJoinBlockTransactionData(it.transaction).getData().SW_UNIQUE_ID
            }

        val distinctById = newWallets
            .filter {
                // Make sure that the trust chain block has the correct type
                it.type == CoinCommunity.JOIN_BLOCK
            }.distinctBy {
                SWJoinBlockTransactionData(it.transaction).getData().SW_UNIQUE_ID
            }

        for (wallet in distinctById) {
            val currentId = SWJoinBlockTransactionData(wallet.transaction).getData().SW_UNIQUE_ID
            if (!walletIds.contains(currentId)) {
                wallets.add(wallet)
            }
        }
    }

    suspend fun getAllSharedWallets(): List<TrustChainBlock> {
        val wallets: ArrayList<TrustChainBlock> = ArrayList()
        val discoveredWallets = discoverSharedWallets()
        addToWalletsList(wallets, discoveredWallets)

        val allUsers = getPeers()
        // Log.i("Coin", "Found ${allUsers.size} peers, crawling")

        for (peer in allUsers) {
            val newWallets = crawlAvailableSharedWallets(peer)
            addToWalletsList(wallets, newWallets)
        }

        return wallets
    }

    fun getUsers(): List<UserInfo> {
        return getTrustChainCommunity().database.getUsers()
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

    /**
     * Get the public key of the one that is receiving the request
     * @return string
     */
    private fun fetchSignatureRequestReceiver(block: TrustChainBlock): String {
        if (block.type == SIGNATURE_ASK_BLOCK) {
            return SWSignatureAskTransactionData(block.transaction).getData().SW_RECEIVER_PK
        }

        if (block.type == TRANSFER_FUNDS_ASK_BLOCK) {
            return SWTransferFundsAskTransactionData(block.transaction).getData().SW_RECEIVER_PK
        }

        return "invalid-pk"
    }

    fun fetchSignatureRequestProposalId(block: TrustChainBlock): String {
        if (block.type == SIGNATURE_ASK_BLOCK) {
            return SWSignatureAskTransactionData(block.transaction).getData().SW_UNIQUE_PROPOSAL_ID
        }
        if (block.type == TRANSFER_FUNDS_ASK_BLOCK) {
            return SWTransferFundsAskTransactionData(block.transaction).getData()
                .SW_UNIQUE_PROPOSAL_ID
        }

        return "invalid-proposal-id"
    }

    /**
     * Fetch all join and transfer proposals in descending timestamp order.
     * Speed assumption: each proposal has a unique proposal ID (distinct by unique proposal id,
     * without taking the unique wallet id into account).
     */
    fun fetchProposalBlocks(): List<TrustChainBlock> {
        val joinProposals = getTrustChainCommunity().database.getBlocksWithType(SIGNATURE_ASK_BLOCK)
        val transferProposals =
            getTrustChainCommunity().database.getBlocksWithType(
                TRANSFER_FUNDS_ASK_BLOCK
            )
        return joinProposals
            .union(transferProposals)
            .filter {
                fetchSignatureRequestReceiver(it) ==
                    myPeer.publicKey.keyToBin()
                        .toHex() && !checkEnoughFavorSignatures(it)
            }
            .distinctBy { fetchSignatureRequestProposalId(it) }
            .sortedByDescending { it.timestamp }
    }

    /**
     * Fetch all DAO blocks that contain a signature. These blocks are the response of a signature request.
     * Signatures are fetched from [SIGNATURE_AGREEMENT_BLOCK] type blocks.
     */
    fun fetchProposalResponses(
        walletId: String,
        proposalId: String
    ): List<SWResponseSignatureBlockTD> {
        return getTrustChainCommunity().database.getBlocksWithType(SIGNATURE_AGREEMENT_BLOCK)
            .filter {
                val blockData = SWResponseSignatureTransactionData(it.transaction)
                blockData.matchesProposal(walletId, proposalId)
            }.map {
                SWResponseSignatureTransactionData(it.transaction).getData()
            }
    }

    /**
     * Fetch all DAO blocks that contain a negative signature. These blocks are the response of a negative signature request.
     * Signatures are fetched from [SIGNATURE_AGREEMENT_NEGATIVE_BLOCK] type blocks.
     */
    fun fetchNegativeProposalResponses(
        walletId: String,
        proposalId: String
    ): List<SWResponseNegativeSignatureBlockTD> {
        return getTrustChainCommunity().database.getBlocksWithType(
            SIGNATURE_AGREEMENT_NEGATIVE_BLOCK
        )
            .filter {
                val blockData = SWResponseNegativeSignatureTransactionData(it.transaction)
                blockData.matchesProposal(walletId, proposalId)
            }.map {
                SWResponseNegativeSignatureTransactionData(it.transaction).getData()
            }
    }

    /**
     * Given a shared wallet proposal block, calculate the signature and respond with a trust chain block.
     */
    fun joinAskBlockReceived(
        block: TrustChainBlock,
        myPublicKey: ByteArray,
        votedInFavor: Boolean,
        context: SimulatedContext
    ) {
        val latestHash =
            SWSignatureAskTransactionData(block.transaction).getData()
                .SW_PREVIOUS_BLOCK_HASH
        val mostRecentSWBlock =
            fetchLatestSharedWalletBlock(latestHash.hexToBytes())
                ?: throw IllegalStateException("Most recent DAO block not found")
        val joinBlock = SWJoinBlockTransactionData(mostRecentSWBlock.transaction).getData()
        val oldTransaction = joinBlock.SW_TRANSACTION_SERIALIZED

        Log.i("SIGNING", "SIGNED")
        DAOJoinHelper.joinAskBlockReceived(oldTransaction, block, joinBlock, myPublicKey, votedInFavor, context)

        // CHECK FOR ENOUGH

        // @TODO : delete this delay
        // delay(5000)

        // @TODO : make the transaction if enough votes.
        val latestAskForSignaturesBlock =
            SWSignatureAskTransactionData(block.transaction).getData()
        var signatures: List<SWResponseSignatureBlockTD>? = collectJoinWalletResponses(latestAskForSignaturesBlock);
        if (signatures != null) {
           Log.i("SIGNING", "SIGNATURES ALIVE")
            //make the transaction
            val newMostRecentSWBlock =
            fetchLatestSharedWalletBlock(block.calculateHash())
                ?: block

            joinBitcoinWallet(
                newMostRecentSWBlock.transaction,
                latestAskForSignaturesBlock,
                signatures,
                context
            )


            // .addNewNonceKey(latestAskForSignaturesBlock.SW_UNIQUE_ID, simContext)
            // @TODO : consider add notification (I've made the transaction)
            // sendVoteFinishedMessage(peer, "Crawl result: ${crawlResult.size} proposals")
        }


     
        // getCoinCommunity().sendVoteFinishedMessage(peer, "Crawl result: ${crawlResult.size} proposals")

    }

    fun collectJoinWalletResponses(blockData: SWSignatureAskBlockTD): List<SWResponseSignatureBlockTD>? {
        val responses = fetchProposalResponses(
                blockData.SW_UNIQUE_ID,
                blockData.SW_UNIQUE_PROPOSAL_ID
            )

        Log.i(
            "Coin",
            "Waiting for signatures. ${responses.size}/${blockData.SW_SIGNATURES_REQUIRED} received!"
        )


        if (responses.size >= blockData.SW_SIGNATURES_REQUIRED-1) {
            return responses
        }
        return null
    }

    /**
     * Given a shared wallet transfer fund proposal block, calculate the signature and respond with a trust chain block.
     */
    fun transferFundsBlockReceived(
        block: TrustChainBlock,
        myPublicKey: ByteArray,
        votedInFavor: Boolean,
        context: SimulatedContext
    ) {
        val latestHash =
            SWTransferFundsAskTransactionData(block.transaction).getData()
                .SW_PREVIOUS_BLOCK_HASH
        val mostRecentSWBlock =
            fetchLatestSharedWalletBlock(latestHash.hexToBytes())
                ?: throw IllegalStateException("Most recent DAO block not found")
        val transferBlock = SWTransferDoneTransactionData(mostRecentSWBlock.transaction).getData()
        val oldTransaction = transferBlock.SW_TRANSACTION_SERIALIZED

        DAOTransferFundsHelper.transferFundsBlockReceived(
            oldTransaction,
            block,
            transferBlock,
            myPublicKey,
            votedInFavor,
            context
        )
    }

    /**
     * Given a proposal, check if the number of signatures required is met
     */
    fun checkEnoughFavorSignatures(block: TrustChainBlock): Boolean {
        if (block.type == SIGNATURE_ASK_BLOCK) {
            val data = SWSignatureAskTransactionData(block.transaction).getData()
            val signatures =
                ArrayList(
                    fetchProposalResponses(
                        data.SW_UNIQUE_ID,
                        data.SW_UNIQUE_PROPOSAL_ID
                    )
                )
            return data.SW_SIGNATURES_REQUIRED <= signatures.size
        }
        if (block.type == TRANSFER_FUNDS_ASK_BLOCK) {
            val data = SWTransferFundsAskTransactionData(block.transaction).getData()
            val signatures =
                ArrayList(
                    fetchProposalResponses(
                        data.SW_UNIQUE_ID,
                        data.SW_UNIQUE_PROPOSAL_ID
                    )
                )
            return data.SW_SIGNATURES_REQUIRED <= signatures.size
        }

        return false
    }

    /**
     * Check if the number of required votes are more than the number of possible votes minus the negative votes.
     */
    fun canWinJoinRequest(data: SWSignatureAskBlockTD): Boolean {
        val sw =
            discoverSharedWallets().filter { b -> SWJoinBlockTransactionData(b.transaction).getData().SW_UNIQUE_ID == data.SW_UNIQUE_ID }[0]
        val swData = SWJoinBlockTransactionData(sw.transaction).getData()
        val againstSignatures =
            ArrayList(
                fetchNegativeProposalResponses(
                    data.SW_UNIQUE_ID,
                    data.SW_UNIQUE_PROPOSAL_ID
                )
            )
        val totalVoters = swData.SW_BITCOIN_PKS
        val requiredVotes = data.SW_SIGNATURES_REQUIRED

        return requiredVotes <= totalVoters.size - againstSignatures.size
    }

    /**
     * Check if the number of required votes are more than the number of possible votes minus the negative votes.
     */
    fun canWinTransferRequest(data: SWTransferFundsAskBlockTD): Boolean {
        val againstSignatures =
            ArrayList(
                fetchNegativeProposalResponses(
                    data.SW_UNIQUE_ID,
                    data.SW_UNIQUE_PROPOSAL_ID
                )
            )
        val totalVoters = data.SW_BITCOIN_PKS
        val requiredVotes = data.SW_SIGNATURES_REQUIRED

        return requiredVotes <= totalVoters.size - againstSignatures.size
    }

    object MessageId {
        const val MESSAGE_ID = 1
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

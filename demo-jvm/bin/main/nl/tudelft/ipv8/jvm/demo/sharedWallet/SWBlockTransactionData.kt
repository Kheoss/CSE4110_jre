package nl.tudelft.ipv8.jvm.demo.sharedWallet

import com.google.gson.JsonObject
import nl.tudelft.ipv8.attestation.trustchain.TrustChainTransaction

abstract class SWBlockTransactionData(var jsonData: JsonObject, val blockType: String) {
    fun getJsonString(): String {
        return jsonData.toString()
    }

    fun getTransactionData(): TrustChainTransaction {
        return mapOf("message" to getJsonString())
    }
}

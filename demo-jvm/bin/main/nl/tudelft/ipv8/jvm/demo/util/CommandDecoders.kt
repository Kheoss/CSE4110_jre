package nl.tudelft.ipv8.jvm.demo.util

import com.beust.klaxon.Json

enum class Operation(val op: Int){
    PRINT_ALL_WALLETS(1),
    PRINT_USER_COUNT(2),
    CREATE_DAO(3),
    SEND_NEW_DAO_ID(4),
    JOIN_WALLET(5),
    NOTIFICATION(6),
    START_SIMULATION(7),
    SYNC_COMPLETE(8);
    companion object {
        fun fromInt(value: Int) = entries.first { it.op == value }
    }
}
data class Message(
    @Json(name = "operation") val operation: Int,
    @Json(name = "params") val params: String)

data class ParamsDAOIdResponse(val id: String)

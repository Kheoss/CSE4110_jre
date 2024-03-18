package nl.tudelft.ipv8.jvm.demo

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import nl.tudelft.ipv8.*
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import nl.tudelft.ipv8.attestation.trustchain.TrustChainSettings
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainSQLiteStore
import nl.tudelft.ipv8.keyvault.PrivateKey
import nl.tudelft.ipv8.messaging.EndpointAggregator
import nl.tudelft.ipv8.messaging.udp.UdpEndpoint
import nl.tudelft.ipv8.peerdiscovery.DiscoveryCommunity
import nl.tudelft.ipv8.peerdiscovery.Network
import nl.tudelft.ipv8.peerdiscovery.strategy.PeriodicSimilarity
import nl.tudelft.ipv8.peerdiscovery.strategy.RandomChurn
import nl.tudelft.ipv8.peerdiscovery.strategy.RandomWalk
import nl.tudelft.ipv8.sqldelight.Database
import java.net.InetAddress

object IPv8Network {
    private var ipv8: IPv8? = null

    fun getInstance(): IPv8 {
        return ipv8 ?: throw IllegalStateException("IPv8 is not initialized")
    }

    class Factory {
        private var privateKey: PrivateKey? = null

        fun setPrivateKey(key: PrivateKey): Factory {
            this.privateKey = key
            return this
        }

        fun init(): IPv8 {
            val ipv8 = create()

            if (!ipv8.isStarted()) {
                ipv8.start()
            }

            IPv8Network.ipv8 = ipv8


            return ipv8
        }

        private fun createDiscoveryCommunity(): OverlayConfiguration<DiscoveryCommunity> {
            val randomWalk = RandomWalk.Factory(timeout = 3.0, peers = 20)
            val randomChurn = RandomChurn.Factory()
            val periodicSimilarity = PeriodicSimilarity.Factory()
            return OverlayConfiguration(
                DiscoveryCommunity.Factory(),
                listOf(randomWalk, randomChurn, periodicSimilarity)
            )
        }

        private fun createTrustChainCommunity(): OverlayConfiguration<TrustChainCommunity> {
            val settings = TrustChainSettings()
            val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            Database.Schema.create(driver)
            val database = Database(driver)
            val store = TrustChainSQLiteStore(database)
            val randomWalk = RandomWalk.Factory(timeout = 3.0, peers = 20)
            return OverlayConfiguration(
                TrustChainCommunity.Factory(settings, store),
                listOf(randomWalk)
            )
        }

        private fun createCoinCommunity(): OverlayConfiguration<CoinCommunity> {
            val randomWalk = RandomWalk.Factory()

            return OverlayConfiguration(
                Overlay.Factory(CoinCommunity::class.java),
                listOf(randomWalk)
            )
        }

        private fun create(): IPv8 {
            val privateKey = privateKey ?: throw IllegalStateException("Private key is not set")

            val config = IPv8Configuration(
                overlays = listOf(
                    createDiscoveryCommunity(),
                    createTrustChainCommunity(),
                    createCoinCommunity()
                ), walkerInterval = 1.0
            )

            val myPeer = Peer(privateKey);
            val udpEndpoint = UdpEndpoint(8090, InetAddress.getByName("0.0.0.0"))
            val network = Network()

            return IPv8(EndpointAggregator(udpEndpoint, null), config, myPeer, network);

        }
    }
}

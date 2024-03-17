import express from "express";
import clients from "../clients_tracker/clientsList.js";
import operations from "./operations.js";

const router = express.Router()

function getClients(req, res) {
    res.send(clients.map(x => {
        return {
            id: x.id,
            wallet: x.wallet,
            joinedWallets: x.joinedWallets
        }
    }));
}

function postCreateWallet(req, res) {
    const client = clients[req.query.clientId];
    client.send(operations.CREATE_DAO, {});
    res.sendStatus(200);
}

function postPrintWallets(req, res) {
    const client = clients[req.query.clientId];
    client.send(operations.PRINT_ALL_WALLETS, {});
    res.sendStatus(200);
}

function postPrintPeers(req, res) {
    const client = clients[req.query.clientId];
    client.send(operations.PRINT_USER_COUNT, {});
    res.sendStatus(200);
}

function postJoinWallet(req, res) {
    const client = clients[req.query.clientId];
}

router.get("/clients", getClients);
router.post("/printWallets", postPrintWallets);
router.post("/printPeers", postPrintPeers);
router.post("/createWallet", postCreateWallet);
router.post("/joinWallet", postJoinWallet)

export default router;
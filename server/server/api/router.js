import express from "express";
import { clients } from "../clients_tracker/clientsList.js";
import operations from "./operations.js";

const router = express.Router();

function getClients(req, res) {
  res.send(
    clients.map((x) => {
      return {
        id: x.id,
        wallet: x.wallet,
        joinedWallets: x.joinedWallets,
      };
    })
  );
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
  const walletId = req.body.walletId;
  client.send(operations.JOIN_WALLET, { id: walletId });
  res.sendStatus(200);
}

function postAllJoin(req, res) {
  // const client = clients[req.query.clientId];
  const walletId = req.body.walletId;
  console.log(walletId);

  for (var i = 0; i < clients.length; i++) {
    if (clients[i].wallet.includes(walletId)) {
      clients[i].last_join_time = 0;
      continue;
    }

    clients[i].timer = Date.now();
    clients[i].send(operations.JOIN_WALLET, { id: walletId });
  }
  // client.send(operations.JOIN_WALLET, {id: walletId});
  res.sendStatus(200);
}

function getTimes(req, res) {
  res.send(
    clients.map((x) => {
      return {
        id: x.id,
        time: x.last_join_time,
      };
    })
  );
}

const startSimulation = () => {};

router.get("/startSimulation", startSimulation);
router.get("/clients", getClients);
router.get("/times", getTimes);
router.post("/printWallets", postPrintWallets);
router.post("/printPeers", postPrintPeers);
router.post("/createWallet", postCreateWallet);
router.post("/joinWallet", postJoinWallet);
router.post("/allJoin", postAllJoin);

export default router;

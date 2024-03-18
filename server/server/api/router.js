import express from "express";
import clients from "../clients_tracker/clientsList.js";
import operations from "./operations.js";

const router = express.Router();
router.use(express.urlencoded());

// Parse JSON bodies (as sent by API clients)
router.use(express.json());
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

function postJoinWalletProposal(req, res) {
  const client = clients[req.query.clientId];
  client.send(operations.SEND_JOIN_PROPOSAL, { daoId: req.body.walletId });
  res.sendStatus(200);
}

router.get("/clients", getClients);
router.post("/printWallets", postPrintWallets);
router.post("/printPeers", postPrintPeers);
router.post("/createWallet", postCreateWallet);
router.post("/sendJoinProposalWallet", postJoinWalletProposal);

export default router;

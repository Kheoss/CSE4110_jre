import { WebSocketServer } from "ws";
import Client from "../clients_tracker/client.js";
import { clients, wallets } from "../clients_tracker/clientsList.js";
import operations from "../api/operations.js";
import * as readline from "readline";
import TableManager from "./tableManager.js";
import { table } from "console";

const PEERS_ON_TRIAL = 2;
let walletsToBeCreated = parseInt(PEERS_ON_TRIAL / 2);
let peerToJoinWallet = 0;

function askQuestion(query) {
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  return new Promise((resolve) =>
    rl.question(query, (ans) => {
      rl.close();
      resolve(ans);
    })
  );
}

const wss = new WebSocketServer({ port: 7071 });

const tableManager = new TableManager(PEERS_ON_TRIAL);

tableManager.start(1000);
function shuffleArray(array) {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
}

function selectHalfRandomly(arr) {
  shuffleArray(arr);
  return arr.slice(0, arr.length / 2);
}

let timer;
let otherClients = [];
let walletId = "";

let peerToReceiveNotification = (PEERS_ON_TRIAL - 1) * PEERS_ON_TRIAL;

let individualTimer;
const startSimulation = async () => {
  // chose half of the clients to generate wallets.
  const ans = await askQuestion("Are you sure you want to START the simulation? ");
  timer = Date.now();
  const clientsToGenerateWallets = selectHalfRandomly(clients);

  for (let client of clients) {
    client.send(operations.CREATE_DAO, { id: client.id.toString() });
    break;
  }
};

const nextClientJoin = () => {
  peerToJoinWallet++;
  const nextClient = otherClients[Math.floor(Math.random() * otherClients.length)];
  individualTimer = Date.now();
  nextClient.send(operations.JOIN_WALLET, { id: walletId });
  otherClients = otherClients.filter((x) => x.id != nextClient.id);
};

const newDaoCreated = (client) => {
  // chose half of the clients to join my wallet
  tableManager.writeToLog("WALLET CREATED BY: " + client.id);
  tableManager.setPeerJoinedWallet(client.id);
  walletsToBeCreated--;

  otherClients = clients.filter((x) => x.id != client.id);

  // make all other guys async
  for (let otherClient of otherClients) {
    tableManager.setPeerDesynced(otherClient.id);
  }
  walletId = client.wallet[0].id;
  nextClientJoin();
  //   peerToJoinWallet += otherClients.length;
  //   for (let otherClient of otherClients) {
  //     otherClient.send(operations.JOIN_WALLET, { id: walletId });
  //   }
};

const receivePing = (client) => {
  peerToReceiveNotification--;
  if (peerToReceiveNotification == 0) {
    console.log("TIME ON TRIAL: " + (Date.now() - timer));
  }
};

const onJoinSucceed = (client) => {
  peerToJoinWallet--;
  tableManager.writeToLog("[JOINED] CLIENT " + client.id);
  tableManager.writeToLog("TIME TO JOIN: " + (individualTimer - Date.now()));

  otherClients = clients.filter((x) => x.id != client.id);

  // make all other guys async
  for (let otherClient of otherClients) {
    tableManager.setPeerDesynced(otherClient.id);
  }
  //   if (peerToJoinWallet == 0 && walletsToBeCreated == 0) {
  // if (otherClients.length == 0) {
  //   tableManager.writeToLog("SIMULATION ENDED");
  //   tableManager.writeToLog("SIMULATION TIME:" + (Date.now() - timer));
  //   return;
  // }

  // nextClientJoin();
};
const onSync = (client) => {
  tableManager.setPeerSynced(client.id);
};
wss.on("connection", (ws) => {
  console.log("connected");
  const client = new Client(
    clients.length,
    ws,
    newDaoCreated,
    onJoinSucceed,
    receivePing,
    onSync
  );
  clients.push(client);

  tableManager.setPeerActive(client.id);

  if (clients.length == PEERS_ON_TRIAL) {
    // start simulation
    // console.log("START?");
    // setTimeout(() => {
    startSimulation();
    // }, 1000);
  }

  ws.on("message", function message(data) {
    // console.log(data);
    client.interpret(data);
  });

  ws.on("close", function close() {
    clients.splice(client.id, 1);

    tableManager.setPeerInactive(client.id);
    tableManager.writeToLog("closed");
  });
});

export default wss;

import { WebSocketServer } from "ws";
import Client from "../clients_tracker/client.js";
import { clients, wallets } from "../clients_tracker/clientsList.js";
import operations from "../api/operations.js";
import * as readline from "readline";
import TableManager from "./tableManager.js";
import { table } from "console";

const PEERS_ON_TRIAL = 2;

let walletCreated = false;

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

let individualTimer;
const startSimulation = async () => {
  clients.sort(function (a, b) {
    return a.id - b.id;
  });
  const ans = await askQuestion("Are you sure you want to START the simulation? ");
  timer = Date.now();
  const clientsToGenerateWallets = selectHalfRandomly(clients);

  // for (let client of clients) {
  clients[0].send(operations.CREATE_DAO, { id: clients[0].id.toString() });
  // break;
  // }
};

const nextClientJoin = () => {
  tableManager.writeToLog("SUNTEM AICI");
  if (otherClients.length == 0) {
    tableManager.writeToLog("DONE");
    return;
  }

  const nextClient = otherClients[0];

  tableManager.writeToLog("TO JOIN: " + nextClient.id);

  otherClients = otherClients.filter((x) => x.id != nextClient.id);
  nextClient.send(operations.JOIN_WALLET, { id: walletId });
};

const newDaoCreated = async (client) => {
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

  // block untill all peers before client are sync
  // let x = setInterval(() => {}, 1);
  walletCreated = true;
  // let x = setInterval(() => {
  //   if (tableManager.arePeerBeforeSync(client.id)) {
  //     tableManager.writeToLog("ALL SYNC: ");
  //     // nextClientJoin();
  //     clearInterval(x);
  //   }
  // }, 1);

  // peerToJoinWallet += otherClients.length;
  // for (let otherClient of otherClients) {
  //   otherClient.send(operations.JOIN_WALLET, { id: walletId });
  // }
};

let isFirst = false;
const receivePing = (client) => {
  tableManager.writeToLog("a dat notificare: " + client.id);
  // tableManager.writeToLog("NOTIFICATION FROM " + client.id);
  // peerToReceiveNotification--;
  // if (peerToReceiveNotification == 0) {
  //   console.log("TIME ON TRIAL: " + (Date.now() - timer));
  // tableManager.writeToLog("BA PULAAAA :" + client.id);
  tableManager.setPeerSynced(client.id);
  if (walletCreated && !isFirst) {
    if (tableManager.arePeerBeforeSync(client.id)) {
      tableManager.writeToLog("ALL SYNC: ");
      isFirst = true;
      // setTimeout(() => {
      nextClientJoin();
      // }, 10000);
    }
  }
  // }
};

const onJoinSucceed = async (client) => {
  peerToJoinWallet--;
  tableManager.writeToLog("[JOINED] CLIENT " + client.id);
  // tableManager.writeToLog("TIME TO JOIN: " + (individualTimer - Date.now()));
  tableManager.setPeerJoinedWallet(client.id);
  otherClients = clients.filter((x) => x.id != client.id);

  // make all other guys async
  for (let otherClient of otherClients) {
    tableManager.setPeerDesynced(otherClient.id);
  }
  // let y = setInterval(() => {
  //   if (tableManager.arePeerBeforeSync(client.id)) {
  //     tableManager.writeToLog("ALL SYNC: ");
  //     nextClientJoin();
  //     clearInterval(y);
  //   }
  // }, 1);
};
const onSync = (client) => {
  tableManager.writeToLog("BA PULAAAA :" + client.id);
  tableManager.setPeerSynced(client.id);
  if (walletCreated) {
    if (tableManager.arePeerBeforeSync(client.id)) {
      tableManager.writeToLog("ALL SYNC: ");
      // nextClientJoin();
    }
  }
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

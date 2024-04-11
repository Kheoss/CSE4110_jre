import { WebSocketServer } from "ws";
import Client from "../clients_tracker/client.js";
import { clients, wallets } from "../clients_tracker/clientsList.js";
import operations from "../api/operations.js";
import * as readline from "readline";
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

/**
 * ALL JOIN ALL WALLETS:
 * 6-> 57150
 *
 * ALL JOIN ONE WALLET:
 *
 * 6 -> 63069
 *
 *
 * ALL JOIN ONE WALLET - NEW METHOD
 *
 * 6 -> 4089
 */

/**
 * Message transfer without gossip:
 * 2  peers: 672ms
 * 5  peers: 1477ms | 156KB peek
 * 10 peers: 3666ms | 545KB peek
 * 15 peers: 6320ms | 1.22MB  = 1220 KB peek
 * 17 peers: crash
 *
 * Message transfer with gossip:
 *
 * 5  peers: 1465ms | 74kb peek
 * 10 peers: 3213ms | 135KB peek
 * 15 peers: 5178ms | 228KB peek
 * 17 peers: crash
 *
 *
 * IDEAS FOR SHARDING:
 * Separate the blockchain into 4 parts: proposals/votes for joining, joining,
 * Achiving system.
 */

const wss = new WebSocketServer({ port: 7071 });

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
const PEERS_ON_TRIAL = 100;
let walletsToBeCreated = parseInt(PEERS_ON_TRIAL / 2);
let peerToJoinWallet = 0;

let timer;
let otherClients = [];
let walletId = "";

let peerToReceiveNotification = (PEERS_ON_TRIAL - 1) * PEERS_ON_TRIAL;

let individualTimer;
const startSimulation = async () => {
  // chose half of the clients to generate wallets.
  const ans = await askQuestion("Are you sure you want to START the simulation? ");
  console.log("START SIMULATION");
  timer = Date.now();
  const clientsToGenerateWallets = selectHalfRandomly(clients);

  console.log("SEND NOTIFICATION TO ALL CLIENTS: " + clients.length);
  for (let client of clients) {
    client.send(operations.START_SIMULATION, { id: client.id.toString() });
    // break;
  }
};

const nextClientJoin = () => {
  peerToJoinWallet++;
  console.log(otherClients.length);
  console.log(Math.floor(Math.random() * otherClients.length));
  const nextClient = otherClients[Math.floor(Math.random() * otherClients.length)];
  individualTimer = Date.now();
  nextClient.send(operations.JOIN_WALLET, { id: walletId });
  otherClients = otherClients.filter((x) => x.id != nextClient.id);
};

const newDaoCreated = (client) => {
  // chose half of the clients to join my wallet
  console.log("WALLET CREATED BY: " + client.id);
  walletsToBeCreated--;

  otherClients = clients.filter((x) => x.id != client.id);

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
  //   console.log("wallets to be created:" + walletsToBeCreated);
  //   console.log("peers to join: " + peerToJoinWallet);
  peerToJoinWallet--;
  console.log("[JOINED] CLIENT " + client.id);
  console.log("TIME TO JOIN: " + (individualTimer - Date.now()));

  //   if (peerToJoinWallet == 0 && walletsToBeCreated == 0) {
  if (otherClients.length == 0) {
    console.log("SIMULATION ENDED");
    console.log("SIMULATION TIME:" + (Date.now() - timer));
    return;
  }

  nextClientJoin();
};

wss.on("connection", (ws) => {
  console.log("connected");
  const client = new Client(
    clients.length,
    ws,
    newDaoCreated,
    onJoinSucceed,
    receivePing
  );
  clients.push(client);

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
    console.log("closed");
  });
});

export default wss;

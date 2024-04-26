import { WebSocketServer } from "ws";
import Client from "../clients_tracker/client.js";
import { clients, wallets } from "../clients_tracker/clientsList.js";
import operations from "../api/operations.js";
import * as readline from "readline";
import TableManager from "./tableManager.js";

const PEERS_ON_TRIAL = 5;

let peerToJoinWallet = 0;

let clientKnowledge = [];

let roundOnGoing = false;
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

let timer;
let otherClients = [];
let walletId = "";

const getMaximumKnowledge = () => {
  return Math.max(...clientKnowledge);
};

const startSimulation = async () => {
  clients.sort(function (a, b) {
    return b.id - a.id;
  });

  for (let client of clients) {
    clientKnowledge.push(0);
  }

  const ans = await askQuestion("Are you sure you want to START the simulation? ");
  timer = Date.now();

  clients[0].send(operations.CREATE_DAO, { id: clients[0].id.toString() });
};

const nextClientJoin = () => {
  // tableManager.writeToLog("SUNTEM AICI");
  if (otherClients.length == 0) {
    // tableManager.writeToLog("DONE");
    return;
  }

  const nextClient = otherClients[0];

  tableManager.writeToLog("TO JOIN: " + nextClient.id);

  otherClients = otherClients.filter((x) => x.id != nextClient.id);
  roundOnGoing = true;
  timer = Date.now();
  nextClient.send(operations.JOIN_WALLET, { id: walletId });
};

const updateSyncOfClients = () => {
  const maximumKnowledge = getMaximumKnowledge();
  tableManager.writeToLog(clientKnowledge);
  for (let client of clients) {
    if (clientKnowledge[client.id] < maximumKnowledge)
      tableManager.setPeerDesynced(client.id);
    else tableManager.setPeerSynced(client.id);
  }

  if (tableManager.arePeerBeforeSync() && !roundOnGoing) {
    // tableManager.writeToLog("ALL SYNC: ");
    nextClientJoin();
  }
};
const newDaoCreated = async (client) => {
  tableManager.writeToLog("Peer created DAO in " + (Date.now() - timer) + "ms");
  roundOnGoing = false;

  tableManager.writeToLog("WALLET CREATED BY: " + client.id);

  updateSyncOfClients();

  tableManager.setPeerJoinedWallet(client.id);

  otherClients = clients.filter((x) => x.id != client.id);

  walletId = client.wallet[0].id;
};

const receivePing = (client, knowledge) => {
  tableManager.writeToLog("client: " + client.id + "->" + knowledge);
  clientKnowledge[client.id] = knowledge;
  updateSyncOfClients();
};

const onJoinSucceed = async (client) => {
  tableManager.writeToLog(
    "Peer joined a DAO of size " +
      (PEERS_ON_TRIAL - otherClients.length) +
      " in " +
      (Date.now() - timer) +
      "ms"
  );
  roundOnGoing = false;

  peerToJoinWallet--;
  tableManager.writeToLog("[JOINED] CLIENT " + client.id);
  tableManager.setPeerJoinedWallet(client.id);
  otherClients = clients.filter((x) => x.id != client.id);

  updateSyncOfClients();
};
const onSync = (client) => {
  tableManager.writeToLog("BA PULAAAA :" + client.id);
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
    startSimulation();
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

import { WebSocketServer } from "ws";
import Client from "../clients_tracker/client.js";
import { clients } from "../clients_tracker/clientsList.js";
import operations from "../api/operations.js";
import * as readline from "readline";
import TableManager from "./tableManager.js";

const getArgs = () =>
  process.argv.reduce((args, arg) => {
    // long arg
    if (arg.slice(0, 2) === "--") {
      const longArg = arg.split("=");
      const longArgFlag = longArg[0].slice(2);
      const longArgValue = longArg.length > 1 ? longArg[1] : true;
      args[longArgFlag] = longArgValue;
    }
    // flags
    else if (arg[0] === "-") {
      const flags = arg.slice(1).split("");
      flags.forEach((flag) => {
        args[flag] = true;
      });
    }
    return args;
  }, {});

const args = getArgs();

let setupNotifications = 0;
const PEERS_ON_TRIAL = args.peers;
let clientKnowledge = [];

let roundOnGoing = false;
function blockUntillKeyboard(query) {
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

if (!args.peers) {
  console.log("INCORRECT FORMAT, PLEASE SPECIFIY --peers");
} else {
  tableManager.start(1000);
}

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

  tableManager.writeToLog("Simulation started");
  // const ans = await blockUntillKeyboard("");
  timer = Date.now();

  otherClients = clients.filter((x) => x.id != clients[0].id);
  clients[0].send(operations.CREATE_DAO, { id: clients[0].id.toString() });
};

const nextClientJoin = () => {
  if (otherClients.length == 0) {
    roundOnGoing = true;
    tableManager.writeToLog("FINISHED");
    return;
  }

  const nextClient = otherClients[0];

  tableManager.writeToLog("TO JOIN: " + nextClient.id);

  otherClients = otherClients.filter((x) => x.id != nextClient.id);
  roundOnGoing = true;
  // setTimeout(() => {
  timer = Date.now();
  nextClient.send(operations.JOIN_WALLET, { id: walletId });
  // }, 5000);
};

const updateSyncOfClients = () => {
  const maximumKnowledge = getMaximumKnowledge();

  for (let client of clients) {
    if (clientKnowledge[client.id] < maximumKnowledge)
      tableManager.setPeerDesynced(client.id);
    else tableManager.setPeerSynced(client.id);
  }

  if (tableManager.arePeerBeforeSync() && !roundOnGoing) {
    nextClientJoin();
  }
};
const newDaoCreated = async (client) => {
  tableManager.writeToLog("Peer created DAO in " + (Date.now() - timer) + "ms");
  roundOnGoing = false;

  tableManager.writeToLog("WALLET CREATED BY: " + client.id);
  for (let i = 0; i < clientKnowledge.length; i++) {
    clientKnowledge[i] = getMaximumKnowledge();
  }

  updateSyncOfClients();

  tableManager.setPeerJoinedWallet(client.id);

  otherClients = clients.filter((x) => x.id != client.id);

  walletId = client.wallet[0].id;
};

const receivePing = (client, knowledge) => {
  // tableManager.writeToLog("client: " + client.id + "->" + knowledge);
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

  tableManager.writeToLog("[JOINED] CLIENT " + client.id);
  tableManager.setPeerJoinedWallet(client.id);
  otherClients = otherClients.filter((x) => x.id != client.id);

  for (let i = 0; i < clientKnowledge.length; i++) {
    clientKnowledge[i] = getMaximumKnowledge();
  }
  updateSyncOfClients();
};
const onSync = (client) => {
  tableManager.setPeerSynced(client.id);
  setupNotifications++;
  if (setupNotifications == PEERS_ON_TRIAL * 2) {
    startSimulation();
  }
};
wss.on("connection", (ws) => {
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

  ws.on("message", function message(data) {
    client.interpret(data);
  });

  ws.on("close", function close() {
    clients.splice(client.id, 1);

    tableManager.setPeerInactive(client.id);
    tableManager.writeToLog("closed");
  });
});

export default wss;

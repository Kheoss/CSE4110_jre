import operations from "../api/operations.js";
import Wallet from "../clients_tracker/wallet.js";
class Client {
  id;
  ws;
  wallet;
  joinedWallets;
  timer;
  last_join_time;
  constructor(id, ws, newDaoCreated, onJoinSucceed, receivePing, onSync, onReceiveBalance) {
    this.id = id;
    this.ws = ws;
    this.wallet = [];
    this.joinedWallets = [];
    this.timer = Date.now();
    this.last_join_time = 0;
    this.newDaoCreated = newDaoCreated;
    this.onJoinSucceed = onJoinSucceed;
    this.receivePing = receivePing;
    this.onSync = onSync;
    this.balance = 0;
    this.onReceiveBalance = onReceiveBalance;
  }

  send(op, msg) {
    msg = JSON.stringify(msg);

    console.log("SENT: ", msg);
    this.ws.send(
      JSON.stringify({
        operation: op,
        params: msg,
      })
    );
  }

  interpret(data) {
    try {
      const msg = JSON.parse(data);

      switch (msg.operation) {
        case operations.SEND_NEW_DAO_ID:
          const params = JSON.parse(msg.params);
          this.wallet.push(new Wallet(params.id));
          console.log("WALLET CREATED");
          this.newDaoCreated(this);
          break;
        case operations.JOIN_WALLET:
          const join_params = JSON.parse(msg.params);
          this.joinedWallets.push(join_params.id);
          this.last_join_time = Date.now() - this.timer;
          this.onJoinSucceed(this);
          break;

        case operations.NOTIFICATION:
          const notificationParams = JSON.parse(msg.params);
          this.receivePing(this);
          break;

        case operations.SYNC_COMPLETE:
          this.onSync(this);
          break;
        case operations.BALANCE:
          const balanceParams = JSON.parse(msg.params);
          this.balance = balanceParams.balance;
          this.onReceiveBalance(this, this.balance);
          console.log("BALANCE: ", this.balance);
          break;
      }
    } catch (error) {
      console.log(error);
    }
  }
}

export default Client;

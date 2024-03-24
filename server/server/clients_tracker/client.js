import operations from "../api/operations.js";
import Wallet from "../clients_tracker/wallet.js";
class Client {
  id;
  ws;
  wallet;
  joinedWallets;
  timer;
  last_join_time;
  constructor(id, ws, newDaoCreated, onJoinSucceed) {
    this.id = id;
    this.ws = ws;
    this.wallet = [];
    this.joinedWallets = [];
    this.timer = Date.now();
    this.last_join_time = 0;
    this.newDaoCreated = newDaoCreated;
    this.onJoinSucceed = onJoinSucceed;
  }

  send(op, msg) {
    msg = JSON.stringify(msg);

    console.log("SENT: ", msg)
    this.ws.send(
      JSON.stringify({
        operation: op,
        params: msg,
      })
    );
  }

  interpret(data) {
    console.log("received: %s", data);

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
      }
    } catch (error) {
      console.log(error);
    }
  }
}

export default Client;

import operations from "../api/operations.js";

class Client {
  id;
  ws;
  wallet;
  joinedWallets;
  constructor(id, ws) {
    this.id = id;
    this.ws = ws;
    this.wallet = [];
    this.joinedWallets = [];
  }

  send(op, msg) {
    console.log("here");
    msg = JSON.stringify(msg);
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
          this.wallet = params.ids;
          break;
      }
    } catch (error) {
      console.log(error);
    }
  }
}

export default Client;

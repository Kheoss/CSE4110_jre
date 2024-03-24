import { WebSocketServer } from "ws";
import app from "./api/app.js";
import { clients, wallets } from "./clients_tracker/clientsList.js";
import Client from "./clients_tracker/client.js";
import wss from "./web_socket_server/server.js";

// var stdin = process.openStdin();

// stdin.addListener("data", function(d) {
//     // note:  d is an object, and when converted to a string it will
//     // end with a linefeed.  so we (rather crudely) account for that
//     // with toString() and then trim()

//     const [idx, cmd] = d.toString().split(" ").map(x => parseInt(x));
//     console.log(idx);
//     console.log(cmd);

//     idxToClient[idx].send(cmd);

//     console.log("you entered: [" +
//         d.toString().trim() + "]");
// });

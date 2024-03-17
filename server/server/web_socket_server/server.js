import { WebSocketServer } from "ws";
import Client from "../clients_tracker/client.js";
import clients from "../clients_tracker/clientsList.js";

const wss = new WebSocketServer({port: 7071});

wss.on("connection", (ws) => {
    const client = new Client(clients.length, ws);
    clients.push(client)

    ws.on('message', function message(data) {
        client.interpret(data)
    });

    ws.on('close', function close() {
        clients.splice(client.id, 1);
        console.log("closed")
    })
    console.log("connected")
})

export default wss;
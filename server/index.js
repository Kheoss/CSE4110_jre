import { WebSocketServer } from "ws";



const wss = new WebSocketServer({port: 7071})

const clients = {}
const daos = {}
const idxToClient = []

var id = 0;


wss.on("connection", (ws) => {

    idxToClient.push(ws)
    clients[ws] = idxToClient.length - 1;

    ws.on('message', function message(data) {
        console.log('received: %s', data);

        const [msg, payload] = data.toString().split(" ")

        switch(msg) {
            case "0":
                daos[payload] = []
                daos[payload].push(ws)
                break;
            
        }
    });

    ws.on('close', function close() {
        console.log("closed")
    })

    // ws.send("hallo");
    console.log("connected")

    ws.send("AAA")
})

var stdin = process.openStdin();

stdin.addListener("data", function(d) {
    // note:  d is an object, and when converted to a string it will
    // end with a linefeed.  so we (rather crudely) account for that  
    // with toString() and then trim() 

    const [idx, cmd] = d.toString().split(" ").map(x => parseInt(x));
    console.log(idx);
    console.log(cmd);

    idxToClient[idx].send(cmd);

    console.log("you entered: [" + 
        d.toString().trim() + "]");
});

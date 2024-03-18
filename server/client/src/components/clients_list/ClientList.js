import Client from "../client/Client.js";
import "./ClientList.css";
import React, { useEffect, useState } from "react";

function ClientList() {
  const [clients, setClients] = useState(null);

  useEffect(() => {
    async function getData() {
      console.log("here");
      const data = await fetch("/api/clients?clientId=").then((response) =>
        response.json()
      );
      setClients(data);
      console.log(data);
    }

    getData();
  }, []);

  return (
    <div className="container">
      {/* <Client id={3} wallets={["1", "2"]} /> */}
      {clients &&
        clients.map((x) => (
          <Client
            id={x.id}
            wallets={x.wallet}
            key={x.id}
            joinedWallets={x.joinedWallets}
          />
        ))}
    </div>
  );
}

export default ClientList;

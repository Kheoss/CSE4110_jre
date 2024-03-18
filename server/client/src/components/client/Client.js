import { useState } from "react";
import "./Client.css";

function Client({ id, wallets, joinedWallets }) {
  const [joinText, setJoinText] = useState("");

  function printUsers() {
    const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
    };
    fetch("/api/printPeers?clientId=" + id, requestOptions);
  }

  function printWallets() {
    const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
    };
    fetch("/api/printWallets?clientId=" + id, requestOptions);
  }

  function createWallet() {
    const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
    };
    fetch("/api/createWallet?clientId=" + id, requestOptions);
  }

  function joinWallet() {
    const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ walletId: joinText }),
    };
    fetch("/api/joinWallet?clientId=" + id, requestOptions);

    setJoinText("");
  }

  return (
    <div className="container-tile">
      <span>Client {id}</span>

      <div className="wallets">
        <span>Wallets</span>
        {wallets.map((x) => (
          <span key={x}>{x}</span>
        ))}
      </div>

      <div className="wallets">
        <span>Joined wallets</span>
        {joinedWallets.map((x) => (
          <span key={x}>{x}</span>
        ))}
      </div>
      <div>
        <button onClick={() => printUsers()}>Print users</button>
        <button onClick={() => printWallets()}>Print wallets</button>

        <button onClick={() => createWallet()}>Create wallet</button>
      </div>

      <div>
        <input value={joinText} onChange={(e) => setJoinText(e.target.value)} />
        <button onClick={() => joinWallet()}> Join </button>
      </div>
    </div>
  );
}

export default Client;

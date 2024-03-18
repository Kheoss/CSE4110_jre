import logo from './logo.svg';
import './App.css';
import ClientList from './components/clients_list/ClientList.js';
import { useState } from 'react';

function App() {
  const [joinText, setJoinText] = useState("");

  function joinWallet() {
    const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ walletId: joinText }),
    };
    fetch("/api/allJoin", requestOptions);

    setJoinText("");
  }

  return (
    <div className="App">
      {/* <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
      </header> */}
      <div style={{marginTop: "15px"}}>
        <input value={joinText} onChange={(e) => setJoinText(e.target.value)} />
        <button onClick={() => joinWallet()}> Join </button>
      </div>
      <ClientList/>
    </div>
  );
}

export default App;

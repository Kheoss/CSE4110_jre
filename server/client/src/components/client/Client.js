import { useState } from 'react';
import './Client.css';


function Client({
    id,
    wallets
}) {

    const [joinText, setJoinText] = useState("");

    function printUsers() {
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        };
        fetch('/api/printPeers?clientId=' + id, requestOptions)
            .then(response => response.json())
    }

    function printWallets() {
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        };
        fetch('/api/printWallets?clientId=' + id, requestOptions)
            .then(response => response.json())
    }

    function createWallet() {
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        };
        fetch('/api/createWallet?clientId=' + id, requestOptions)
            .then(response => response.json())
    }

    function joinWallet() {
        console.log("join");
        setJoinText("");
    }

    return (
        <div className="container-tile">
            <span>
                Client {id}
            </span>

            <div className="wallets">
                <span>
                    Wallets
                </span>
                {wallets.map(x => 
                    <span key = {x}>
                        {x}
                    </span>
                )}
            </div>
                    
            <div>
                <button onClick = {() => printUsers()}>
                    Print users
                </button>
                <button onClick = {() => printWallets()}>
                    Print wallets
                </button>

                <button onClick = {() => createWallet()}>
                    Create wallet
                </button>
            </div>

            <div>
                <input value = {joinText} onChange={e => setJoinText(e.target.value)} />
                <button onClick = {() => joinWallet()}> Join </button>
            </div>
        </div>
    )
}

export default Client;
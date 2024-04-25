import Table from "cli-table3";
import chalk from "chalk";

export default class TableManager {
  // Initialize the table with headers and column widths
  constructor(numberOfInstances) {
    this.log = [];

    this.numberOfInstances = numberOfInstances;
    let header = ["Processes"];
    let headerSize = [20];
    for (let i = 0; i < numberOfInstances; i++) {
      header.push(chalk.blue("Process " + i));
      headerSize.push(20);
    }
    this.table = new Table({
      head: header,
      colWidths: headerSize,
    });
    this.interval = null;

    this.initiateActivityMatrix();
  }

  startSim() {
    this.activityMatrix[0][0] = chalk.green("Processes");
    this.updateTable();
  }

  initiateActivityMatrix() {
    this.activityMatrix = [
      this.initiateProcessStatusColumn(),
      this.initiateSyncStatusColumn(),
      this.initiateBalanceColumn(),
      this.initiateJoinedStatusColumn(),
    ];
  }

  log(line) {
    this.log.push(line);
  }

  writeToLog(line) {
    this.log.push(line);
  }

  initiateProcessStatusColumn() {
    let data = ["ACTIVE"];
    for (let i = 0; i < this.numberOfInstances; i++) data.push(chalk.red("NO"));
    return data;
  }

  initiateJoinedStatusColumn() {
    let data = ["JOINED WALLET"];
    for (let i = 0; i < this.numberOfInstances; i++) data.push(chalk.red("NO"));
    return data;
  }
  initiateSyncStatusColumn() {
    let data = ["SYNC"];
    for (let i = 0; i < this.numberOfInstances; i++) data.push(chalk.red("NO"));
    return data;
  }

  initiateBalanceColumn() {
    let data = ["BALANCE"];
    for (let i = 0; i < this.numberOfInstances; i++) data.push(chalk.red(0));
    return data;
  }

  setPeerJoinedWallet(peerId) {
    this.activityMatrix[3][peerId + 1] = chalk.green("YES");
    this.updateTable();
  }

  setPeerActive(peerId) {
    this.writeToLog("Peer set to active: " + peerId);
    this.activityMatrix[0][peerId + 1] = chalk.green("YES");
    this.updateTable();
  }

  setPeerInactive(peerId) {
    this.activityMatrix[0][peerId + 1] = chalk.red("NO");
    this.updateTable();
  }

  setPeerSynced(peerId) {
    this.activityMatrix[1][peerId + 1] = chalk.green("YES");
    this.updateTable();
  }

  setBalance(peerId, balance) {
    if (balance === 0) {
      this.activityMatrix[2][peerId + 1] = chalk.red(0);
    }
    else {
      this.activityMatrix[2][peerId + 1] = chalk.green(balance);
    }
    this.updateTable();
  }


  setPeerDesynced(peerId) {
    this.activityMatrix[1][peerId + 1] = chalk.red("NO");
    this.updateTable();
  }
  // Update the table with new data
  updateTable() {
    const data = this.activityMatrix;
    this.table.length = 0;

    data.forEach((item) => {
      this.table.push(item);
    });

    console.clear(); // Clear the console
    console.log(this.table.toString()); // Print the table
    console.log("LOG: " + this.log.length);
    for (let line of this.log) {
      console.log(line);
    }
  }

  // Start the periodic table update
  start(intervalMs = 1000) {
    if (this.interval !== null) {
      clearInterval(this.interval); // Ensure no intervals are duplicated
    }
    this.interval = setInterval(() => this.updateTable(), intervalMs);
  }

  // Stop the periodic table update
  stop() {
    if (this.interval !== null) {
      clearInterval(this.interval);
      this.interval = null;
    }
  }
}

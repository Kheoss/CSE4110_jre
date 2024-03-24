class Wallet {
  id;
  members;

  constructor(id) {
    this.id = id;
    this.members = 1;
  }

  addMember = () => {
    this.members++;
  };
}

export default Wallet;

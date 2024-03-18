import router from "./router.js";
import express from "express";

const PORT = 3001;

const app = express();

app.listen(PORT, () => {
    console.log(`Server listening on ${PORT}`);
});

app.use(express.json({ extended: false }));
app.use("/api", router);

export default app;
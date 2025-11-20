import express from "express";
import cors from "cors";
import admin from "firebase-admin";
import dotenv from "dotenv";
import { readFileSync } from "fs";

// Load service account
const serviceAccount = JSON.parse(readFileSync('./service_account.json', 'utf8'));

dotenv.config();

// Initialize admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const app = express();
app.use(cors());
app.use(express.json());

// Simple admin API key (for securing your backend)
const ADMIN_SECRET_KEY = process.env.ADMIN_SECRET_KEY;

// Reset password API
app.post("/api/resetPassword", async (req, res) => {
  const { secretKey, email, newPassword } = req.body;

  // Security check
  if (secretKey !== ADMIN_SECRET_KEY) {
    return res.status(403).json({ error: "Unauthorized" });
  }

  if (!email || !newPassword) {
    return res.status(400).json({ error: "Missing email or newPassword" });
  }

  try {
    // get user by email
    const user = await admin.auth().getUserByEmail(email);

    // update password
    await admin.auth().updateUser(user.uid, {
      password: newPassword,
    });

    res.json({ success: true, message: "Password updated" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log("Server running on port " + PORT));

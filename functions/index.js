const functions = require("firebase-functions");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

/**
 * 🔔 Cloud Function: Notificaciones Push para Chat Privado
 * Se dispara automáticamente cuando entra un mensaje en /private_inbox/{targetNick}/{msgId}
 */
exports.sendPrivateMessageNotification = functions.database
  .ref("/private_inbox/{targetNick}/{msgId}")
  .onCreate(async (snapshot, context) => {
    const msg = snapshot.val();
    if (!msg) return null;

    const targetNick = context.params.targetNick;
    const senderNick = (msg.senderNick || "ALGUIEN").toString().toUpperCase();
    const text = (msg.text || "Te ha enviado un mensaje privado.").toString();

    // El tema FCM al que está suscrito el destinatario en Android
    const topic = `user_${targetNick}`;

    const payload = {
      notification: {
        title: `💬 Mensaje privado de ${senderNick}`,
        body: text,
      },
      data: {
        type: "private_chat",
        sender_nick: senderNick,
        message: text,
        title: `💬 Mensaje privado de ${senderNick}`,
      },
      android: {
        priority: "high",
        notification: {
          sound: "default",
          channelId: "chat_notifications",
          clickAction: "OPEN_CHAT",
        },
      },
      topic: topic,
    };

    try {
      await admin.messaging().send(payload);
      console.log(`Notificación enviada a ${topic} desde ${senderNick}`);
    } catch (error) {
      console.error("Error al enviar la notificación Push:", error);
    }

    return null;
  });

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
      console.error("Error al enviar la notificación Push privada:", error);
    }

    return null;
  });

/**
 * 📣 Cloud Function: Notificaciones Push para Menciones Públicas (@NICK)
 * Se dispara automáticamente cuando entra una mención en /public_mentions/{targetNick}/{msgId}
 */
exports.sendPublicMentionNotification = functions.database
  .ref("/public_mentions/{targetNick}/{msgId}")
  .onCreate(async (snapshot, context) => {
    const msg = snapshot.val();
    if (!msg) return null;

    const targetNick = context.params.targetNick;
    const senderNick = (msg.senderNick || "ALGUIEN").toString().toUpperCase();
    const text = (msg.text || "Te ha mencionado en el canal.").toString();

    const topic = `user_${targetNick}`;

    const payload = {
      notification: {
        title: `📣 TE HAN MENCIONADO EN CANAL (${senderNick})`,
        body: text,
      },
      data: {
        type: "public_mention",
        sender_nick: senderNick,
        message: text,
        title: `📣 TE HAN MENCIONADO EN CANAL (${senderNick})`,
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
      console.log(`Mención enviada a ${topic} desde ${senderNick}`);
    } catch (error) {
      console.error("Error al enviar la notificación Push de mención:", error);
    }

    // Auto-limpieza tras enviar
    try {
      await snapshot.ref.remove();
    } catch (e) {}

    return null;
  });

package net.newlydev.ngrok.ngrok_core;

public class HeartThread extends Thread {
    private MessageHandler messageHandler;

    public HeartThread(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                messageHandler.sendPing();
                Thread.sleep(3000);
            }
        } catch (Exception e) {
			e.printStackTrace();
        }

    }
}


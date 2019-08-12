package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.engine.events.ComServerEvent;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisterAndReceiveAllEventCategories implements WebSocketListener {
    private CountDownLatch messageReceivedLatch;
    private AtomicInteger receivedReplies = new AtomicInteger(0);
    private List<String> receivedMessages = new ArrayList<String>();
    private List<ComServerEvent> receivedEvents = new ArrayList<ComServerEvent>();
    private Session session;

    public RegisterAndReceiveAllEventCategories() {
    }

    public RegisterAndReceiveAllEventCategories(CountDownLatch messageReceivedLatch) {
        this.messageReceivedLatch = messageReceivedLatch;
    }

    public void closeIfOpen() {
        if (isOpen()) {
            session.close();
        }
    }

    public boolean isOpen() {
        return session != null;
    }

    public void register() throws IOException {
        session.getRemote().sendString("Register request for binary events for warnings:");
    }

    public void registerForInfo() throws IOException {
        session.getRemote().sendString("Register request for info:");
    }

    public void registerForComTasksOnly() throws IOException {
        session.getRemote().sendString("Register request for binary events for tracing: COMTASK");
//        session.getRemote().sendString("Register request for debugging: COMTASK");
    }

    public void registerMalformedRequest() throws IOException {
        session.getRemote().sendString("Anything as long as it does not conform to the expected parse format");
    }

    public synchronized int getReceivedReplies() {
        return receivedReplies.get();
    }

    public synchronized List<String> getReceivedMessages() {
        return receivedMessages;
    }

    public synchronized List<ComServerEvent> getReceivedEvents() {
        return receivedEvents;
    }

    private synchronized void parseReply(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object object = ois.readObject();
        if (object instanceof String) {
            String stringReply = (String) object;
            receivedMessages.add(stringReply);
        } else {
            ComServerEvent event = (ComServerEvent) object;
            receivedEvents.add(event);
        }
        ois.close();
    }

    @Override
    public void onWebSocketBinary(byte[] data, int offset, int len) {
//        System.out.println("[RegisterAndReceiveAllEventCategories] received binary message, offset=" + offset + " len=" + len);
        receivedReplies.incrementAndGet();
        try {
            byte[] actualBytes = new byte[len];
            System.arraycopy(data, offset, actualBytes, 0, len);
            parseReply(actualBytes);
            if (messageReceivedLatch != null) {
                messageReceivedLatch.countDown();
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void onWebSocketText(String message) {
//        System.out.println("[RegisterAndReceiveAllEventCategories] received text message: " + message);
        receivedMessages.add(message);
        if (messageReceivedLatch != null) {
            messageReceivedLatch.countDown();
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        session = null;
//        System.out.println("[RegisterAndReceiveAllEventCategories] Closing socket with code=" + statusCode + " reason: " + reason);
    }

    @Override
    public void onWebSocketConnect(Session session) {
//        System.out.println("[RegisterAndReceiveAllEventCategories] received session request");
        this.session = session;
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace();
    }
}


package elif.marketdata.api.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class WebSocketHandler extends TextWebSocketHandler implements Runnable {

    boolean debug = false;

    final Set<WebSocketSession> sessions = new HashSet<>();

    @PostConstruct
    public void init() {
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        final String payload = message.getPayload();
        System.out.println("WSS Received Message " + payload);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.add(session);
//        session.sendMessage(new TextMessage("{\"text\":\"hello\"}"));
//        System.out.println("new session " + session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession closedSession, CloseStatus status) throws Exception {
        sessions.remove(closedSession);
        super.afterConnectionClosed(closedSession, status);
        System.out.println("ClosingSocket session " + closedSession);
    }

    @Override
    public void run() {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage("{\"time\":\"" + new Date() + "\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendToEveryone(String jsonString) {
        System.out.println(sessions.size() + " SENDING TO EVERONE " + jsonString);
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(jsonString));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

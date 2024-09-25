package com.xiaoxu.websocket;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint("/api/websocket/{userId}/{teamId}/myapp.ws")
public class WebsocketDemo {

    @OnMessage
    public void message(String message, Session session) {
        for (Session s : session.getOpenSessions()) {
            System.out.println("receive : " + message);
            s.getAsyncRemote().sendText(message);
        }
    }
}
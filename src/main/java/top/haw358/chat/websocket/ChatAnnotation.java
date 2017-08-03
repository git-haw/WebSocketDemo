package top.haw358.chat.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import top.haw358.chat.filter.HTMLFilter;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket消息推送服务类
 * @author haw
 * Created by haw on 17-8-3.
 */
@ServerEndpoint(value = "/websocket/chat")
public class ChatAnnotation {
    private static final Log log = LogFactory.getLog(ChatAnnotation.class);

    private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Map<String,Object> connections = new HashMap<String,Object>();

    private final String nickname;
    private Session session;

    public ChatAnnotation() {
        nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
    }


    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        connections.put(nickname, this);
        String message = String.format("* %s %s", nickname, "has joined.");
        broadcast(message);
    }


    @OnClose
    public void onClose(){
        close(this);
    }

    private static void close(ChatAnnotation chatAnnotation) {
        try {
            chatAnnotation.session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        chatAnnotation.connections.remove(chatAnnotation.nickname);
        String message = String.format("* %s %s",
                chatAnnotation.nickname, "has disconnected.");
        broadcast(message);
    }


    /**
     * 消息发送触发方法
     * @param message
     */
    @OnMessage
    public void incoming(String message) {
        // Never trust the client
        String filteredMessage = String.format("%s: %s",
                nickname, HTMLFilter.filter(message.toString()));
        broadcast(filteredMessage);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        log.error("Chat Error: " + t.toString(), t);
    }

    /**
     * 消息发送方法
     * @param msg
     */
    private static void broadcast(String msg) {
        if(msg.indexOf("Guest0")!=-1){
            sendUser(msg);
        } else{
            sendAll(msg);
        }
    }

    /**
     * 向所有用户发送
     * @param msg
     */
    public static void sendAll(String msg){
        for (String key : connections.keySet()) {
            ChatAnnotation client = null ;
            try {
                client = (ChatAnnotation) connections.get(key);
                synchronized (client) {
                    client.session.getBasicRemote().sendText(msg);
                }
            } catch (IOException e) {
                log.debug("Chat Error: Failed to send message to client", e);

                close(client);
            }
        }
    }

    /**
     * 向指定用户发送消息
     * @param msg
     */
    public static void sendUser(String msg){
        ChatAnnotation chatAnnotation = (ChatAnnotation)connections.get("Guest0");
        if(chatAnnotation == null)
            return;

        try {
            chatAnnotation.session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            log.debug("Chat Error: Failed to send message to client", e);
            close(chatAnnotation);
        }
    }
}

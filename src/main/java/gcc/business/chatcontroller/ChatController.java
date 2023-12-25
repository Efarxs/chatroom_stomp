package gcc.business.chatcontroller;

import gcc.pojo.ChatMessage;
import gcc.service.ChatMessageService;
import gcc.service.ChatUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatUserService chatUserService;
    private final ChatMessageService chatMessageService;


    private Set<String> userList = new HashSet<>();

    /**
     * 发送私聊消息
     * @param chatMessage
     */
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        System.out.println(chatMessage);
        // 把消息保存到数据库中
        chatMessage.setCreateTime(new Date());
        chatMessageService.save(chatMessage);
        // 这里拼接成的订阅地址是 userPrefix + userName + /queue
        // 通知一条给自己
        messagingTemplate.convertAndSendToUser(chatMessage.getSender(), "/queue", chatMessage);
        // 通知给目标用户
        messagingTemplate.convertAndSendToUser(chatMessage.getToUser(), "/queue", chatMessage);
    }

    /**
     * 广播消息
     * @param chatMessage
     * @return
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        //MyList.add(chatMessage);

        // 把消息保存到数据库中
        chatMessage.setCreateTime(new Date());
        chatMessageService.save(chatMessage);
        return chatMessage;
    }

    /**
     *
     * @param chatMessage
     * @param headerAccessor
     * @return
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
//        if (userList.contains(chatMessage.getSender())) {
//            return null;
//        }
        userList.add(chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        //NameList.getActiveUsers().add(chatMessage.getSender());
        //MyList.add(chatMessage);

        chatMessage.setCreateTime(new Date());
        chatMessageService.save(chatMessage);
        return chatMessage;
    }

    /**
     * 用户断开Websocket连接事件
     * @param event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username != null) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);
            chatMessage.setContent("离开了聊天室");
            userList.remove(username);
            //MyList.add(chatMessage);
            chatMessage.setCreateTime(new Date());
            chatMessageService.save(chatMessage);
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}

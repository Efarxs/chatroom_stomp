package gcc.dataobject;

import gcc.pojo.ChatMessage;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 消息列表
 */
public class MyList {
    /**
     * 创建一个消息队列 用来保存用户发送的消息
     */
    private static ConcurrentLinkedDeque<ChatMessage> list = new ConcurrentLinkedDeque<>();

    /**
     * 获取消息列表
     * @return
     */
    public static ConcurrentLinkedDeque<ChatMessage> getList(){
        return list;
    }

    /**
     * 添加消息到消息队列
     * @param chatMessage
     */
    public static void add(ChatMessage chatMessage){
        list.add(chatMessage);
    }

    /**
     * 移除一条消息（删除）
     * @param chatMessage
     */
    public static void remove(ChatMessage chatMessage){
        list.remove(chatMessage);
    }
}

package gcc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个 WebSocket 端点，客户端将使用它连接到我们的 WebSocket 服务器。
        registry.addEndpoint("/chat")
                .withSockJS(); // 支持SockJS
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 设置发送消息到客户端时的超时时间限制，单位为毫秒
        // 如果消息在这个时间内没有发送完成，将会失败
        registration.setSendTimeLimit(15 * 1000)
                // 设置服务器端发送消息缓冲区的大小限制，单位为字节
                // 这可以防止发送大量数据时使用过多内存
                .setSendBufferSizeLimit(512 * 1024)
                // 设置允许的接收消息的最大字节大小
                // 如果收到的消息超过这个大小，将不会被接收，并可能导致连接关闭
                .setMessageSizeLimit(128 * 1024)
                // 设置从连接建立后到接收到首条消息的最大等待时间，单位为毫秒
                // 如果在这个时间段内没有收到任何消息，连接可能会被关闭
                .setTimeToFirstMessage(15 * 1000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理，并配置消息代理的目的地前缀
        // 这里前缀可以设定很多个，最后自己根据这个前缀来判断是群发还是私发，这里定义的是queue是私发，topic是群发
        registry.enableSimpleBroker("/private",
                        "/topic")
                //设置一个定时器
                .setTaskScheduler(new DefaultManagedTaskScheduler())
                // 设置心跳值，长数组中的第一个值表示服务器发送心跳的频率，单位为毫秒
                // 第二个值表示服务器期望客户端发送心跳的频率，单位为毫秒
                // 如果在这些时间间隔内没有收到对应的心跳，连接可能会被视为断开
                .setHeartbeatValue(new long[]{10000, 20000});
        // 设置应用程序向客户端发送消息时的目的地前缀
        // 客户端订阅或发布消息时需要使用这个前缀
        // 客户端send命令的时候需要带上/app前缀
        registry.setApplicationDestinationPrefixes("/app");

        // 用户订阅前缀 默认就是/user/
        registry.setUserDestinationPrefix("/private/");
    }
}

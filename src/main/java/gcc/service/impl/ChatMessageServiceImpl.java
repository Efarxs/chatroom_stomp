package gcc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcc.pojo.ChatMessage;
import gcc.service.ChatMessageService;
import gcc.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【chat_message】的数据库操作Service实现
* @createDate 2023-12-23 15:01:07
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

}





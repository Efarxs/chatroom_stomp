package gcc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcc.pojo.ChatUser;
import gcc.service.ChatUserService;
import gcc.mapper.ChatUserMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【chat_user】的数据库操作Service实现
* @createDate 2023-12-23 15:01:07
*/
@Service
public class ChatUserServiceImpl extends ServiceImpl<ChatUserMapper, ChatUser>
    implements ChatUserService{

}





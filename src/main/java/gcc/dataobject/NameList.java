package gcc.dataobject;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 当前用户列表
 */
public class NameList {
    // 可以使用一个线程安全的Set来存储活跃的用户名
    private static final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    public static Set<String> getActiveUsers(){
        return activeUsers;
    }
}

package gcc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/12/23
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 登录拦截
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                Object user = request.getSession().getAttribute("user");
                if (user == null) {
                    // 没有登录，那么就跳转到登录
                    response.sendRedirect("/login");
                    return false;
                }

                return true;
            }
        }).excludePathPatterns("/login","/toLogin","/register","/toRegister","/captcha");
    }
}

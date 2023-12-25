package gcc.business.htmlcontroller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.base.Captcha;
import gcc.pojo.ChatMessage;
import gcc.pojo.ChatUser;
import gcc.service.ChatMessageService;
import gcc.service.ChatUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class HtmlController {

    private final ChatUserService chatUserService;
    private final ChatMessageService chatMessageService;

    /**
     * 开通VIP
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/ktvip")
    public void ktvip(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ChatUser user = (ChatUser) request.getSession().getAttribute("user");
        user.setVip(true);

        // 更新信息
        chatUserService.updateById(user);
        request.getSession().setAttribute("user",user);

        // 刷新页面
        response.sendRedirect("/main");
    }

    /**
     * 注册响应
     * @param username
     * @param password
     * @param captcha
     * @param model
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @PostMapping("/toRegister")
    public String toRegister(String username, String password, String captcha, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 先判断验证码
        String verifyCode = (String) request.getSession().getAttribute("verifyCode");
        if (verifyCode == null) {
            model.addAttribute("msg","注册失败，验证码失效了");
            return "register";
        }
        if (!verifyCode.equalsIgnoreCase(captcha)) {
            model.addAttribute("msg","注册失败，验证码错误");
            return "register";
        }
        Map<String,Object> whereMap = new HashMap<>(1);
        whereMap.put("username",username.trim());
        List<ChatUser> chatUsers = chatUserService.getBaseMapper().selectByMap(whereMap);
        if (!chatUsers.isEmpty()) {
            model.addAttribute("msg","注册失败，用户名已被占用了");
            return "register";
        }
        ChatUser chatUser = new ChatUser();
        chatUser.setUsername(username.trim());
        chatUser.setPassword(password);
        chatUser.setVip(false);
        chatUser.setCreateTime(new Date());
        chatUser.setUpdateTime(new Date());
        // 保存数据到数据库
        chatUserService.save(chatUser);
        // 注册成功
        response.sendRedirect("/login");

        return null;
    }

    /**
     * 登录响应
     * @param username
     * @param password
     * @param model
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @PostMapping("/toLogin")
    public String toLogin(String username, String password , Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String,Object> whereMap = new HashMap<>(1);
        whereMap.put("username",username.trim());
        List<ChatUser> chatUsers = chatUserService.getBaseMapper().selectByMap(whereMap);
        if (chatUsers.isEmpty()) {
            model.addAttribute("msg","登录失败，用户不存在");
            return "login";
        }
        ChatUser chatUser = chatUsers.get(0);
        if (!chatUser.getPassword().equals(password)) {
            model.addAttribute("msg","登录失败，用户或密码错误");
            return "login";
        }

        // 登录成功
        // 更新登录信息
        chatUser.setLastLoginTime(new Date());
        chatUserService.updateById(chatUser);
        request.getSession().setAttribute("user",chatUser);
        request.getSession().setAttribute("username",chatUser.getUsername());
        response.sendRedirect("/main");

        return null;
    }

    /**
     * 主页
     * @return
     */
    @GetMapping("/main")
    public String main(HttpServletRequest request,Model model) {
        ChatUser user = (ChatUser) request.getSession().getAttribute("user");
        model.addAttribute("vip",user.getVip());
        return "main";
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control","no-store");
        response.setHeader("Pragma","no-cache");
        response.setDateHeader("Expires",0);
        response.setContentType("image/gif");
        //生成验证码对象,三个参数分别是 宽、高、位数
        GifCaptcha captcha = new GifCaptcha(130, 32, 5);
        // 设置验证码的字符类型为数字和字母混合
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        // 设置内置字体
        captcha.setCharType(Captcha.FONT_1);
        // 验证码存入session
        request.getSession().setAttribute("verifyCode",captcha.text().toLowerCase());
        //输出图片流
        captcha.out(response.getOutputStream());
    }

    /**
     * 注册页
     * @return
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * 登录页
     * @return
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 获得音频
     * @param name
     * @return
     */
    @GetMapping(value = "/u/{name}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> getAudio(@PathVariable String name) {
        String filePath = System.getProperty("user.dir") + "/file/upload/" + name + ".webm"; // 音频文件路径

        FileSystemResource file = new FileSystemResource(filePath);
        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=" + file.getFilename())
                .body(file);
    }

    /**
     * 上传音频
     * @param request
     * @param audio
     * @return
     */
    @PostMapping("/upload")
    public @ResponseBody Map<String, Object> upload(HttpServletRequest request, @RequestBody MultipartFile audio) {
        Map<String,Object> map = new HashMap<>(3);
        // 保存的文件名
        String fileName = "audio-" + UUID.randomUUID();
        // 文件保存路径
        String uploadPath = "file" + File.separator + "upload" + File.separator;
        // 先判断目录是否存在，如果不存在，那么就创建
//        File fileDir = new File(uploadPath);
//        if (!fileDir.exists() && !fileDir.mkdir()) {
//            // 目录创建事件
//            //throw new RuntimeException("目录创建失败");
//            map.put("code",500);
//            map.put("message","文件目录创建失败" + uploadPath);
//            map.put("data",null);
//            return map;
//        }

        try {
            File file = new File(uploadPath + fileName + ".webm");
            FileOutputStream outputStream = new FileOutputStream(file);

            FileCopyUtils.copy(audio.getInputStream(), outputStream);
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            map.put("code",500);
            map.put("message","文件上传失败" + e.getMessage());
            map.put("data",null);
            return map;
        }

        map.put("code",200);
        map.put("message","success");
        Map<String,Object> dataMap = new HashMap<>(2);
        dataMap.put("url",request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/u/" + fileName);
        dataMap.put("file",fileName);
        map.put("data",dataMap);
        return map;
    }

    /**
     * 好友聊天
     * @return
     */
    @RequestMapping("/friend")
    public String friend() {
        return "friend";
    }

    /**
     * 聊天室
     * @return
     */
    @RequestMapping("/room")
    public String gotoRoom(){
        return "room";
    }

    /**
     * 获取私聊聊天记录
     * @return
     */
    @RequestMapping("/initPrivateMessage/{toUser}")
    public @ResponseBody List<ChatMessage> initPrivateMessage(HttpServletRequest request, @PathVariable String toUser) {
        ChatUser user = (ChatUser) request.getSession().getAttribute("user");
        List<ChatMessage> list;
        if (user.getVip()) {
            // 如果是VIP，那么就获取所有聊天记录
            Map<String,Object> whereMap = new HashMap<>(1);
            whereMap.put("to_user",toUser);
            list = chatMessageService.listByMap(whereMap);
        } else {
            // 不是VIP，只能获取登录后的聊天记录
            list = chatMessageService.list(Wrappers.<ChatMessage>lambdaQuery().eq(ChatMessage::getToUser,toUser).gt(ChatMessage::getCreateTime, user.getLastLoginTime()));
        }

        return list;
    }

    /**
     * 获取聊天室聊天记录
     * @return
     */
    @RequestMapping("/initMessage")
    public @ResponseBody List<ChatMessage> initMessage(HttpServletRequest request) {
        ChatUser user = (ChatUser) request.getSession().getAttribute("user");
        List<ChatMessage> list;
        if (user.getVip()) {
            // 如果是VIP，那么就获取所有聊天记录
            Map<String,Object> whereMap = new HashMap<>(1);
            whereMap.put("to_user",null);
            list = chatMessageService.listByMap(whereMap);
        } else {
            // 不是VIP，只能获取登录后的聊天记录
            list = chatMessageService.list(Wrappers.<ChatMessage>lambdaQuery().eq(ChatMessage::getToUser,null).gt(ChatMessage::getCreateTime, user.getLastLoginTime()));
        }

        return list;
    }

    /**
     * 进入好友聊天，判断好友是否存在
     * @param username
     * @return
     */
    @RequestMapping("/checkFriend")
    public @ResponseBody String checkFriend(@RequestParam String username, HttpServletRequest request) {
        String loginUsername = (String) request.getSession().getAttribute("username");
        if (loginUsername.equals(username)) {
            // 不能与自己聊天
            return "fail";
        }
        LambdaQueryWrapper<ChatUser> lq = new LambdaQueryWrapper<>();
        lq.eq(ChatUser::getUsername, username);
        ChatUser one = chatUserService.getOne(lq);
        if (one == null) {
            return "fail";
        }

        return "success";
    }

    @RequestMapping("/")
    public void index(HttpServletResponse response) throws IOException {
        response.sendRedirect("/main");
    }

//    /**
//     * 进入聊天室，检查是否重名
//     * @param username
//     * @return
//     */
//    @RequestMapping("/checkRepeatedName")
//    public @ResponseBody String checkRepeatedName(@RequestParam String username) {
//        // 如果这个用户名已经存在了，那么就拒绝进入聊天室
//        if(NameList.getActiveUsers().contains(username)){
//            return "fail";
//        }
//        return "success";
//    }
}
package com.site.blog.my.core.controller.admin;

import cn.hutool.captcha.ShearCaptcha;
import com.site.blog.my.core.config.Constants;
import com.site.blog.my.core.entity.AdminUser;
import com.site.blog.my.core.service.*;
import com.site.blog.my.core.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 13
 * @qq交流群 796794009
 * @email 2449207463@qq.com
 * @link http://13blog.site
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private BlogService blogService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private LinkService linkService;
    @Resource
    private TagService tagService;
    @Resource
    private CommentService commentService;


    @GetMapping({"/login"})
    public String login() {
        return "admin/login";
    }

    @GetMapping({"", "/", "/index", "/index.html"})
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "index");
        request.setAttribute("categoryCount", categoryService.getTotalCategories());
        request.setAttribute("blogCount", blogService.getTotalBlogs());
        request.setAttribute("linkCount", linkService.getTotalLinks());
        request.setAttribute("tagCount", tagService.getTotalTags());
        request.setAttribute("commentCount", commentService.getTotalComments());
        return "admin/index";
    }

    @GetMapping({"/signin"})
    public String signin(HttpServletRequest request) {
        return "admin/signin";
    }

    @PostMapping({"/showSidebar"})
    @ResponseBody
    public String showSidebar(HttpServletRequest request) {
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        AdminUser user = adminUserService.getUserDetailById(loginUserId);
        if(user.getLevel() == Constants.LEVEL_MANAGER){
            return "fail";
        }
        return "success";
    }

    @PostMapping({"/signin"})
    public String addUser(@RequestParam("userName") String userName,
                          @RequestParam("password") String password,
                          @RequestParam("mail") String mail,
                          @RequestParam("nickName") String nickName,
                          @RequestParam("verifyCode") String verifyCode,
                          HttpSession session) {
        if (!StringUtils.hasText(verifyCode)) {
            session.setAttribute("errorMsg", "验证码不能为空");
            return "admin/signin";
        }
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(password)) {
            session.setAttribute("errorMsg", "用户名或密码不能为空");
            return "admin/signin";
        }
        if (!StringUtils.hasText(mail)) {
            session.setAttribute("errorMsg", "邮箱不能为空");
            return "admin/signin";
        }
        if (!StringUtils.hasText(nickName)) {
            session.setAttribute("errorMsg", "别名不能为空");
            return "admin/signin";
        }
        ShearCaptcha shearCaptcha = (ShearCaptcha) session.getAttribute("verifyCode");
        if (shearCaptcha == null || !shearCaptcha.verify(verifyCode)) {
            session.setAttribute("errorMsg", "验证码错误");
            return "admin/signin";
        }
        ;
        if (adminUserService.isUserExist(userName)) {
            session.setAttribute("errorMsg", "用户名已存在");
            return "admin/signin";
        }
    //添加用户
        if(!adminUserService.addUser(userName,password,mail,nickName)){
            System.out.println("用户注册错误-》" + "userName：" + userName + "password：" + password + "mail：" + mail + "nickName：" + nickName);
            session.setAttribute("errorMsg", "未知错误，请联系管理员处理");
            return "admin/signin";
        }
        return "admin/login";
    }

    @PostMapping(value = "/login")
    public String login(@RequestParam("userName") String userName,
                        @RequestParam("password") String password,
                        @RequestParam("verifyCode") String verifyCode,
                        HttpSession session) {
        if (!StringUtils.hasText(verifyCode)) {
            session.setAttribute("errorMsg", "验证码不能为空");
            return "admin/login";
        }
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(password)) {
            session.setAttribute("errorMsg", "用户名或密码不能为空");
            return "admin/login";
        }
        ShearCaptcha shearCaptcha = (ShearCaptcha) session.getAttribute("verifyCode");
        if (shearCaptcha == null || !shearCaptcha.verify(verifyCode)) {
            session.setAttribute("errorMsg", "验证码错误");
            return "admin/login";
        }
        AdminUser adminUser = adminUserService.login(userName, password);
        if (adminUser != null) {
            session.setAttribute("loginUser", adminUser.getNickName());
            session.setAttribute("loginUserId", adminUser.getAdminUserId());
            session.setAttribute("avatar", adminUser.getAvatar());
            //session过期时间设置为7200秒 即两小时
            //session.setMaxInactiveInterval(60 * 60 * 2);
            return "redirect:/admin/index";
        } else {
            if(adminUserService.isUserExist(userName)){
                session.setAttribute("errorMsg", "登陆失败,请等待审核");
                return "admin/login";
            }
            session.setAttribute("errorMsg", "登陆失败");
            return "admin/login";
        }
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request) {
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        AdminUser adminUser = adminUserService.getUserDetailById(loginUserId);
        if (adminUser == null) {
            return "admin/login";
        }
        request.setAttribute("path", "profile");
        request.setAttribute("loginUserName", adminUser.getLoginUserName());
        request.setAttribute("nickName", adminUser.getNickName());
        request.setAttribute("avatar", adminUser.getAvatar());
        request.setAttribute("email", adminUser.getMail());
        return "admin/profile";
    }

    @PostMapping("/profile/password")
    @ResponseBody
    public String passwordUpdate(HttpServletRequest request, @RequestParam("originalPassword") String originalPassword,
                                 @RequestParam("newPassword") String newPassword) {
        if (!StringUtils.hasText(originalPassword) || !StringUtils.hasText(newPassword)) {
            return "参数不能为空";
        }
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        if (adminUserService.updatePassword(loginUserId, originalPassword, newPassword)) {
            //修改成功后清空session中的数据，前端控制跳转至登录页
            request.getSession().removeAttribute("loginUserId");
            request.getSession().removeAttribute("loginUser");
            request.getSession().removeAttribute("errorMsg");
            return "success";
        } else {
            return "修改失败";
        }
    }

    @PostMapping("/profile/name")
    @ResponseBody
    public String nameUpdate(HttpServletRequest request, @RequestParam("loginUserName") String loginUserName,
                             @RequestParam("nickName") String nickName,@RequestParam("avatar") String avatar,@RequestParam("email") String email) {
        if (!StringUtils.hasText(loginUserName) || !StringUtils.hasText(nickName)|| !StringUtils.hasText(avatar)|| !StringUtils.hasText(email)) {
            return "参数不能为空";
        }
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");

        List<String> strs = new ArrayList<>();
        strs.add(avatar);
        if(!changePicPath(strs)){
            return "修改失败";
        }
        avatar = avatar.replaceAll("temp","avatar");
        if (adminUserService.updateName(loginUserId, loginUserName, nickName,avatar,email)) {
            return "success";
        } else {
            return "修改失败";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().removeAttribute("loginUserId");
        request.getSession().removeAttribute("loginUser");
        request.getSession().removeAttribute("errorMsg");
        return "admin/login";
    }

    /*
    博客内容所有图片地址转换
     */
    public boolean changePicPath(List<String> inputs){
        File file1 = new File(Constants.FILE_UPLOAD_BLOG_DIC);
        File file2 = new File(Constants.FILE_UPLOAD_TEMP_DIC);
        File file3 = new File(Constants.FILE_UPLOAD_AVATAR_DIC);
        if(!file1.exists()){
            file1.mkdir();
        }
        if(!file2.exists()){
            file2.mkdir();
        }
        if(!file3.exists()){
            file3.mkdir();
        }
        for (String path:inputs) {
            String str = "temp";
            if(!path.contains(str)){
                continue;
            }
            int tempIndex = path.indexOf(str);
            String picName = path.substring(tempIndex + str.length() +1, path.length());

            String newPath = Constants.FILE_UPLOAD_AVATAR_DIC + picName;
            String oldPath = Constants.FILE_UPLOAD_TEMP_DIC + picName;;
            System.out.println("===============================");
            System.out.println("newPath:" + newPath);
            System.out.println("oldPath:" + oldPath);
            System.out.println("===============================");
            File oldFile = new File(oldPath);
            File newFile = new File(newPath);
            if(!oldFile.renameTo(newFile)){
                System.out.println("图片地址转换失败,未知");
                return false;
            }
        }
        return true;
    }
}

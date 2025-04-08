package com.site.blog.my.core.controller.common;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class CommonController {

    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);

    @GetMapping("/common/kaptcha")
    public void defaultKaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        logger.info("进入生成验证码方法，请求路径: {}", httpServletRequest.getRequestURI());

        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/png");

        ShearCaptcha shearCaptcha= CaptchaUtil.createShearCaptcha(150, 30, 4, 2);

        // 验证码存入session
        httpServletRequest.getSession().setAttribute("verifyCode", shearCaptcha);

        logger.debug("检查 HttpServletResponse 是否为 null: {}", httpServletResponse!= null);

        try {
            shearCaptcha.write(httpServletResponse.getOutputStream());
            logger.info("验证码图片已成功写入响应");
        } catch (IOException e) {
            logger.error("写入验证码图片时发生错误", e);
        } catch (NullPointerException e) {
            logger.error("获取 OutputStream 时发生空指针异常", e);
        }

//
//        // 输出图片流
//        shearCaptcha.write(httpServletResponse.getOutputStream());
    }
}


package com.site.blog.my.core.config;

import java.io.File;

/**
 * Created by 13 on 2019/5/24.
 */
public class Constants {
//    public final static String FILE_UPLOAD_DIC = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "upload" + File.separator;//上传文件的默认url前缀，根据部署设置自行修改
    public final static String FILE_UPLOAD_DIC = File.separator +  System.getProperty("user.dir") + File.separator + "upload" + File.separator;//上传文件的默认url前缀，根据部署设置自行修改
    public final static String FILE_UPLOAD_AVATAR_DIC = System.getProperty("user.dir") + File.separator + "upload" + File.separator + "avatar" +  File.separator;//上传头像的位置
    public final static String FILE_UPLOAD_BLOG_DIC = System.getProperty("user.dir") + File.separator + "upload" + File.separator + "blog" +  File.separator;//上传博客图片的位置
    public final static String FILE_UPLOAD_TEMP_DIC = System.getProperty("user.dir") + File.separator + "upload" + File.separator + "temp" +  File.separator;//临时上传图片的位置
    public final static Integer LEVEL_MANAGER = 1;//管理员
    public final static Integer LEVEL_ORDINARY_USER = 0;//普通用户
    public final static String DEFAULT_AVATAR = "/admin/dist/img/avatar.png";//默认用户头像
}

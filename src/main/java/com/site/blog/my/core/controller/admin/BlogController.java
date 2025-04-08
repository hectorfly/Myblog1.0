package com.site.blog.my.core.controller.admin;

import com.site.blog.my.core.config.Constants;
import com.site.blog.my.core.entity.AdminUser;
import com.site.blog.my.core.entity.Blog;
import com.site.blog.my.core.service.AdminUserService;
import com.site.blog.my.core.service.BlogService;
import com.site.blog.my.core.service.CategoryService;
import com.site.blog.my.core.util.MyBlogUtils;
import com.site.blog.my.core.util.PageQueryUtil;
import com.site.blog.my.core.util.Result;
import com.site.blog.my.core.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 13
 * @qq交流群 796794009
 * @email 2449207463@qq.com
 * @link http://13blog.site
 */
@Controller
@RequestMapping("/admin")
public class BlogController {

    @Resource
    private BlogService blogService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private AdminUserService adminUserService;

    @GetMapping("/blogs/list")
    @ResponseBody
    public Result list(@RequestParam Map<String, Object> params,HttpServletRequest request) {
        if (ObjectUtils.isEmpty(params.get("page")) || ObjectUtils.isEmpty(params.get("limit"))) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        AdminUser user = adminUserService.getUserDetailById(loginUserId);
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        if(user.getLevel() == Constants.LEVEL_MANAGER){
            return ResultGenerator.genSuccessResult(blogService.getBlogsPage(pageUtil));
        }
        return ResultGenerator.genSuccessResult(blogService.getBlogsPageByID(pageUtil,user.getAdminUserId()));
    }


    @GetMapping("/blogs")
    public String list(HttpServletRequest request) {
        request.setAttribute("path", "blogs");
        return "admin/blog";
    }

    @GetMapping("/blogs/edit")
    public String edit(HttpServletRequest request) {
        request.setAttribute("path", "edit");
        request.setAttribute("categories", categoryService.getAllCategories());
        return "admin/edit";
    }

    @GetMapping("/blogs/edit/{blogId}")
    public String edit(HttpServletRequest request, @PathVariable("blogId") Long blogId) {
        request.setAttribute("path", "edit");
        Blog blog = blogService.getBlogById(blogId);
        if (blog == null) {
            return "error/error_400";
        }
        request.setAttribute("blog", blog);
        request.setAttribute("categories", categoryService.getAllCategories());
        return "admin/edit";
    }

    @PostMapping("/blogs/save")
    @ResponseBody
    public Result save(@RequestParam("blogTitle") String blogTitle,
                       @RequestParam(name = "blogSubUrl", required = false) String blogSubUrl,
                       @RequestParam("blogCategoryId") Integer blogCategoryId,
                       @RequestParam("blogTags") String blogTags,
                       @RequestParam("blogContent") String blogContent,
                       @RequestParam("blogCoverImage") String blogCoverImage,
                       @RequestParam("blogStatus") Byte blogStatus,
                       @RequestParam("enableComment") Byte enableComment,
                       HttpServletRequest request) {
        if (!StringUtils.hasText(blogTitle)) {
            return ResultGenerator.genFailResult("请输入文章标题");
        }
        if (blogTitle.trim().length() > 150) {
            return ResultGenerator.genFailResult("标题过长");
        }
        if (!StringUtils.hasText(blogTags)) {
            return ResultGenerator.genFailResult("请输入文章标签");
        }
        if (blogTags.trim().length() > 150) {
            return ResultGenerator.genFailResult("标签过长");
        }
        if (blogSubUrl.trim().length() > 150) {
            return ResultGenerator.genFailResult("路径过长");
        }
        if (!StringUtils.hasText(blogContent)) {
            return ResultGenerator.genFailResult("请输入文章内容");
        }
        if (blogTags.trim().length() > 100000) {
            return ResultGenerator.genFailResult("文章内容过长");
        }
        if (!StringUtils.hasText(blogCoverImage)) {
            return ResultGenerator.genFailResult("封面图不能为空");
        }
        //处理博客图片内容
        List<String> picUrl = getPicUrl(blogContent);
        blogContent = convert(blogContent);
        //转移处理上传好的图片
        picUrl.add(blogCoverImage);
        if(!changePicPath(picUrl)){
            return ResultGenerator.genFailResult("图片发生错误，请联系管理员处理");
        }
        blogCoverImage = blogCoverImage.replaceAll("temp","blog");
        //////
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        Blog blog = new Blog();
        blog.setBlogTitle(blogTitle);
        blog.setBlogSubUrl(blogSubUrl);
        blog.setBlogCategoryId(blogCategoryId);
        blog.setBlogTags(blogTags);
        blog.setBlogContent(blogContent);
        blog.setBlogCoverImage(blogCoverImage);
        blog.setBlogStatus(blogStatus);
        blog.setEnableComment(enableComment);
        blog.setBlogOwner(loginUserId);
        String saveBlogResult = blogService.saveBlog(blog);
        if ("success".equals(saveBlogResult)) {
            return ResultGenerator.genSuccessResult("添加成功");
        } else {
            return ResultGenerator.genFailResult(saveBlogResult);
        }
    }

    @PostMapping("/blogs/update")
    @ResponseBody
    public Result update(@RequestParam("blogId") Long blogId,
                         @RequestParam("blogTitle") String blogTitle,
                         @RequestParam(name = "blogSubUrl", required = false) String blogSubUrl,
                         @RequestParam("blogCategoryId") Integer blogCategoryId,
                         @RequestParam("blogTags") String blogTags,
                         @RequestParam("blogContent") String blogContent,
                         @RequestParam("blogCoverImage") String blogCoverImage,
                         @RequestParam("blogStatus") Byte blogStatus,
                         @RequestParam("enableComment") Byte enableComment) {
        if (!StringUtils.hasText(blogTitle)) {
            return ResultGenerator.genFailResult("请输入文章标题");
        }
        if (blogTitle.trim().length() > 150) {
            return ResultGenerator.genFailResult("标题过长");
        }
        if (!StringUtils.hasText(blogTags)) {
            return ResultGenerator.genFailResult("请输入文章标签");
        }
        if (blogTags.trim().length() > 150) {
            return ResultGenerator.genFailResult("标签过长");
        }
        if (blogSubUrl.trim().length() > 150) {
            return ResultGenerator.genFailResult("路径过长");
        }
        if (!StringUtils.hasText(blogContent)) {
            return ResultGenerator.genFailResult("请输入文章内容");
        }
        if (blogTags.trim().length() > 100000) {
            return ResultGenerator.genFailResult("文章内容过长");
        }
        if (!StringUtils.hasText(blogCoverImage)) {
            return ResultGenerator.genFailResult("封面图不能为空");
        }
        //处理博客图片内容
        List<String> picUrl = getPicUrl(blogContent);
        blogContent = convert(blogContent);
        //转移处理上传好的图片
        picUrl.add(blogCoverImage);
        if(!changePicPath(picUrl)){
            return ResultGenerator.genFailResult("图片发生错误，请联系管理员处理");
        }
        blogCoverImage = blogCoverImage.replaceAll("temp","blog");
        //////
        Blog blog = new Blog();
        blog.setBlogId(blogId);
        blog.setBlogTitle(blogTitle);
        blog.setBlogSubUrl(blogSubUrl);
        blog.setBlogCategoryId(blogCategoryId);
        blog.setBlogTags(blogTags);
        blog.setBlogContent(blogContent);
        blog.setBlogCoverImage(blogCoverImage);
        blog.setBlogStatus(blogStatus);
        blog.setEnableComment(enableComment);
        String updateBlogResult = blogService.updateBlog(blog);
        if ("success".equals(updateBlogResult)) {
            return ResultGenerator.genSuccessResult("修改成功");
        } else {
            return ResultGenerator.genFailResult(updateBlogResult);
        }
    }

    /**
     * 博客编辑中上传图片
     * @param request
     * @param response
     * @param file
     * @throws IOException
     * @throws URISyntaxException
     */
    @PostMapping("/blogs/md/uploadfile")
    public void uploadFileByEditormd(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam(name = "editormd-image-file", required = true)
                                             MultipartFile file) throws IOException, URISyntaxException {
        ////检查是否存在文件夹
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
        /////


        String fileName = file.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //生成文件名称通用方法
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Random r = new Random();
        StringBuilder tempName = new StringBuilder();
        tempName.append(sdf.format(new Date())).append(r.nextInt(100)).append(suffixName);
        String newFileName = tempName.toString();
        //创建文件
        File destFile = new File(Constants.FILE_UPLOAD_TEMP_DIC + newFileName);
        String fileUrl = MyBlogUtils.getHost(new URI(request.getRequestURL() + "")) + "/upload/temp/" + newFileName;
        File fileDirectory = new File(Constants.FILE_UPLOAD_TEMP_DIC);
        try {
            if (!fileDirectory.exists()) {
                if (!fileDirectory.mkdir()) {
                    throw new IOException("文件夹创建失败,路径为：" + fileDirectory);
                }
            }
            file.transferTo(destFile);
            request.setCharacterEncoding("utf-8");
            response.setHeader("Content-Type", "text/html");
            response.getWriter().write("{\"success\": 1, \"message\":\"success\",\"url\":\"" + fileUrl + "\"}");
        } catch (UnsupportedEncodingException e) {
            response.getWriter().write("{\"success\":0}");
        } catch (IOException e) {
            response.getWriter().write("{\"success\":0}");
        }
    }

    @PostMapping("/blogs/delete")
    @ResponseBody
    public Result delete(@RequestBody Integer[] ids) {
        if (ids.length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        if (blogService.deleteBatch(ids)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("删除失败");
        }
    }

    //转换方法

    /**
     * 获取博客内容中的所有图片信息
     * @param input
     */
    public List<String> getPicUrl(String input){
        String regex = "!\\[\\]\\(\\S*\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        List<String> resultList = new ArrayList<>();
        while (matcher.find()) {
            resultList.add(matcher.group());
        }
        String[] resultArray = resultList.toArray(new String[0]);
        List<String> r = new ArrayList<>();
        for (String str: resultArray) {
            str = str.substring(4,str.length()-1);
            r.add(str);
        }
        return r;
    }

    /*
    博客内容所有图片地址转换
     */
    public String convert(String input){
        String regex = "temp";
        return input.replaceAll(regex, "blog");
    }

    /*
    博客内容所有图片地址转换
     */
    public boolean changePicPath(List<String> inputs){
        File file0 = new File(Constants.FILE_UPLOAD_DIC);
        File file1 = new File(Constants.FILE_UPLOAD_BLOG_DIC);
        File file2 = new File(Constants.FILE_UPLOAD_TEMP_DIC);
        File file3 = new File(Constants.FILE_UPLOAD_AVATAR_DIC);
        if(!file0.exists()){
            if(!file0.mkdir()){
                System.out.println("false0");
            }
            ;
        }
        if(!file1.exists()){
            if(!file1.mkdir()){
                System.out.println("false1");
            }
            ;
        }
        if(!file2.exists()){
            if(!file2.mkdir()){
                System.out.println("false2");
            }
        }
        if(!file3.exists()){
            if(!file3.mkdir()){
                System.out.println("false3");
            }
        }
        for (String path:inputs) {
            String str = "temp";
            if(!path.contains(str)){
                continue;
            }
            int tempIndex = path.indexOf(str);
            String picName = path.substring(tempIndex + str.length() +1, path.length());

            String newPath = Constants.FILE_UPLOAD_BLOG_DIC + picName;
            String oldPath = Constants.FILE_UPLOAD_TEMP_DIC + picName;;
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

package com.site.blog.my.core.schedule;

import com.site.blog.my.core.config.Constants;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

//定时清理上传的垃圾图片
@Component
public class TempPicClearSchedule {

//    @Scheduled(fixedRate = 3600000)

    /**
     * 遍历temp文件并全部删除
     * @throws IOException
     */
    @Scheduled(fixedRate = 360000)
    public void clearTemp() throws IOException {
        File file = new File(Constants.FILE_UPLOAD_DIC);
        File file1 = new File(Constants.FILE_UPLOAD_BLOG_DIC);
        File file2 = new File(Constants.FILE_UPLOAD_TEMP_DIC);
        File file3 = new File(Constants.FILE_UPLOAD_AVATAR_DIC);
        if(!file.exists()){
            if(!file.mkdir()){
                System.out.println("false");
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
        Path dir = Paths.get(Constants.FILE_UPLOAD_TEMP_DIC);
        Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(
                path -> {
                    if(!path.equals(dir)){
                        try{
                            Files.delete(path);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
        );
    }


}

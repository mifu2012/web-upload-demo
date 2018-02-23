package com.mif.demo.webupload;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

/**
 * @description: 文件上传
 * @author: mif
 * @date: 2018/2/9
 * @time: 11:06
 * @copyright: 拓道金服 Copyright (c) 2017
 */
@Controller
public class UploadController {

    private static final String UPLOAD_PATH = "E:/";

    @GetMapping("/")
    public String page() {
        return "upload";
    }

//    @PostMapping("upload")
//    @ResponseBody
//    public Boolean upload(MultipartFile file) {
//        File saveFile = new File("E:", file.getOriginalFilename());
//        try {
//            //将文件保存
//            file.transferTo(saveFile);
//        } catch (IOException e) {
//            return false;
//        }
//        return true;
//    }

    /**
     * 检查文件是否存在
     *
     * @param md5File 文件md5
     * @return true/false
     */
    @PostMapping("checkFile")
    @ResponseBody
    public Boolean checkFile(@RequestParam(value = "md5File") String md5File) {
        Boolean exist = false;

        //实际项目中，这个md5File唯一值，应该保存到数据库或者缓存中，通过判断唯一值存不存在，来判断文件存不存在，这里我就不演示了
		/*if(true) {
			exist = true;
		}*/
        return exist;
    }

    /**
     * 检查分片存不存在
     */
    @PostMapping("checkChunk")
    @ResponseBody
    public Boolean checkChunk(@RequestParam(value = "md5File") String md5File,
                              @RequestParam(value = "chunk") Integer chunk) {
        Boolean exist = false;
        //分片存放目录
        String path = UPLOAD_PATH + md5File + "/";
        //分片名
        String chunkName = chunk + ".tmp";
        File file = new File(path + chunkName);
        if (file.exists()) {
            exist = true;
        }
        return exist;
    }

    /**
     * 修改上传
     */
    @PostMapping("upload")
    @ResponseBody
    public Boolean upload(@RequestParam(value = "file") MultipartFile file,
                          @RequestParam(value = "md5File") String md5File,
                          @RequestParam(value = "chunk", required = false) Integer chunk) { //第几片，从0开始
        String path = UPLOAD_PATH + md5File + "/";
        File dirFile = new File(path);
        //目录不存在，创建目录
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        String chunkName;
        //表示是小文件，还没有一片
        if (chunk == null) {
            chunkName = "0.tmp";
        } else {
            chunkName = chunk + ".tmp";
        }
        String filePath = path + chunkName;
        File saveFile = new File(filePath);

        try {
            if (!saveFile.exists()) {
                //文件不存在，则创建
                saveFile.createNewFile();
            }
            //将文件保存
            file.transferTo(saveFile);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 合成分片
     */
    @PostMapping("merge")
    @ResponseBody
    public Boolean merge(@RequestParam(value = "chunks", required = false) Integer chunks,
                         @RequestParam(value = "md5File") String md5File,
                         @RequestParam(value = "name") String name) throws Exception {
        //合成后的文件
        FileOutputStream fileOutputStream = new FileOutputStream(UPLOAD_PATH + name);
        try {
            byte[] buf = new byte[1024];
            for (long i = 0; i < chunks; i++) {
                String chunkFile = i + ".tmp";
                File file = new File(UPLOAD_PATH + md5File + "/" + chunkFile);
                InputStream inputStream = new FileInputStream(file);
                int len = 0;
                while ((len = inputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, len);
                }
                inputStream.close();
            }

            // 删除 MD5文件夹
            deleteFileOrFolder(Paths.get(UPLOAD_PATH + md5File));
        } catch (Exception e) {
            return false;
        } finally {
            fileOutputStream.close();
        }
        return true;
    }


    private void deleteFileOrFolder(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                return handleException(e);
            }

            private FileVisitResult handleException(final IOException e) {
                e.printStackTrace(); // replace with more robust error handling
                return TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
                    throws IOException {
                if (e != null) {
                    return handleException(e);
                }
                Files.delete(dir);
                return CONTINUE;
            }
        });
    }

    ;
}

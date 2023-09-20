package com.hj.hjBi.utils;

import com.hj.hjBi.common.ErrorCode;
import com.hj.hjBi.exception.BusinessException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.hj.hjBi.constant.FileConstant.BASEPATH;

/**
 * @author: WHJ
 * @createTime: 2023-09-20 11:44
 * @description:
 */
public class FileUtils {

    public static String uploadFile(MultipartFile file) {
        //获取原文件名
        String originalFilename = file.getOriginalFilename();
        if (Strings.isEmpty(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //获取后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (Strings.isEmpty(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        File dir = new File(System.getProperty("user.dir") + BASEPATH);
        //如果文件夹不存在则新建文件夹
        if (!dir.exists()) {
            dir.mkdir();
        }
        File localFile = new File(System.getProperty("user.dir") + BASEPATH + originalFilename);
        try {
            //将文件从tomcat临时目录转移到指定的目录
            file.transferTo(localFile);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        byte[] imageStream = getImageStream(localFile);
        String fileName = QiNiuUtils.uploadByBytes(imageStream);
        localFile.delete();
        //上传七牛云
        return fileName;
    }

    public static byte[] getImageStream(File imageFile) {
        byte[] buffer = null;
        FileInputStream fis;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            fis = new FileInputStream(imageFile);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}

package com.hj.hjBi.controller;

import com.hj.hjBi.common.BaseResponse;
import com.hj.hjBi.common.ErrorCode;
import com.hj.hjBi.common.ResultUtils;
import com.hj.hjBi.exception.BusinessException;
import com.hj.hjBi.model.entity.User;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.hj.hjBi.service.UserService;
import com.hj.hjBi.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.hj.hjBi.constant.FileConstant.QINIU_HOST;

/**
 * 文件接口
 *
 * @author: WHJ
 * @createTime: 2023-09-13 09:48
 * @description:
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param request
     * @param request
     * @return
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           HttpServletRequest request) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请登录");
        }
        String filename = FileUtils.uploadFile(multipartFile);
        String fileUrl = QINIU_HOST + filename;
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserAvatar(fileUrl);
        boolean success = userService.updateById(user);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
        }
        return ResultUtils.success(fileUrl);

    }


}

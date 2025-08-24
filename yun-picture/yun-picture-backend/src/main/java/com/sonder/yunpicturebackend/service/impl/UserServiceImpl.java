package com.sonder.yunpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.sonder.yunpicturebackend.constant.UserConstant;
import com.sonder.yunpicturebackend.exception.BusinessException;
import com.sonder.yunpicturebackend.exception.ErrorCode;
import com.sonder.yunpicturebackend.exception.ThrowUtils;
import com.sonder.yunpicturebackend.manager.CosManager;
import com.sonder.yunpicturebackend.manager.auth.StpKit;
import com.sonder.yunpicturebackend.manager.upload.FilePictureUpload;
import com.sonder.yunpicturebackend.manager.upload.PictureUploadTemplate;
import com.sonder.yunpicturebackend.model.dto.file.UploadPictureResult;
import com.sonder.yunpicturebackend.model.dto.user.UserInfoUpdateRequest;
import com.sonder.yunpicturebackend.model.dto.user.UserQueryRequest;
import com.sonder.yunpicturebackend.model.dto.user.UserRegisterRequest;
import com.sonder.yunpicturebackend.model.dto.user.VipCode;
import com.sonder.yunpicturebackend.model.entity.User;
import com.sonder.yunpicturebackend.model.enums.UserRoleEnum;
import com.sonder.yunpicturebackend.model.vo.LoginUserVO;
import com.sonder.yunpicturebackend.model.vo.UserVO;
import com.sonder.yunpicturebackend.service.UserService;
import com.sonder.yunpicturebackend.mapper.UserMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
* @author Sonder
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-31 20:46:47
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @javax.annotation.Resource
    private CosManager cosManager;

    @javax.annotation.Resource
    private FilePictureUpload filePictureUpload;

    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {
        //1.校验参数
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if(StrUtil.hasBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户过短");
        }
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //2.检查用户账户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        Long count = this.baseMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户已存在");
        }
        //3.密码加密、加盐
        String encryptPassword = getEncryptPassword(userPassword);
        //4.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("LaLa");
        user.setUserAvatar("https://sonder-1367770210.cos.ap-guangzhou.myqcloud.com/test/%E6%B4%9B%E7%90%AA%E5%B8%8C.jpg");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if(StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户错误");
        }
        if(userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
        }
        //2.对用户传递的密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //3.查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if(user == null){
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //4.保存用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        //5.用户信息脱敏
        return getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if(user == null){
            return null;
        }
        return BeanUtil.copyProperties(user, LoginUserVO.class);
    }

    @Override
    public UserVO getUserVO(User user) {
        if(user == null){
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if(CollUtil.isEmpty(userList)){
            return Collections.emptyList();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        //加盐，混淆密码
        final String SALT = "sonder";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        //判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null || currentUser.getId() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        //从数据库中查询用户（追求性能的话可以直接返回）
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        //判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        //移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if(userQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }


    // region ----------------- 以下代码为会员兑换功能 -----------------

    @Autowired
    private ResourceLoader resourceLoader;

    // 在类中添加锁对象，防止并发问题
    private final ReentrantLock fileLock = new ReentrantLock();

    /**
     * 兑换会员
     * @param user
     * @param vipCode
     * @return
     */
    // 实现exchangeVip方法
    @Override
    public boolean exchangeVip(User user, String vipCode) {
        // 1. 参数校验
        if (user == null || StrUtil.isBlank(vipCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 2. 读取并校验兑换码
        VipCode targetCode = validateAndMarkVipCode(vipCode);

        // 3. 更新用户信息
        updateUserVipInfo(user, targetCode.getCode());
        return true;
    }

    /**
     * 校验并标记兑换码为已使用
     * @param vipCode
     * @return
     */
    private VipCode validateAndMarkVipCode(String vipCode) {
        fileLock.lock(); // 加锁
        try {
            // 读取 JSON 文件
            JSONArray jsonArray = readVipCodeFile();

            // 查找匹配的未使用兑换码
            List<VipCode> codes = JSONUtil.toList(jsonArray, VipCode.class);
            VipCode target = codes.stream()
                    .filter(code -> code.getCode().equals(vipCode) && !code.isHasUsed())
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "无效的兑换码"));

            // 标记为已使用
            target.setHasUsed(true);

            // 写回文件
            writeVipCodeFile(JSONUtil.parseArray(codes));
            return target;
        } finally {
            fileLock.unlock(); // 解锁
        }
    }

    /**
     * 读取兑换码文件
     * @return
     */
    private JSONArray readVipCodeFile() {
        try {
            Resource resource = resourceLoader.getResource("classpath:biz/vipCode.json");
            String content = FileUtil.readString(resource.getFile(), Charset.defaultCharset());
            return JSONUtil.parseArray(content);
        } catch (IOException e) {
            log.error("读取兑换码文件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙");
        }
    }

    /**
     * 写入兑换码文件
     * @param jsonArray
     */
    private void writeVipCodeFile(JSONArray jsonArray) {
        try {
            Resource resource = resourceLoader.getResource("classpath:biz/vipCode.json");
            FileUtil.writeString(jsonArray.toStringPretty(), resource.getFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("更新兑换码文件失败 ", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙");
        }
    }

    /**
     * 更新用户信息
     * @param user
     * @param vipCode
     */
    private void updateUserVipInfo(User user, String vipCode) {
        // 计算过期时间（当前时间或当前过期时间 + 1 年）
        Date localExpireTime = user.getVipExpireTime() == null ? new Date() : user.getVipExpireTime();
        Date expireTime = DateUtil.offsetMonth(localExpireTime, 12);

        // 构建对象
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setVipCode(vipCode);
        updateUser.setVipExpireTime(expireTime);
        updateUser.setUserRole(UserConstant.VIP_ROLE);
        updateUser.setUpdateTime(new Date());

        // 更新数据库
        boolean result = this.updateById(updateUser);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "开通会员失败，操作数据库失败");
        }
    }

    // endregion ----------------- 会员兑换功能部分结束 -----------------

    // region ----------------- 以下代码为用户修改个人信息功能 -----------------
    @Override
    public void updateUserInfo(UserInfoUpdateRequest userInfoUpdateRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(userInfoUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = this.getLoginUser(request);

        String userName = StrUtil.blankToDefault(StrUtil.trim(userInfoUpdateRequest.getUserName()), loginUser.getUserName());
        String userProfile = userInfoUpdateRequest.getUserProfile();

        ThrowUtils.throwIf(StrUtil.length(userName) > 50, ErrorCode.PARAMS_ERROR, "昵称过长");

        // 更新用户信息
        User updateUser = new User();
        updateUser.setId(loginUser.getId());
        updateUser.setUserName(userName);
        updateUser.setUserProfile(userProfile);
        updateUser.setEditTime(new Date());
        updateUser.setUpdateTime(new Date());
        boolean ok = this.updateById(updateUser);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR, "更新失败");
    }

    @Override
    public String uploadAvatar(MultipartFile file, HttpServletRequest request) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "头像不能为空");
        User loginUser = getLoginUser(request);

        // 校验文件类型与大小（示例：5MB，图片后缀）
        String originalName = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalName);
        ThrowUtils.throwIf(!StrUtil.equalsAnyIgnoreCase(suffix, "jpg", "jpeg", "png", "webp"),
                ErrorCode.PARAMS_ERROR, "仅支持图片");
        ThrowUtils.throwIf(file.getSize() > 5 * 1024 * 1024, ErrorCode.PARAMS_ERROR, "头像不能超过 5MB");

        // 路径：userAvatar/{uid}/{uuid}.{ext}
        String uploadPathPrefix = String.format("public/%s/userAvatar", loginUser.getId());

        // === 上传到 COS ===
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(file, uploadPathPrefix);

        // 写回用户表
        User updateUserAvatar = new User();
        updateUserAvatar.setId(loginUser.getId());
        updateUserAvatar.setUserAvatar(uploadPictureResult.getUrl());
        boolean ok = this.updateById(updateUserAvatar);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR, "更新头像失败");

        return updateUserAvatar.getUserAvatar();
    }
    // endregion ----------------- 用户修改个人信息功能结束 -----------------
}





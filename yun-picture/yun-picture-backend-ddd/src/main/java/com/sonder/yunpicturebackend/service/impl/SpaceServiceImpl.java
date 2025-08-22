package com.sonder.yunpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sonder.yunpicture.infrastructure.exception.BusinessException;
import com.sonder.yunpicture.infrastructure.exception.ErrorCode;
import com.sonder.yunpicture.infrastructure.exception.ThrowUtils;
import com.sonder.yunpicture.infrastructure.mapper.SpaceMapper;
import com.sonder.yunpicturebackend.model.dto.space.SpaceAddRequest;
import com.sonder.yunpicturebackend.model.dto.space.SpaceQueryRequest;
import com.sonder.yunpicturebackend.model.entity.Space;
import com.sonder.yunpicturebackend.model.entity.SpaceUser;
import com.sonder.yunpicture.domain.user.entity.User;
import com.sonder.yunpicturebackend.model.enums.SpaceLevelEnum;
import com.sonder.yunpicturebackend.model.enums.SpaceRoleEnum;
import com.sonder.yunpicturebackend.model.enums.SpaceTypeEnum;
import com.sonder.yunpicturebackend.model.vo.SpaceVO;
import com.sonder.yunpicture.interfaces.vo.user.UserVO;
import com.sonder.yunpicturebackend.service.SpaceService;
import com.sonder.yunpicturebackend.service.SpaceUserService;
import com.sonder.yunpicture.application.service.UserApplicationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sonder
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-08-15 21:13:10
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserService spaceUserService;

    // TODO: 可选 为了方便部署，注释掉分表
//    @Resource
//    @Lazy
//    private DynamicShardingManager dynamicShardingManager;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1.校验参数默认值
        // 转换实体类和 DTO
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        if(StrUtil.isBlank(space.getSpaceName())){
            space.setSpaceName("默认空间");
        }
        if(space.getSpaceLevel() == null){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 填充容量和大小
        this.fillSpaceBySpaceLevel(space);
        // 2.校验参数
        this.validSpace(space, true);
        // 3.校验权限，非管理员只能创建普通级级别的空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if(SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !loginUser.isAdmin()){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        // 4.控制同一用户只能创建一个私有空间，以及一个团队空间
        String lock = String.valueOf(userId).intern();
        synchronized (lock){
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 判断是否已有空间
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType, space.getSpaceType())
                        .exists();
                // 如果已有空间，则不能再创建
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户每类空间仅能创建一个");
                // 创建空间
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
                // 创建成功后，如果是团队空间，关联新增团队成员记录
                if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    result = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败！");
                }
//                // 创建分表 （仅对团队空间生效）   TODO: 为方便部署，暂时不使用
//                dynamicShardingManager.createSpacePictureTable(space);
                // 返回创建的空间id
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        //创建时校验
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类别不能为空");
            }
        }
        // 修改数据时，空间名称进行校验
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        // 修改数据时，空间级别进行校验
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        // 修改数据时，空间类型进行校验
        if (spaceType != null && spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类别不存在");
        }
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userApplicationService.getUserById(userId);
            UserVO userVO = userApplicationService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }

        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1.关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2.填充用户信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userApplicationService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if(spaceLevelEnum != null){
            long maxSize = spaceLevelEnum.getMaxSize();
            if(space.getMaxSize() == null){
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if(space.getMaxCount() == null){
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 仅本人或管理员可编辑
        if (!space.getUserId().equals(loginUser.getId()) && !loginUser.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}





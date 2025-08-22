package com.sonder.yunpicture.shared.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.sonder.yunpicture.shared.auth.SpaceUserAuthManager;
import com.sonder.yunpicture.shared.auth.model.SpaceUserPermissionConstant;
import com.sonder.yunpicture.domain.picture.entity.Picture;
import com.sonder.yunpicture.domain.space.entity.Space;
import com.sonder.yunpicture.domain.user.entity.User;
import com.sonder.yunpicture.domain.space.valueobject.SpaceTypeEnum;
import com.sonder.yunpicture.application.service.PictureApplicationService;
import com.sonder.yunpicture.application.service.SpaceApplicationService;
import com.sonder.yunpicture.application.service.UserApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 拦截器，建立连接前要先校验
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private PictureApplicationService pictureApplicationService;

    @Resource
    private SpaceApplicationService spaceApplicationService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 建立连接前要先校验
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes 给 WebSocketSession 会话设置属性
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 获取登录用户
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 从请求中获取参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("WebSocket 连接建立失败，缺少参数 pictureId");
                return false;
            }
            // 获取当前登录用户
            User loginUser = userApplicationService.getLoginUser(httpServletRequest);
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("WebSocket 连接建立失败，用户未登录");
                return false;
            }
            // 校验用户是否有编辑当前图片的权限
            Picture picture = pictureApplicationService.getById(pictureId);
            if (ObjUtil.isEmpty(picture)) {
                log.error("WebSocket 连接建立失败，图片不存在");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            // 如果是团队空间并且有编辑者权限，才能建立链接
            if (spaceId != null) {
                space = spaceApplicationService.getById(spaceId);
                if (ObjUtil.isEmpty(space)) {
                    log.error("WebSocket 连接建立失败，空间不存在");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.error("WebSocket 连接建立失败，非团队空间");
                    return false;
                }
            }
            // spaceId 为空表示为公共空间，只有管理员可以协同编辑
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                log.error("WebSocket 连接建立失败，用户无编辑权限");
                return false;
            }
            // 设置用户登录信息等属性到 WebSocket 会话中
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId)); // 转换为 Long 类型

        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}

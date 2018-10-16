package com.kiss.account.service;

import com.kiss.account.client.PermissionClient;
import com.kiss.account.dao.PermissionDao;
import com.kiss.account.entity.Permission;
import com.kiss.account.entity.PermissionModule;
import com.kiss.account.input.CreatePermissionInput;
import com.kiss.account.input.CreatePermissionModuleInput;
import com.kiss.account.input.UpdatePermissionInput;
import com.kiss.account.input.UpdatePermissionModuleInput;
import com.kiss.account.output.BindPermissionOutput;
import com.kiss.account.output.PermissionModuleOutput;
import com.kiss.account.output.PermissionOutput;
import com.kiss.account.status.AccountStatusCode;
import com.kiss.account.utils.ResultOutputUtil;
import com.kiss.account.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import output.ResultOutput;

import java.util.ArrayList;
import java.util.List;

@RestController
@Api(tags = "Permission", description = "权限相关接口")
public class PermissionServiceImpl implements PermissionClient {

    @Autowired
    private PermissionDao permissionDao;

    @Override
    @ApiOperation(value = "创建权限")
    @Transactional
    public ResultOutput<PermissionOutput> createPermission(@Validated @RequestBody CreatePermissionInput createPermissionInput) {

        // 1. 查询权限所属模块信息
        PermissionModule permissionModule = permissionDao.getPermissionModuleById(createPermissionInput.getModuleId());
        if (permissionModule == null) {
            return ResultOutputUtil.error(AccountStatusCode.PERMISSION_MODULE_NOT_EXIST);
        }

        // 2. 添加权限信息
        Permission permission = new Permission();
        permission.setName(createPermissionInput.getName());
        permission.setModuleId(createPermissionInput.getModuleId());
        permission.setType(createPermissionInput.getType());
        permission.setCode(createPermissionInput.getCode());
        // permission.setDataFields();
        permission.setStatus(createPermissionInput.getStatus());
        permission.setSeq(10);
        permission.setRemark(createPermissionInput.getRemark());
        permission.setOperatorId(UserUtil.getUserId());
        permission.setOperatorName(UserUtil.getUsername());
        permission.setOperatorIp("0.0.0.0");
        Permission exist = permissionDao.getPermissionByNameOrCode(permission);

        if (exist != null) {
            return ResultOutputUtil.error(AccountStatusCode.PERMISSION_EXIST);
        }

        permissionDao.createPermission(permission);

        // 3. 更新权限所属模块的权限数
        permissionDao.updatePermissionModulePermissionsCount(permission.getModuleId(), +1);

        // 4. 更新权限所属模块父模块权限数
        String[] modulesIds = permissionModule.getLevel().split(",");
        for (String moduleIdString : modulesIds) {
            permissionDao.updatePermissionModulePermissionsCount(Integer.parseInt(moduleIdString), +1);
        }


        PermissionOutput permissionOutput = new PermissionOutput();
        BeanUtils.copyProperties(permission, permissionOutput);
        return ResultOutputUtil.success(permissionOutput);
    }

    /**
     * 创建权限模块
     */
    @Override
    @ApiOperation(value = "创建权限模块")
    public ResultOutput<PermissionModuleOutput> createPermissionModule(@Validated @RequestBody CreatePermissionModuleInput permissionModuleInput) {

        PermissionModule permissionModule = permissionDao.getPermissionModuleByName(permissionModuleInput.getName());

        if (permissionModule != null) {
            return ResultOutputUtil.error(AccountStatusCode.PERMISSION_MODULE_EXIST);
        }

        permissionModule = new PermissionModule();
        PermissionModule parentPermissionModule = new PermissionModule();

        // 1. 如果父模块ID不等于0，则查询父模块信息；
        //    * 绑定权限模块 level， 等于父权限模块的 level 拼接父权限模块的 id；
        if (permissionModuleInput.getParentId() != 0) {
            parentPermissionModule = permissionDao.getPermissionModuleById(permissionModuleInput.getParentId());
            if (parentPermissionModule == null) {
                return ResultOutputUtil.error(AccountStatusCode.PERMISSION_MODULE_NOT_EXIST);
            }
            permissionModule.setLevel(String.format("%s,%d", parentPermissionModule.getLevel(), parentPermissionModule.getId()));
        }

        // 2. 添加权限模块信息
        permissionModule.setName(permissionModuleInput.getName());
        permissionModule.setParentId(permissionModuleInput.getParentId());
        permissionModule.setSeq(100);
        permissionModule.setRemark(permissionModuleInput.getRemark());
        permissionModule.setPermissions(0);
        permissionModule.setOperatorId(UserUtil.getUserId());
        permissionModule.setOperatorName(UserUtil.getUsername());
        permissionModule.setOperatorIp("0.0.0.0");
        permissionModule.setStatus(1);


        permissionDao.createPermissionModule(permissionModule);

        PermissionModuleOutput permissionModuleOutput = new PermissionModuleOutput();
        BeanUtils.copyProperties(permissionModule, permissionModuleOutput);

        return ResultOutputUtil.success(permissionModuleOutput);
    }

    @Override
    @ApiOperation(value = "获取权限列表")
    public ResultOutput<List<PermissionOutput>> getPermissions() {

        List<PermissionOutput> permissions = permissionDao.getPermissions();

        return ResultOutputUtil.success(permissions);
    }

    @Override
    @ApiOperation(value = "获取权限详情")
    public ResultOutput<PermissionOutput> getPermission() {
        return null;
    }

    @Override
    @ApiOperation(value = "获取可以绑定的权限列表")
    public ResultOutput<List<BindPermissionOutput>> getbindPermissions() {

        List<BindPermissionOutput> bindPermissionOutputs = permissionDao.getBindPermissions();

        return ResultOutputUtil.success(bindPermissionOutputs);
    }

    @Override
    @ApiOperation(value = "获取权限模块列表")
    public ResultOutput<List<PermissionModuleOutput>> getPermissionModules() {

        List<PermissionModule> permissionModules = permissionDao.getPermissionModules();
        List<PermissionModuleOutput> permissionModuleOutputs = new ArrayList<>();

        for (PermissionModule permissionModule : permissionModules) {
            PermissionModuleOutput permissionModuleOutput = new PermissionModuleOutput();
            BeanUtils.copyProperties(permissionModule, permissionModuleOutput);
            permissionModuleOutputs.add(permissionModuleOutput);
        }

        return ResultOutputUtil.success(permissionModuleOutputs);
    }

    @Override
    @ApiOperation(value = "获取权限模块列表")
    public ResultOutput<List<PermissionModuleOutput>> getBindPermissionModules() {

        List<PermissionModule> permissionModules = permissionDao.getBindPermissionModules();
        List<PermissionModuleOutput> permissionModuleOutputs = new ArrayList<>();

        for (PermissionModule permissionModule : permissionModules) {
            PermissionModuleOutput permissionModuleOutput = new PermissionModuleOutput();
            BeanUtils.copyProperties(permissionModule, permissionModuleOutput);
            permissionModuleOutputs.add(permissionModuleOutput);
        }

        return ResultOutputUtil.success(permissionModuleOutputs);
    }

    @Override
    @ApiOperation(value = "更新权限")
    public ResultOutput<PermissionOutput> updatePermission(@Validated @RequestBody UpdatePermissionInput updatePermissionInput) {
        Permission permission = new Permission();
        permission.setCode(updatePermissionInput.getCode());
        permission.setName(updatePermissionInput.getName());
        Permission exist = permissionDao.getPermissionByNameOrCode(permission);

        if (exist != null) {
            return ResultOutputUtil.error(AccountStatusCode.PERMISSION_EXIST);
        }

        PermissionOutput permissionOutput = new PermissionOutput();
        BeanUtils.copyProperties(updatePermissionInput, permissionOutput);
        Integer count = permissionDao.updatePermission(permissionOutput);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.PUT_PERMISSION_FAILD);
        }

        return ResultOutputUtil.success(permissionOutput);
    }

    @Override
    @ApiOperation(value = "更新权限模块")
    public ResultOutput<PermissionModuleOutput> updatePermissionModule(@Validated @RequestBody UpdatePermissionModuleInput updatePermissionModuleInput) {
        PermissionModule permissionModule = permissionDao.getPermissionModuleByName(updatePermissionModuleInput.getName());

        if (permissionModule != null) {
            return ResultOutputUtil.error(AccountStatusCode.PERMISSION_MODULE_EXIST);
        }

        PermissionModuleOutput permissionModuleOutput = new PermissionModuleOutput();
        BeanUtils.copyProperties(updatePermissionModuleInput, permissionModuleOutput);
        Integer count = permissionDao.updatePermissionModule(permissionModuleOutput);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.PUT_PERMISSION_MODULE_FAILD);
        }

        return ResultOutputUtil.success(permissionModuleOutput);
    }

    @Override
    @ApiOperation(value = "删除权限")
    public ResultOutput deletePermission(@RequestParam("id") Integer id) {
        Integer count = permissionDao.deletePermission(id);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.DELETE_PERMISSION_FAILED);
        }

        return ResultOutputUtil.success();
    }

    @Override
    @ApiOperation(value = "删除权限模块")
    public ResultOutput deletePermissionModule(@RequestParam("id") Integer id) {

        List<Permission> permissions = permissionDao.getPermissionByModuleId(id);

        if (permissions != null && permissions.size() != 0) {
            return ResultOutputUtil.error(AccountStatusCode.PERMISSION_MODULE_IS_NOT_EMPTY);
        }

        Integer count = permissionDao.deletePermissionModule(id);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.DELETE_PERMISSION_MODULE_FAILED);
        }

        return ResultOutputUtil.success();
    }
}

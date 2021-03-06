package com.kiss.account.controller;

import com.kiss.account.client.PermissionModuleClient;
import com.kiss.account.entity.Permission;
import com.kiss.account.entity.PermissionModule;
import com.kiss.account.input.CreatePermissionModuleInput;
import com.kiss.account.input.UpdatePermissionModuleInput;
import com.kiss.account.output.PermissionModuleOutput;
import com.kiss.account.entity.OperationTargetType;
import com.kiss.account.status.AccountStatusCode;
import com.kiss.account.validator.PermissionModuleValidator;
import entity.Guest;
import exception.StatusException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import utils.ThreadLocalUtil;

import java.util.ArrayList;
import java.util.List;

@RestController
@Api(tags = "Permission", description = "权限相关接口")
public class PermissionModuleController extends BaseController implements PermissionModuleClient {

    @Autowired
    private PermissionModuleValidator permissionModuleValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(permissionModuleValidator);
    }

    /**
     * 创建权限模块
     */
    @Override
    @ApiOperation(value = "创建权限模块")
    public PermissionModuleOutput createPermissionModule(@Validated @RequestBody CreatePermissionModuleInput permissionModuleInput) {

        Guest guest = ThreadLocalUtil.getGuest();
        PermissionModule permissionModule = new PermissionModule();
        permissionModule.setName(permissionModuleInput.getName());
        permissionModule.setParentId(permissionModuleInput.getParentId());
        permissionModule.setSeq(100);
        permissionModule.setRemark(permissionModuleInput.getRemark());
        permissionModule.setLevel("0,");
        permissionModule.setPermissions(0);
        permissionModule.setOperatorId(guest.getId());
        permissionModule.setOperatorName(guest.getName());
        permissionModule.setOperatorIp(guest.getIp());
        permissionModule.setStatus(1);
        permissionDao.createPermissionModule(permissionModule);
        PermissionModuleOutput permissionModuleOutput = new PermissionModuleOutput();
        BeanUtils.copyProperties(permissionModule, permissionModuleOutput);
        operationLogService.saveOperationLog(guest, null, permissionModule, "id", OperationTargetType.TYPE_PERMISSION_MODULE);

        return permissionModuleOutput;
    }

    @Override
    @ApiOperation(value = "获取权限模块列表")
    public List<PermissionModuleOutput> getPermissionModules() {

        List<PermissionModule> permissionModules = permissionDao.getPermissionModules();
        List<PermissionModuleOutput> permissionModuleOutputs = new ArrayList<>();

        for (PermissionModule permissionModule : permissionModules) {
            PermissionModuleOutput permissionModuleOutput = new PermissionModuleOutput();
            BeanUtils.copyProperties(permissionModule, permissionModuleOutput);
            permissionModuleOutputs.add(permissionModuleOutput);
        }

        return permissionModuleOutputs;
    }

    @Override
    @ApiOperation(value = "获取权限模块列表")
    public List<PermissionModuleOutput> getBindPermissionModules() {

        List<PermissionModule> permissionModules = permissionDao.getBindPermissionModules();
        List<PermissionModuleOutput> permissionModuleOutputs = new ArrayList<>();

        for (PermissionModule permissionModule : permissionModules) {
            PermissionModuleOutput permissionModuleOutput = new PermissionModuleOutput();
            BeanUtils.copyProperties(permissionModule, permissionModuleOutput);
            permissionModuleOutputs.add(permissionModuleOutput);
        }

        return permissionModuleOutputs;
    }

    @Override
    @ApiOperation(value = "更新权限模块")
    public PermissionModuleOutput updatePermissionModule(@Validated @RequestBody UpdatePermissionModuleInput updatePermissionModuleInput) {

        Guest guest = ThreadLocalUtil.getGuest();
        PermissionModuleOutput permissionModuleOutput = new PermissionModuleOutput();
        BeanUtils.copyProperties(updatePermissionModuleInput, permissionModuleOutput);
        Integer count = permissionDao.updatePermissionModule(permissionModuleOutput);

        if (count == 0) {
            throw new StatusException(AccountStatusCode.PUT_PERMISSION_MODULE_FAILD);
        }

        return permissionModuleOutput;
    }

    @Override
    @ApiOperation(value = "删除权限模块")
    public void deletePermissionModule(@RequestParam("id") Integer id) {

        List<Permission> permissions = permissionDao.getPermissionByModuleId(id);

        if (permissions != null && permissions.size() != 0) {
            throw new StatusException(AccountStatusCode.PERMISSION_MODULE_NOT_EMPTY);
        }

        List<PermissionModule> permissionModules = permissionDao.getPermissionModuleChildrenByParentId(id);

        if (permissionModules != null && permissionModules.size() != 0) {
            throw new StatusException(AccountStatusCode.PERMISSION_MODULE_NOT_EMPTY);
        }

        Guest guest = ThreadLocalUtil.getGuest();
        PermissionModule oldValue = permissionDao.getPermissionModuleById(id);
        Integer count = permissionDao.deletePermissionModuleById(id);

        if (count == 0) {
            throw new StatusException(AccountStatusCode.DELETE_PERMISSION_MODULE_FAILED);
        }

        operationLogService.saveOperationLog(guest, oldValue, null, "id", OperationTargetType.TYPE_PERMISSION_MODULE);

    }
}

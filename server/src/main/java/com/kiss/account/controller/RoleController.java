package com.kiss.account.controller;

import com.alibaba.fastjson.JSONObject;
import com.kiss.account.client.RoleClient;
import com.kiss.account.dao.AccountDao;
import com.kiss.account.dao.RoleDao;
import com.kiss.account.entity.AccountRole;
import com.kiss.account.entity.Role;
import com.kiss.account.entity.RolePermission;
import com.kiss.account.input.*;
import com.kiss.account.output.AccountRoleOutput;
import com.kiss.account.output.RoleOutput;
import com.kiss.account.output.RolePermissionOutput;
import com.kiss.account.service.OperationLogService;
import com.kiss.account.entity.OperationTargetType;
import com.kiss.account.status.AccountStatusCode;
import com.kiss.account.utils.ResultOutputUtil;
import com.kiss.account.validator.RoleValidator;
import entity.Guest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import output.ResultOutput;
import utils.ThreadLocalUtil;

@RestController
@Api(tags = "Role", description = "角色相关接口")
public class RoleController extends BaseController implements RoleClient {

    @Autowired
    private RoleValidator roleValidator;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.setValidator(roleValidator);
    }

    @Override
    @ApiOperation("添加角色")
    public ResultOutput<RoleOutput> createRole(@Validated @RequestBody CreateRoleInput createRoleInput) {

        Guest guest = ThreadLocalUtil.getGuest();
        Role role = new Role();
        BeanUtils.copyProperties(createRoleInput, role);
        role.setOperatorId(guest.getId());
        role.setOperatorName(guest.getUsername());
        role.setOperatorIp(guest.getIp());
        roleDao.createRole(role);
        RoleOutput roleOutput = new RoleOutput();
        BeanUtils.copyProperties(role, roleOutput);
        operationLogService.saveOperationLog(guest,null,role,"id",OperationTargetType.TYPE_ROLE);

        return ResultOutputUtil.success(roleOutput);
    }

    @Override
    @ApiOperation("分配角色权限")
    @Transactional
    public ResultOutput<List<RolePermissionOutput>> bindRolePermissions(@Validated @RequestBody BindPermissionToRoleInput bindPermissionToRoleInput) {

        Guest guest = ThreadLocalUtil.getGuest();
        roleDao.getRolePermissionsByRoleId(bindPermissionToRoleInput.getRoleId());
        List<BindRoleDataPermissions> permissions = bindPermissionToRoleInput.getPermissions();
        List<RolePermission> rolePermissions = new ArrayList<>();

        for (BindRoleDataPermissions dataPermission : permissions) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(bindPermissionToRoleInput.getRoleId());
            rolePermission.setOperatorId(guest.getId());
            rolePermission.setOperatorName(guest.getUsername());
            rolePermission.setOperatorIp(guest.getIp());
            rolePermission.setPermissionId(dataPermission.getPermissionId());
            rolePermission.setLimitDescription(dataPermission.getLimitDescription());
            String limitString = dataPermission.getLimitString();
            if (!StringUtils.isEmpty(limitString) && !limitString.startsWith("page") && !limitString.startsWith("component")) {
                ResultOutput resultOutput = analyseLimitString(limitString);

                if (resultOutput.getCode() != 200) {
                    return resultOutput;
                }

                rolePermission.setLimitScope(resultOutput.getData().toString());
                rolePermission.setLimitString(limitString.substring(limitString.indexOf("?") + 1));
            }

            rolePermissions.add(rolePermission);
        }

        List<RolePermission> oldRolePermissions = roleDao.getRolePermissionsByRoleId(bindPermissionToRoleInput.getRoleId());
        roleDao.deleteRolePermissionsByRoleId(bindPermissionToRoleInput.getRoleId());
        roleDao.bindPermissionsToRole(rolePermissions);
        List<RolePermissionOutput> rolePermissionOutputs = new ArrayList<>();

        for (RolePermission rolePermission : rolePermissions) {
            RolePermissionOutput rolePermissionOutput = new RolePermissionOutput();
            BeanUtils.copyProperties(rolePermission, rolePermissionOutput);
            rolePermissionOutputs.add(rolePermissionOutput);
        }

        BindPermissionToRoleInput oldBindPermissionToRoleInput = new BindPermissionToRoleInput();
        List<BindRoleDataPermissions> oldPermissions = new ArrayList<>();

        if (oldRolePermissions != null && oldRolePermissions.size() != 0) {
            for (RolePermission rolePermission : oldRolePermissions) {
                BindRoleDataPermissions bindRoleDataPermissions = new BindRoleDataPermissions();
                BeanUtils.copyProperties(rolePermission,bindRoleDataPermissions);
                oldPermissions.add(bindRoleDataPermissions);
            }
        }

        oldBindPermissionToRoleInput.setPermissions(oldPermissions);
        oldBindPermissionToRoleInput.setRoleId(bindPermissionToRoleInput.getRoleId());
        operationLogService.saveOperationLog(guest,oldBindPermissionToRoleInput,bindPermissionToRoleInput,"roleId",OperationTargetType.TYPE_ROLE_PERMISSIONS);

        return ResultOutputUtil.success(rolePermissionOutputs);
    }

    @Override
    @ApiOperation("获取所有角色")
    public ResultOutput<List<RoleOutput>> getRoles() {

        List<Role> roles = roleDao.getRoles();
        List<RoleOutput> roleOutputs = new ArrayList<>();

        for (Role role : roles) {
            RoleOutput roleOutput = new RoleOutput();
            BeanUtils.copyProperties(role, roleOutput);
            roleOutputs.add(roleOutput);
        }

        return ResultOutputUtil.success(roleOutputs);
    }

    @Override
    @ApiOperation(value = "获取角色绑定的所有权限")
    public ResultOutput getRolePermissions(@RequestParam("id") Integer id) {

        List<RolePermission> rolePermissions = roleDao.getRolePermissionsByRoleId(id);
        List<RolePermissionOutput> rolePermissionOutputList = new ArrayList<>();

        for (RolePermission rolePermission : rolePermissions) {
            RolePermissionOutput rolePermissionOutput = new RolePermissionOutput();
            BeanUtils.copyProperties(rolePermission, rolePermissionOutput);
            rolePermissionOutputList.add(rolePermissionOutput);
        }

        return ResultOutputUtil.success(rolePermissionOutputList);
    }

    @Override
    @ApiOperation(value = "获取角色绑定的用户ID列表")
    public ResultOutput<List<Integer>> getRoleAccountIds(@RequestParam("id") Integer id) {

        List<Integer> accountIds = roleDao.getRolesAccountIdsByRoleId(id);

        return ResultOutputUtil.success(accountIds);
    }

    @Override
    @ApiOperation(value = "绑定角色的用户")
    public ResultOutput<List<AccountRoleOutput>> bindRoleAccounts(@RequestBody BindAccountsToRoleInput bindAccountsToRoleInput) {

        Guest guest = ThreadLocalUtil.getGuest();
        List<Integer> accountIds = bindAccountsToRoleInput.getAccountIds();
        List<AccountRole> accountRolesList = new ArrayList<>();

        for (Integer accountId : accountIds) {
            AccountRole accountRoles = new AccountRole();
            accountRoles.setOperatorId(guest.getId());
            accountRoles.setOperatorIp(guest.getIp());
            accountRoles.setOperatorName(guest.getUsername());
            accountRoles.setAccountId(accountId);
            accountRoles.setRoleId(bindAccountsToRoleInput.getId());
            accountRolesList.add(accountRoles);
        }

        List<Integer> oldAccountIds = roleDao.getRolesAccountIdsByRoleId(bindAccountsToRoleInput.getId());
        roleDao.deleteRoleAccountsByRoleId(bindAccountsToRoleInput.getId());
        accountDao.bindRolesToAccount(accountRolesList);
        List<AccountRoleOutput> accountRoleOutputs = new ArrayList<>();

        for (AccountRole accountRole : accountRolesList) {
            AccountRoleOutput accountRoleOutput = new AccountRoleOutput();
            BeanUtils.copyProperties(accountRole,accountRoleOutput);
            accountRoleOutputs.add(accountRoleOutput);
        }

        BindAccountsToRoleInput oldBindAccountsToRoleInput = new BindAccountsToRoleInput();
        oldBindAccountsToRoleInput.setAccountIds(oldAccountIds);
        oldBindAccountsToRoleInput.setId(bindAccountsToRoleInput.getId());
        operationLogService.saveOperationLog(guest,oldBindAccountsToRoleInput,bindAccountsToRoleInput,"id",OperationTargetType.TYPE_ROLE_ACCOUNTS);

        return ResultOutputUtil.success(accountRoleOutputs);
    }

    @Override
    @ApiOperation(value = "更新角色")
    public ResultOutput<RoleOutput> updateRole(@Validated @RequestBody UpdateRoleInput updateRoleInput) {

        Guest guest = ThreadLocalUtil.getGuest();
        Role role = new Role();
        BeanUtils.copyProperties(updateRoleInput, role);
        role.setOperatorName(guest.getUsername());
        role.setOperatorIp(guest.getIp());
        role.setOperatorId(guest.getId());
        Integer count = roleDao.updateRole(role);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.PUT_ROLE_FAILED);
        }

        RoleOutput roleOutput = new RoleOutput();
        BeanUtils.copyProperties(role,roleOutput);

        return ResultOutputUtil.success(roleOutput);
    }

    @Override
    @Transactional
    @ApiOperation(value = "删除空角色", notes = "未绑定用户的角色")
    public ResultOutput deleteRole(@RequestParam("id") Integer id) {

        Guest guest = ThreadLocalUtil.getGuest();
        Integer roleCount = roleDao.deleteRoleById(id);

        if (roleCount == 0) {
            return ResultOutputUtil.error(AccountStatusCode.DELETE_ROLE_FAILED);
        }

        Role oldValue = roleDao.getRoleById(id);
        roleDao.deleteRolePermissionsByRoleId(id);
        operationLogService.saveOperationLog(guest, oldValue, null,"id",OperationTargetType.TYPE_ROLE);

        return ResultOutputUtil.success(id);
    }

    @Override
    @ApiOperation(value = "获取有效角色数量")
    public ResultOutput getValidRolesCount() {
        Integer count = roleDao.getValidRoleCount();

        return ResultOutputUtil.success(count);
    }

    /**
     * 解析数据权限的格式
     *
     * @param limitString String 数据权限字符
     * @return ResultOutput
     */
    private ResultOutput analyseLimitString(String limitString) {
        Map<String, String> params = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        String[] splits = limitString.split("\\?");

        if (splits.length != 2 || StringUtils.isEmpty(splits[1])) {
            return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
        }

        String[] dataScrops = splits[1].split("&");

        for (String dataScrop : dataScrops) {
            if (!dataScrop.contains("=")) {
                return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
            }

            String[] dataMap = dataScrop.split("=");

            if (dataMap.length != 2 || StringUtils.isEmpty(dataMap[0]) || StringUtils.isEmpty(dataMap[1])) {
                return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
            }

            if (dataScrop.contains("*") && !"*".equals(dataMap[1])) {
                return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
            }

            if (dataMap[0].contains("{") && dataMap[0].contains("}")) {
                String key = dataMap[0];

                if (dataScrop.contains("*") && data.containsKey(key.substring(1, key.length() - 1))) {
                    return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
                }

                if ("{*}".equals(key) && data.size() > 0) {
                    return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
                }

                if (data.containsKey("*")) {
                    return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
                }

                data.put(key.substring(1, key.length() - 1), dataMap[1]);
                continue;
            }

            if (dataMap[0].contains("{") || dataMap[0].contains("}")) {
                return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
            }

            if ("*".equals(dataMap[0]) && params.size() > 0) {
                return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
            }

            if (dataScrop.contains("*") && params.containsKey(dataMap[0])) {
                return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
            }

            params.put(dataMap[0], dataMap[1]);
        }

        Map<String, Object> limitScope = new HashMap<>();

        if (params.size() != 0) {
            limitScope.put("params", params);
        }

        if (data.size() != 0) {
            limitScope.put("data", data);
        }

        if (limitScope.size() == 0) {
            return ResultOutputUtil.error(AccountStatusCode.ROLE_DATA_PERMISSION_PATTERN_ERROR);
        }

        return ResultOutputUtil.success(JSONObject.toJSONString(limitScope));
    }
}

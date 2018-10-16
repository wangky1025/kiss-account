package com.kiss.account.service;

import com.kiss.account.client.RoleClient;
import com.kiss.account.dao.AccountDao;
import com.kiss.account.dao.RoleDao;
import com.kiss.account.entity.Role;
import com.kiss.account.entity.RolePermissions;
import com.kiss.account.input.AllocateAccountsToRoleInput;
import com.kiss.account.input.AllocatePermissionToRoleInput;
import com.kiss.account.input.CreateRoleInput;
import com.kiss.account.input.UpdateRoleInput;
import com.kiss.account.output.AccountRoleOutput;
import com.kiss.account.output.RoleOutput;
import com.kiss.account.output.RolePermissionOutput;
import com.kiss.account.status.AccountStatusCode;
import com.kiss.account.utils.ResultOutputUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import output.ResultOutput;

@RestController
@Api(tags = "Role", description = "角色相关接口")
public class RoleServiceImpl implements RoleClient {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private AccountDao accountDao;

    @Override
    @ApiOperation("添加角色")
    public ResultOutput<RoleOutput> createRole(@Validated @RequestBody CreateRoleInput createRoleInput) {

        Role role = roleDao.getRoleByName(createRoleInput.getName());

        if (role != null) {
            return ResultOutputUtil.error(AccountStatusCode.ROLE_EXIST);
        }

        role = new Role();
        BeanUtils.copyProperties(createRoleInput, role);
        role.setOperatorId(123);
        role.setOperatorIp("127.0.0.5");
        role.setOperatorName("旺旺");
        roleDao.createRole(role);
        RoleOutput roleOutput = new RoleOutput();
        BeanUtils.copyProperties(role, roleOutput);
        return ResultOutputUtil.success(roleOutput);
    }

    @Override
    @ApiOperation("分配角色权限")
    public ResultOutput<List<RolePermissionOutput>> allocateRolePermissions(@Validated @RequestBody AllocatePermissionToRoleInput allocatePermissionToRole) {
        List<Integer> permissions = allocatePermissionToRole.getPermissions();
        List<RolePermissions> rolePermissions = new ArrayList<>();
        for (Integer permissionId : permissions) {
            RolePermissions rolePermission = new RolePermissions();
            rolePermission.setRoleId(allocatePermissionToRole.getRoleId());
            rolePermission.setOperatorId(123);
            rolePermission.setOperatorIp("127.0.0.5");
            rolePermission.setOperatorName("旺旺");
            rolePermission.setPermissionId(permissionId);
            rolePermissions.add(rolePermission);
        }
        roleDao.deleteRolePermissions(allocatePermissionToRole.getRoleId());
        roleDao.allocatePermissionsToRole(rolePermissions);
        List<RolePermissionOutput> rolePermissionOutputs = new ArrayList<>();
        for (RolePermissions rolePermission : rolePermissions) {
            RolePermissionOutput rolePermissionOutput = new RolePermissionOutput();
            BeanUtils.copyProperties(rolePermission, rolePermissionOutput);
            rolePermissionOutputs.add(rolePermissionOutput);
        }
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
    @ApiOperation(value = "获取角色绑定的权限ID列表")
    public ResultOutput<List<Integer>> getRolePermissionIds(Integer id) {
        List<Integer> permissionIds = roleDao.getRolesPermissionIds(id);
        return ResultOutputUtil.success(permissionIds);
    }

    @Override
    @ApiOperation(value = "获取角色绑定的用户ID列表")
    public ResultOutput<List<Integer>> getRoleAccountIds(Integer id) {
        List<Integer> accountIds = roleDao.getRolesAccountIds(id);
        return ResultOutputUtil.success(accountIds);
    }

    @Override
    @ApiOperation(value = "绑定角色的用户")
    public ResultOutput<List<AccountRoleOutput>> allocateRoleAccounts(@RequestBody AllocateAccountsToRoleInput allocateAccountsToRoleInput) {

        List<Integer> accountIds = allocateAccountsToRoleInput.getAccountIds();
        List<AccountRoleOutput> accountRolesList = new ArrayList<>();

        for (Integer accountId : accountIds) {
            AccountRoleOutput accountRoles = new AccountRoleOutput();
            accountRoles.setOperatorId(123);
            accountRoles.setOperatorIp("127.0.0.4");
            accountRoles.setOperatorName("李四");
            accountRoles.setAccountId(accountId);
            accountRoles.setRoleId(allocateAccountsToRoleInput.getId());
            accountRolesList.add(accountRoles);
        }

        accountDao.allocateRolesToAccount(accountRolesList);

        return ResultOutputUtil.success(accountRolesList);
    }

    @Override
    @ApiOperation(value = "更新角色")
    public ResultOutput<RoleOutput> updateRole(@Validated @RequestBody UpdateRoleInput updateRoleInput) {

        RoleOutput roleOutput = new RoleOutput();
        BeanUtils.copyProperties(updateRoleInput, roleOutput);
        Integer count = roleDao.putRole(roleOutput);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.PUT_ROLE_FAILED);
        }

        return ResultOutputUtil.success(roleOutput);
    }

    @Override
    @Transactional
    @ApiOperation(value = "删除空角色", notes = "未绑定用户的角色")
    public ResultOutput deleteRole(@RequestParam("id") Integer id) {
        Integer roleCount = roleDao.deleteRole(id);

        if (roleCount == 0) {
            return ResultOutputUtil.error(AccountStatusCode.DELETE_ROLE_FAILED);
        }

        roleDao.deleteRolePermissions(id);

        return ResultOutputUtil.success(id);
    }

}

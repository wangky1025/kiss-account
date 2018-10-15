package com.kiss.account.mapper;

import com.kiss.account.entity.Role;
import com.kiss.account.entity.RolePermissions;
import com.kiss.account.output.RoleOutput;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleMapper {

    void createRole(Role role);

    void allocatePermissionToRole(RolePermissions rolePermissions);

    void allocatePermissionsToRole(List<RolePermissions> rolePermissions);

    List<Role> getRoles();

    List<Integer> getRolesPermissionIds(Integer id);

    List<Integer> getRolesAccountIds(Integer id);

    Role getRoleByName(String name);

    Integer putRole(RoleOutput roleOutput);

    Integer deleteRole(Integer id);

    Integer deleteRolePermissions(Integer id);
}

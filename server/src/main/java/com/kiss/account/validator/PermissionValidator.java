package com.kiss.account.validator;

import com.kiss.account.dao.PermissionDao;
import com.kiss.account.entity.Permission;
import com.kiss.account.input.CreatePermissionInput;
import com.kiss.account.input.UpdatePermissionInput;
import com.kiss.account.regex.Regex;
import com.kiss.account.status.AccountStatusCode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class PermissionValidator implements Validator {

    @Autowired
    private PermissionDao permissionDao;

    private Permission permission;

    @Override
    public boolean supports(Class<?> clazz) {

        return clazz.equals(CreatePermissionInput.class)
                || clazz.equals(UpdatePermissionInput.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (CreatePermissionInput.class.isInstance(target)) {
            CreatePermissionInput createPermissionInput = (CreatePermissionInput) target;
            validateName(null, createPermissionInput.getName(), errors);
            validateCode(null, createPermissionInput.getCode(),createPermissionInput.getType(), errors);
            validateStatus(createPermissionInput.getStatus(), errors);
        } else if (UpdatePermissionInput.class.isInstance(target)) {
            UpdatePermissionInput updatePermissionInput = (UpdatePermissionInput) target;
            validatePermissionExist(updatePermissionInput.getId(), errors);

            if (permission == null) {
                return;
            }

            validateName(updatePermissionInput.getId(), updatePermissionInput.getName(), errors);
            validateCode(updatePermissionInput.getId(), updatePermissionInput.getCode(),updatePermissionInput.getType(), errors);
            validateStatus(updatePermissionInput.getStatus(), errors);
        } else {
            errors.rejectValue("data", "", "数据格式错误");
        }

    }

    private void validatePermissionExist(Integer id, Errors errors) {

        permission = permissionDao.getPermissionById(id);

        if (permission == null) {
            errors.rejectValue("id", String.valueOf(AccountStatusCode.PERMISSION_NOT_EXIST), "权限不存在");
        }
    }

    private void validateName(Integer id, String name, Errors errors) {

        if (StringUtils.isEmpty(name)) {
            errors.rejectValue("name", String.valueOf(AccountStatusCode.PERMISSION_NAME_NOT_EMPTY), "权限名称不能为空");
        }

        Permission findPermission = new Permission();
        findPermission.setName(name);
        findPermission = permissionDao.getPermissionByNameOrCode(findPermission);

        if (findPermission == null) {
            return;
        }

        if (id != null && permission.getId().equals(findPermission.getId())) {
            return;
        }

        errors.rejectValue("name", String.valueOf(AccountStatusCode.PERMISSION_NAME_EXIST), "权限名称已存在");
    }

    private void validateCode(Integer id, String code,Integer type, Errors errors) {

        if (StringUtils.isEmpty(code)) {
            errors.rejectValue("code", String.valueOf(AccountStatusCode.PERMISSION_CODE_NOT_EMPTY), "权限码不能为空");
        }

        if ((type == 1 && !Pattern.matches(Regex.INTEFACE_PERMISSION,code)) ||
                (type == 2 && !Pattern.matches(Regex.PAGE_PERMISSION,code)) ||
                (type == 3 && !Pattern.matches(Regex.COMPONENT_PERMISSION,code))) {
            errors.rejectValue("code",String.valueOf(AccountStatusCode.PERMISSION_CODE_STYLE_ERROR),"权限码格式不对");
        }

        Permission findPermission = permissionDao.getPermissionByCode(code);

        if (findPermission == null) {
            return;
        }

        if (id != null && permission.getId().equals(findPermission.getId())) {
            return;
        }

        errors.rejectValue("code", String.valueOf(AccountStatusCode.PERMISSION_CODE_EXIST), "权限码已存在");
    }

    private void validateModuleId(Integer moduleId, Errors errors) {

        if (moduleId == null) {
            errors.rejectValue("moduleId", String.valueOf(AccountStatusCode.PERMISSION_MODULE_NOT_EMPTY), "所属模块不能为空");
            return;
        }

        if (permissionDao.getPermissionModuleById(moduleId) == null) {
            errors.rejectValue("moduleId", String.valueOf(AccountStatusCode.PERMISSION_MODULE_NOT_EXIST), "所属模块不存在");
        }
    }


    private void validateStatus(Integer status, Errors errors) {

        if (status == null) {
            errors.rejectValue("status", String.valueOf(AccountStatusCode.PERMISSION_TYPE_NOT_EMPTY), "权限类型不能为空");
            return;
        }

        if (status != 1 && status != 2 && status != 3) {
            errors.rejectValue("status", String.valueOf(AccountStatusCode.PERMISSION_TYPE_ERROR), "权限类型不正确");
        }
    }

}

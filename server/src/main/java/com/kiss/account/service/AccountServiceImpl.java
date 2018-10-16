package com.kiss.account.service;

import com.kiss.account.client.AccountClient;
import com.kiss.account.dao.AccountDao;
import com.kiss.account.dao.AccountGroupDao;
import com.kiss.account.entity.Account;
import com.kiss.account.entity.AccountGroup;
import com.kiss.account.input.*;
import com.kiss.account.output.*;
import com.kiss.account.status.AccountStatusCode;
import com.kiss.account.utils.CryptoUtil;
import com.kiss.account.utils.ResultOutputUtil;
import com.kiss.account.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import output.ResultOutput;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@Api(tags = "Account", description = "账户相关接口")
public class AccountServiceImpl implements AccountClient {

    @Autowired
    private AccountGroupDao accountGroupDao;

    @Autowired
    private AccountDao accountDao;

    @Value("${max.accounts.size}")
    private String maxAccountsSize;

    @Value("${account.default.password}")
    private String accountDefaultPassword;

    @Autowired
    HttpServletRequest request;

    @Override
    @ApiOperation(value = "创建部门")
    public ResultOutput<AccountGroupOutput> createAccountGroup(@Validated @RequestBody CreateAccountGroupInput createAccountGroupInput) {

        AccountGroup accountGroup = accountGroupDao.getAccountGroupByName(createAccountGroupInput.getName());

        if (accountGroup != null) {
            return ResultOutputUtil.error(AccountStatusCode.ACCOUNTGROUP_EXIST);
        }

        accountGroup = new AccountGroup();
        BeanUtils.copyProperties(createAccountGroupInput, accountGroup);
        accountGroup.setLevel("0");
        accountGroup.setSeq(0);
        accountGroup.setOperatorId(UserUtil.getUserId());
        accountGroup.setOperatorIp("127.0.0.1");
        accountGroup.setOperatorName(UserUtil.getUsername());
        accountGroupDao.createAccountGroup(accountGroup);
        AccountGroupOutput accountGroupOutput = new AccountGroupOutput();
        BeanUtils.copyProperties(accountGroup, accountGroupOutput);

        return ResultOutputUtil.success(accountGroupOutput);
    }

    @Override
    @ApiOperation(value = "添加账户")
    public ResultOutput<AccountOutput> createAccount(@Validated @RequestBody CreateAccountInput createAccountInput) {

        Account account = accountDao.getAccountByUniqueIdentification(createAccountInput.getName(), createAccountInput.getUsername(), createAccountInput.getEmail(), createAccountInput.getMobile());

        if (account != null) {
            return verifyAccountExistType(account,createAccountInput.getName(),createAccountInput.getUsername(),createAccountInput.getEmail(),createAccountInput.getMobile());
        }

        account = new Account();
        BeanUtils.copyProperties(createAccountInput, account);
        String salt = CryptoUtil.salt();
        String password = CryptoUtil.hmacSHA256(createAccountInput.getPassword(), salt);
        account.setSalt(salt);
        account.setPassword(password);
        account.setName(createAccountInput.getName());
        account.setOperatorId(UserUtil.getUserId());
        account.setOperatorIp("127.0.0.4");
        account.setOperatorName(UserUtil.getUsername());
        accountDao.createAccount(account);
        AccountOutput accountOutput = new AccountOutput();
        BeanUtils.copyProperties(account, accountOutput);
        AccountGroup group = accountGroupDao.getGroupById(account.getGroupId());
        accountOutput.setGroupName(group.getName());

        return ResultOutputUtil.success(accountOutput);
    }

    @Override
    @ApiOperation(value = "绑定账户角色")
    @Transactional
    public ResultOutput<List<AccountRoleOutput>> bindAccountRoles(@Validated @RequestBody BindRoleToAccountInput bindRoleToAccountInput) {

        List<Integer> roles = bindRoleToAccountInput.getRoleId();
        List<AccountRoleOutput> accountRolesList = new ArrayList<>();

        for (Integer roleId : roles) {
            AccountRoleOutput accountRoles = new AccountRoleOutput();
            accountRoles.setOperatorId(UserUtil.getUserId());
            accountRoles.setOperatorIp("127.0.0.4");
            accountRoles.setOperatorName(UserUtil.getUsername());
            accountRoles.setAccountId(bindRoleToAccountInput.getAccountId());
            accountRoles.setRoleId(roleId);
            accountRolesList.add(accountRoles);
        }

        accountDao.deleteAccountRoles(bindRoleToAccountInput.getAccountId());
        accountDao.bindRolesToAccount(accountRolesList);

        return ResultOutputUtil.success(accountRolesList);
    }

    @Override
    @ApiOperation(value = "获取账户列表")
    public ResultOutput<GetAccountsOutput> getAccounts(String page, String size) {

        Integer queryPage = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        Integer maxSize = Integer.parseInt(maxAccountsSize);
        Integer pageSize = (StringUtils.isEmpty(size) || Integer.parseInt(size) > maxSize) ? maxSize : Integer.parseInt(size);
        List<AccountOutput> accounts = accountDao.getAccounts((queryPage - 1) * pageSize, pageSize);
        Integer count = accountDao.getAccountsCount();
        GetAccountsOutput getAccountsOutput = new GetAccountsOutput(accounts, count);

        return ResultOutputUtil.success(getAccountsOutput);
    }

    @Override
    @ApiOperation(value = "获取账户信息")
    public ResultOutput<AccountOutput> getAccount(String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultOutputUtil.error(AccountStatusCode.PARAMETER_ERROR);
        }
        AccountOutput account = accountDao.getAccountById(Integer.parseInt(id));
        return ResultOutputUtil.success(account);
    }

    @Override
    @ApiOperation(value = "获取部门列表")
    public ResultOutput<AccountGroupOutput> getGroups() {

        List<AccountGroup> groups = accountGroupDao.getGroups();
        List<AccountGroupOutput> groupsOutput = new ArrayList<>();

        for (AccountGroup accountGroup : groups) {
            AccountGroupOutput accountGroupOutput = new AccountGroupOutput();
            BeanUtils.copyProperties(accountGroup, accountGroupOutput);
            groupsOutput.add(accountGroupOutput);
        }

        return ResultOutputUtil.success(groupsOutput);
    }

    @Override
    @ApiOperation(value = "获取部门信息")
    public ResultOutput<AccountGroupOutput> getGroup(String id) {

        AccountGroup group = accountGroupDao.getGroupById(Integer.parseInt(id));
        AccountGroupOutput accountGroupOutput = new AccountGroupOutput();
        BeanUtils.copyProperties(group, accountGroupOutput);

        return ResultOutputUtil.success(accountGroupOutput);
    }

    @Override
    @ApiOperation(value = "获取总账户数")
    public ResultOutput<Integer> getAccountsCount() {

        Integer count = accountDao.getAccountsCount();

        return ResultOutputUtil.success(count);
    }

    @Override
    public ResultOutput get() {
        return ResultOutputUtil.success("++++" + UserUtil.getUsername() + "=====" + UserUtil.getUserId());
    }

    @Override
    @ApiOperation(value = "更新用户")
    public ResultOutput<AccountOutput> updateAccount(@Validated @RequestBody UpdateAccountInput updateAccountInput) {

        Account account = accountDao.getAccountByUniqueIdentification(updateAccountInput.getName(), updateAccountInput.getUsername(), updateAccountInput.getEmail(), updateAccountInput.getMobile());

        if (account != null) {
            return verifyAccountExistType(account,updateAccountInput.getName(),updateAccountInput.getUsername(),updateAccountInput.getEmail(),updateAccountInput.getMobile());
        }

        AccountOutput accountOutput = new AccountOutput();
        BeanUtils.copyProperties(updateAccountInput, accountOutput);
        Integer count = accountDao.updateAccount(accountOutput);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.PUT_ACCOUNT_FAILED);
        }

        return ResultOutputUtil.success(accountOutput);
    }

    @Override
    @ApiOperation(value = "更新部门")
    public ResultOutput updateAccountGroup(@Validated @RequestBody UpdateAccountGroupInput updateAccountGroupInput) {

        AccountGroup accountGroup = accountGroupDao.getAccountGroupByName(updateAccountGroupInput.getName());

        if (accountGroup != null) {
            return ResultOutputUtil.error(AccountStatusCode.ACCOUNTGROUP_EXIST);
        }

        accountGroup = new AccountGroup();
        BeanUtils.copyProperties(updateAccountGroupInput, accountGroup);
        Integer count = accountGroupDao.updateAccountGroup(accountGroup);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.PUT_ACCOUNT_GROUP_FAILED);
        }
        return ResultOutputUtil.success(accountGroup);
    }

    @Override
    @ApiOperation(value = "重置账户密码")
    public ResultOutput updateAccountPassword(Integer id) {

        String salt = CryptoUtil.salt();
        String password = CryptoUtil.hmacSHA256(accountDefaultPassword, salt);
        Account account = new Account();
        account.setId(id);
        account.setSalt(salt);
        account.setPassword(password);
        Integer count = accountDao.updateAccountPassword(account);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.PUT_ACCOUNT_PASSWORD_FAILED);
        }

        return ResultOutputUtil.success();
    }

    @Override
    @ApiOperation(value = "更新用户状态")
    public ResultOutput updateAccountStatus(@RequestBody UpdateAccountStatusInput updateAccountStatusInput) {

        AccountOutput accountOutput = new AccountOutput();
        BeanUtils.copyProperties(updateAccountStatusInput, accountOutput);
        Integer count = accountDao.updateAccountStatus(accountOutput);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.PUT_ACCOUNT_STATUS_FAILED);
        }

        return ResultOutputUtil.success(accountOutput);
    }

    public ResultOutput verifyAccountExistType (Account account,String name,String username,String email,String mobile) {
        if (!StringUtils.isEmpty(account.getName()) && account.getName().equals(name)) {
            return ResultOutputUtil.error(AccountStatusCode.NAME_EXIST);
        }

        if (!StringUtils.isEmpty(account.getUsername()) && account.getUsername().equals(username)) {
            return ResultOutputUtil.error(AccountStatusCode.USERNAME_EXIST);
        }

        if (!StringUtils.isEmpty(account.getEmail()) && account.getEmail().equals(email)) {
            return ResultOutputUtil.error(AccountStatusCode.EMAIL_EXIST);
        }

        if (!StringUtils.isEmpty(account.getMobile()) && account.getMobile().equals(mobile)) {
            return ResultOutputUtil.error(AccountStatusCode.MOBILE_EXIST);
        }

        return ResultOutputUtil.error(AccountStatusCode.Account_EXIST);
    }
}

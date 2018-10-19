package com.kiss.account.service;

import com.kiss.account.client.AccountClient;
import com.kiss.account.client.AccountGroupClient;
import com.kiss.account.dao.AccountDao;
import com.kiss.account.dao.AccountGroupDao;
import com.kiss.account.entity.Account;
import com.kiss.account.entity.AccountGroup;
import com.kiss.account.input.*;
import com.kiss.account.output.AccountGroupOutput;
import com.kiss.account.output.AccountOutput;
import com.kiss.account.output.AccountRoleOutput;
import com.kiss.account.output.GetAccountsOutput;
import com.kiss.account.status.AccountStatusCode;
import com.kiss.account.utils.CryptoUtil;
import com.kiss.account.utils.DbEnumsUtil;
import com.kiss.account.utils.ResultOutputUtil;
import com.kiss.account.utils.UserUtil;
import com.kiss.account.validator.AccountGroupValidator;
import com.kiss.account.validator.AccountValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import output.ResultOutput;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@Api(tags = "Account", description = "账户相关接口")
public class AccountGroupServiceImpl implements AccountGroupClient {

    @Autowired
    private AccountGroupDao accountGroupDao;

    @Autowired
    private AccountGroupValidator accountGroupValidator;

    @Value("${max.accounts.size}")
    private String maxAccountsSize;

    @Value("${account.default.password}")
    private String accountDefaultPassword;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(accountGroupValidator);
    }

    @Override
    @ApiOperation(value = "创建部门")
    public ResultOutput<AccountGroupOutput> createAccountGroup(@Validated @RequestBody CreateAccountGroupInput createAccountGroupInput) {

        AccountGroup accountGroup = accountGroupDao.getAccountGroupByName(createAccountGroupInput.getName());

        if (accountGroup != null) {
            return ResultOutputUtil.error(AccountStatusCode.ACCOUNT_GROUP_NAME_EXIST);
        }

        accountGroup = new AccountGroup();
        BeanUtils.copyProperties(createAccountGroupInput, accountGroup);
        accountGroup.setLevel("0");
        accountGroup.setSeq(0);
        accountGroup.setOperatorId(123);
        accountGroup.setOperatorIp("127.0.0.1");
        accountGroup.setOperatorName("koy");
        accountGroupDao.createAccountGroup(accountGroup);
        AccountGroupOutput accountGroupOutput = new AccountGroupOutput();
        BeanUtils.copyProperties(accountGroup, accountGroupOutput);


        return ResultOutputUtil.success(accountGroupOutput);
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
    @ApiOperation(value = "更新部门")
    public ResultOutput updateAccountGroup(@Valid @RequestBody UpdateAccountGroupInput updateAccountGroupInput) {

        AccountGroup accountGroup = new AccountGroup();
        BeanUtils.copyProperties(updateAccountGroupInput, accountGroup);
        Integer count = accountGroupDao.updateAccountGroup(accountGroup);

        if (count == 0) {
            return ResultOutputUtil.error(AccountStatusCode.PUT_ACCOUNT_GROUP_FAILED);
        }
        return ResultOutputUtil.success(accountGroup);
    }
}
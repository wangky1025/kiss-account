package com.kiss.account.dao;

import com.kiss.account.entity.Account;
import com.kiss.account.output.AccountOutput;
import com.kiss.account.output.AccountRoleOutput;

import java.util.List;

public interface AccountDao {
    /**
     * 创建账户
     *
     * @param account Account
     */
    void createAccount(Account account);

    /**
     * 给账号分配角色
     *
     * @param accountRoles List<AccountRoleOutput>
     */
    void bindRolesToAccount(List<AccountRoleOutput> accountRoles);

    /**
     * 查询账号信息
     *
     * @param start int
     * @param size  int
     * @return List<AccountOutput>
     */
    List<AccountOutput> getAccounts(int start, int size);

    /**
     * 根据账号id查询账号
     *
     * @param id Integer 用户ID
     * @return Account
     */
    Account getAccountById(Integer id);

    /**
     * 根据姓名查找用户
     *
     * @param name String
     * @return Account
     */
    Account getAccountByName(String name);

    /**
     * 通过账号名查询账号
     *
     * @param username 用户名
     * @return Account
     */
    Account getAccountByUsername(String username);

    /**
     * 根据邮箱查找用户
     *
     * @param email String
     * @return Account
     */
    Account getAccountByEmail(String email);

    /**
     * 根据手机号查找用户
     *
     * @param mobile String
     * @return Account
     */
    Account getAccountByMobile(String mobile);

    /**
     * 通过账号id查询账号
     *
     * @param id Integer
     * @return AccountOutput
     */
    AccountOutput getAccountOutputById(Integer id);

    /**
     * 查询所有账号的数量
     *
     * @return Integer
     */
    Integer getAccountsCount();

    /**
     * 获取满足账号唯一表示的账号(账号的部分属性是不允许重复的)
     *
     * @param name     String
     * @param username String
     * @param email    String
     * @param mobile   String
     * @return Account
     */
    Account getAccountByUniqueIdentification(String name, String username, String email, String mobile);

    /**
     * 更新账号信息
     *
     * @param account AccountOutput
     * @return Integer
     */
    Integer updateAccount(AccountOutput account);

    /**
     * 重置账号密码
     *
     * @param account Account
     * @return Integer
     */
    Integer updateAccountPassword(Account account);

    /**
     * 更新账号状态
     *
     * @param accountOutput AccountOutput
     * @return Integer
     */
    Integer updateAccountStatus(AccountOutput accountOutput);

    /**
     * 删除账号所拥有的所有角色
     *
     * @param id Integer
     * @return Integer
     */
    Integer deleteAccountRoles(Integer id);

    /**
     * 获取账户下所有的权限
     *
     * @param id 账户id
     * @return  List<String>
     */
    List<String> getAccountPermissions(Integer id);

    /**
     * 根据账户id及权限码查询账户的数据权限
     *
     * @param id Integer
     * @return List<String>
     */
    List<String> getAccountPermissionDataScope(Integer id, String code);


}

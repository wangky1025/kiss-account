package com.kiss.account.mapper;

import com.kiss.account.entity.Account;
import com.kiss.account.output.AccountOutput;
import com.kiss.account.output.AccountRoleOutput;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AccountMapper {

    /**
     * 创建账号
     *
     * @param account
     * @return
     */
    Integer createAccount(Account account);

    /**
     * 给账号分配角色
     *
     * @param accountRoles
     */
    void bindRolesToAccount(List<AccountRoleOutput> accountRoles);

    /**
     * 分页查询账号信息
     *
     * @param start int
     * @param size  int
     * @return List<AccountOutput>
     */
    List<AccountOutput> getAccounts(@Param("start") int start, @Param("size") int size);


    /**
     * 查询部门用户
     *
     * @param groupId Integer
     * @return List<Account>
     */
    List<Account> getAccountsByGroupId(Integer groupId);

    /**
     * 更加账户id查询账户信息
     *
     * @param id Integer
     * @return Account
     */
    Account getAccountById(Integer id);

    /**
     * 根据用户名查询账户信息
     *
     * @param name String
     * @return Account
     */
    Account getAccountByName(String name);

    /**
     * 根据用户名查找用户
     *
     * @param username String
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
     * 根据账户id查询账户
     *
     * @param userId int
     * @return AccountOutput
     */
    AccountOutput getAccountOutputById(int userId);

    /**
     * 查询所有账户的数量
     *
     * @return Integer
     */
    Integer getAccountsCount();

    /**
     * 获取满足账号唯一表示的账号(账号的部分属性是不允许重复的)
     *
     * @param name String
     * @param username String
     * @param email String
     * @param mobile String
     * @return Account
     */
    Account getAccountByUniqueIdentification(@Param("name") String name, @Param("username") String username, @Param("email") String email, @Param("mobile") String mobile);

    /**
     * 更新账户
     *
     * @param account AccountOutput
     * @return Integer
     */
    Integer updateAccount(AccountOutput account);

    /**
     * 更新账户密码
     *
     * @param account Account
     * @return Integer
     */
    Integer updateAccountPassword(Account account);

    /**
     * 更新账户状态
     *
     * @param accountOutput AccountOutput
     * @return Integer
     */
    Integer updateAccountStatus(AccountOutput accountOutput);

    /**
     * 删除账户所拥有的所有角色
     *
     * @param id Integer
     * @return Integer
     */
    Integer deleteAccountRoles(Integer id);

    /**
     * 获取账户下所有的权限
     *
     * @param id Integer
     * @return   List<String>
     */
    List<String> getAccountPermissions(Integer id);


    /**
     * 根据账户id及权限码code查询账户的数据权限
     *
     * @param params Map<String, Object>
     * @return List<String>
     */
    List<String> getAccountPermissionDataScope(Map<String, Object> params);

    Integer getValidAccountsCount();
}

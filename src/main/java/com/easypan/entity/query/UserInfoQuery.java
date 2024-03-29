package com.easypan.entity.query;

import java.util.Date;


/**
 * 用户信息参数
 */
public class UserInfoQuery extends BaseParam {


	/**
	 * 用户ID
	 */
	private Integer userId;

	/**
	 * 用户名
	 */
	private String userName;

	private String userNameFuzzy;

	/**
	 * 
	 */
	private String phone;

	private String phoneFuzzy;

	/**
	 * 密码
	 */
	private String password;

	private String passwordFuzzy;

	/**
	 * 出生年月
	 */
	private String birthday;

	private String birthdayStart;

	private String birthdayEnd;

	/**
	 * 性别 0:女 1:男
	 */
	private Integer sex;

	/**
	 * 职位0:程序员 1:测试  2:产品经理
	 */
	private Integer position;

	/**
	 * 角色 0:普通用户 1:组长 2:经理 3:管理员 可多选
	 */
	private String roles;

	private String rolesFuzzy;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setUserName(String userName){
		this.userName = userName;
	}

	public String getUserName(){
		return this.userName;
	}

	public void setUserNameFuzzy(String userNameFuzzy){
		this.userNameFuzzy = userNameFuzzy;
	}

	public String getUserNameFuzzy(){
		return this.userNameFuzzy;
	}

	public void setPhone(String phone){
		this.phone = phone;
	}

	public String getPhone(){
		return this.phone;
	}

	public void setPhoneFuzzy(String phoneFuzzy){
		this.phoneFuzzy = phoneFuzzy;
	}

	public String getPhoneFuzzy(){
		return this.phoneFuzzy;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return this.password;
	}

	public void setPasswordFuzzy(String passwordFuzzy){
		this.passwordFuzzy = passwordFuzzy;
	}

	public String getPasswordFuzzy(){
		return this.passwordFuzzy;
	}

	public void setBirthday(String birthday){
		this.birthday = birthday;
	}

	public String getBirthday(){
		return this.birthday;
	}

	public void setBirthdayStart(String birthdayStart){
		this.birthdayStart = birthdayStart;
	}

	public String getBirthdayStart(){
		return this.birthdayStart;
	}
	public void setBirthdayEnd(String birthdayEnd){
		this.birthdayEnd = birthdayEnd;
	}

	public String getBirthdayEnd(){
		return this.birthdayEnd;
	}

	public void setSex(Integer sex){
		this.sex = sex;
	}

	public Integer getSex(){
		return this.sex;
	}

	public void setPosition(Integer position){
		this.position = position;
	}

	public Integer getPosition(){
		return this.position;
	}

	public void setRoles(String roles){
		this.roles = roles;
	}

	public String getRoles(){
		return this.roles;
	}

	public void setRolesFuzzy(String rolesFuzzy){
		this.rolesFuzzy = rolesFuzzy;
	}

	public String getRolesFuzzy(){
		return this.rolesFuzzy;
	}

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

}

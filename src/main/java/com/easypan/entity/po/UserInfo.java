package com.easypan.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.easypan.entity.enums.DateTimePatternEnum;
import com.easypan.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户信息
 */
public class UserInfo implements Serializable {


	/**
	 * 用户ID
	 */
	private Integer userId;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 
	 */
	@JsonIgnore
	private String phone;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 出生年月
	 */
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;

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

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


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

	public void setPhone(String phone){
		this.phone = phone;
	}

	public String getPhone(){
		return this.phone;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return this.password;
	}

	public void setBirthday(Date birthday){
		this.birthday = birthday;
	}

	public Date getBirthday(){
		return this.birthday;
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

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "用户ID:"+(userId == null ? "空" : userId)+"，用户名:"+(userName == null ? "空" : userName)+"，phone:"+(phone == null ? "空" : phone)+"，密码:"+(password == null ? "空" : password)+"，出生年月:"+(birthday == null ? "空" : DateUtil.format(birthday, DateTimePatternEnum.YYYY_MM_DD.getPattern()))+"，性别 0:女 1:男:"+(sex == null ? "空" : sex)+"，职位0:程序员 1:测试  2:产品经理:"+(position == null ? "空" : position)+"，角色 0:普通用户 1:组长 2:经理 3:管理员 可多选:"+(roles == null ? "空" : roles)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}

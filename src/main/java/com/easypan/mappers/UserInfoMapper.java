package com.easypan.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 用户信息 数据库操作接口
 */
public interface UserInfoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据UserId更新
	 */
	 Integer updateByUserId(@Param("bean") T t,@Param("userId") Integer userId);


	/**
	 * 根据UserId删除
	 */
	 Integer deleteByUserId(@Param("userId") Integer userId);


	/**
	 * 根据UserId获取对象
	 */
	 T selectByUserId(@Param("userId") Integer userId);


}

package com.easypan.componet;
import com.easypan.componet.RedisUtils;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.mappers.FileInfoMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("redisComponet")
public class RedisComponet {
    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;
    @Resource
    private RedisUtils redisUtils;

    public SysSettingsDto getSysSettingDto(){
        SysSettingsDto sysSettingsDto = (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (null == sysSettingsDto){
            sysSettingsDto = new SysSettingsDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingsDto);
        }
        return sysSettingsDto;
    }
    public void saveSysSettingDto(String userId, UserSpaceDto userSpaceDto){
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE+userId,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
    }
    public UserSpaceDto getUserSpaceUse(String userId) {
        UserSpaceDto spaceDto = (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
        if (null == spaceDto) {
            spaceDto = new UserSpaceDto();
            Long useSpace = this.fileInfoMapper.selectUseSpace(userId);
            spaceDto.setUseSpace(useSpace);
            spaceDto.setTotalSpace(getSysSettingDto().getUserIninUseSpace() * Constants.MB);
            redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, spaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
        }
        return spaceDto;
    }
    /**
     * 保存已使用的空间
     *
     * @param userId
     */
    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto) {
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
    }

}

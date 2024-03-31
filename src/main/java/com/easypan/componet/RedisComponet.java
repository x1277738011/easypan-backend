package com.easypan.componet;
import com.easypan.componet.RedisUtils;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SysSettingsDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("redisComponet")
public class RedisComponet {
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
}

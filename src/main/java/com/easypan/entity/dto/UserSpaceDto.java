package com.easypan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSpaceDto {
    private Long useSpace;
    private Long totalSpace;

    public Long getUseSpace() {
        return useSpace;
    }

    public void setUseSpace(Long useSpace) {
        this.useSpace = useSpace;
    }

    public Long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(Long totalSpace) {
        this.totalSpace = totalSpace;
    }
}

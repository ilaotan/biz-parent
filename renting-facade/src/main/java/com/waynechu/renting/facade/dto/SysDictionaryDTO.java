package com.waynechu.renting.facade.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhuwei
 * @date 2019/1/18 14:08
 */
@Data
public class SysDictionaryDTO implements Serializable {

    private Long id;

    private Long parentId;

    private String type;

    private String code;

    private String displayName;

    private String description;

    private String createUser;

    private Date createTime;

    private String updateUser;

    private Date updateTime;

    private Boolean isDeleted;
}
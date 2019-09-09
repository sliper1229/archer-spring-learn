package com.luo.spring.framework.aop.config;

import lombok.Data;

/**
 * @author luoxuzheng
 * @create 2019-09-09 8:03
 **/
@Data
public class ArchAopConfig {

    private String pointCut;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectClass;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;

}

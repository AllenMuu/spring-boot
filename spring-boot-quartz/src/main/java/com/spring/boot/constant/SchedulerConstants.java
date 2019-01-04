package com.spring.boot.constant;

public class SchedulerConstants {

    public static final String JOB_PARAM_KEY = "jobParam"; // 任务调度的参数key
    public static final String PREFIX_JOB = "jobDetail_"; // 所有定时任务的名称的前缀
    public static final String JOB_GROUP = "jobGroup"; // 定时任务所属的组
    public static final String PREFIX_TRIGGER = "trigger_"; // 所有触发器名称的前缀
    public static final String TRIGGER_GROUP = "triggerGroup"; // 触发器所属的组
    public static final String PACKAGE_NAME = "com.hzt.scheduler.schedule"; // 定时任务类所在的包
}

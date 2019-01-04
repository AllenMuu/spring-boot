package com.spring.boot.vo;

import java.io.Serializable;

public class ScheduleJob implements Serializable {

    private Long id;
    private String jobName; // 任务名称
    private String jobGroup; // 任务组
    private String clazz; // 任务类路径（包含包名）
    private String jobStatus; // 任务状态
    private String cronExpression; // 任务时间表达式
    private String jobDesc; // 任务描述

    public Long getId() {
        return id;
    }

    public ScheduleJob setId(Long id) {
        this.id = id;
        return this;
    }

    public String getJobName() {
        return jobName;
    }

    public ScheduleJob setJobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public ScheduleJob setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
        return this;
    }

    public String getClazz() {
        return clazz;
    }

    public ScheduleJob setClazz(String clazz) {
        this.clazz = clazz;
        return this;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public ScheduleJob setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
        return this;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public ScheduleJob setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public ScheduleJob setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
        return this;
    }
}
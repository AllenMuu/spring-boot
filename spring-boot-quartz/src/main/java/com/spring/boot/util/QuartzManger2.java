package com.spring.boot.util;

import com.spring.boot.constant.SchedulerConstants;
import com.spring.boot.vo.ScheduleJob;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class QuartzManger2 {

    @Autowired
    private Scheduler scheduler;


    public void addScheduleJob(ScheduleJob scheduleJob) throws Exception {

        // 参数校验
        boolean paramCheck = jobParamCheck(scheduleJob);
        if (!paramCheck) {
            return;
        }
        if (!cronExpressionCheck(scheduleJob.getCronExpression())) {
            return;
        }

        // 获取参数信息
        String jobName = scheduleJob.getJobName().trim();
        String triggerName = SchedulerConstants.PREFIX_TRIGGER + scheduleJob.getJobName();
        String jobClassName = scheduleJob.getClazz();
        if (!jobClassName.contains(".")) {
            jobClassName = SchedulerConstants.PACKAGE_NAME + jobClassName;
        }

        // 构建Job对象
        JobBuilder jobBuilder = JobBuilder.newJob((Class<? extends Job>) Class.forName(jobClassName)).withIdentity(jobName, SchedulerConstants.JOB_GROUP);
        jobBuilder.withDescription(scheduleJob.getJobDesc() == null || scheduleJob.getJobDesc() == "" ? "定时任务：" + jobName : scheduleJob.getJobDesc()); // 描述
        jobBuilder.requestRecovery(true); // 指示调度程序是否应该在遇到“恢复”或“失败”的情况下重新执行作业，默认false
        jobBuilder.storeDurably(true);// quartz会把job持久化到数据库中  默认false
        JobDetail jobDetail = jobBuilder.build();

        // 表达式调度构建器
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());

        // 放入参数，运行时的方法可以获取
        jobDetail.getJobDataMap().put(SchedulerConstants.JOB_PARAM_KEY, scheduleJob);

        // 按新的cronExpression表达式构建一个新的trigger
        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();
        triggerBuilder.withIdentity(triggerName, SchedulerConstants.TRIGGER_GROUP);
        triggerBuilder.withSchedule(scheduleBuilder);
        triggerBuilder.forJob(jobName, SchedulerConstants.JOB_GROUP);
        Trigger trigger = triggerBuilder.build();

        // 加入定时任务
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void updateScheduleJob(ScheduleJob scheduleJob) throws Exception {

        // 参数校验
        if (!cronExpressionCheck(scheduleJob.getCronExpression())) {
            return;
        }

        // 查找需要更新的定时任务
        Trigger trigger = scheduler.getTrigger(getTriggerKey(scheduleJob));
        JobDetail job = scheduler.getJobDetail(getJobKey(scheduleJob));

        // 若任务存在，则先删除
        if (trigger != null || job != null) {
            deleteScheduleJob(scheduleJob);
        }

        // 新增定时任务
        addScheduleJob(scheduleJob);
    }

    /**
     * 删除定时任务
     *
     * @param scheduleJob
     * @throws Exception
     */
    public void deleteScheduleJob(ScheduleJob scheduleJob) throws Exception {

        // 获取定时任务JobKey
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.pauseJob(jobKey); // 先暂停任务
        scheduler.deleteJob(jobKey); // 再删除任务
    }

    /**
     * @param scheduleJob
     */
    public void triggerScheduleJob(ScheduleJob scheduleJob) {
        try {
            JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void pauseScheduleJob(ScheduleJob scheduleJob) {
        try {
            JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 恢复任务
     *
     * @param scheduleJob
     */
    public void resumeScheduleJob(ScheduleJob scheduleJob) {
        try {
            JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停任务
     *
     * @throws Exception
     */
    public void pauseAllScheduleJob() throws Exception {
        scheduler.pauseAll();
    }

    /**
     * 恢复任务
     *
     * @throws Exception
     */
    public void resumeAllScheduleJob() throws Exception {
        scheduler.resumeAll();
    }

    /**
     * 开始任务
     *
     * @throws Exception
     */
    public void startScheduler() throws Exception {
        if (scheduler.isShutdown() || scheduler.isInStandbyMode()) {
            scheduler.start();
        }
    }

    /**
     * 结束任务
     *
     * @throws Exception
     */
    public void shutdownScheduler() throws Exception {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 查询所有任务
     */
    public void getAllScheduleJobList() {
        List<ScheduleJob> scheduleJobList = new ArrayList<ScheduleJob>();
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    ScheduleJob scheduleJob = new ScheduleJob();
                    scheduleJob.setJobName(jobKey.getName());
                    scheduleJob.setJobGroup(jobKey.getGroup());
                    scheduleJob.setClazz(jobKey.getClass().toString());
                    scheduleJob.setJobDesc("触发器:" + trigger.getKey());
                    scheduleJob.setJobStatus(scheduler.getTriggerState(trigger.getKey()).name());
                    if (trigger instanceof CronTrigger) {
                        scheduleJob.setCronExpression(((CronTrigger) trigger).getCronExpression());
                    }
                    scheduleJobList.add(scheduleJob);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getScheduleJobRunningList() {
        List<ScheduleJob> scheduleJobList = new ArrayList<ScheduleJob>();
        try {
            List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
            for (JobExecutionContext executingJob : executingJobs) {
                JobKey jobKey = executingJob.getJobDetail().getKey();
                Trigger trigger = executingJob.getTrigger();
                ScheduleJob scheduleJob = new ScheduleJob();
                scheduleJob.setJobName(jobKey.getName());
                scheduleJob.setJobGroup(jobKey.getGroup());
                scheduleJob.setClazz(jobKey.getClass().toString());
                scheduleJob.setJobDesc("触发器:" + trigger.getKey());
                scheduleJob.setJobStatus(scheduler.getTriggerState(trigger.getKey()).name());
                if (trigger instanceof CronTrigger) {
                    scheduleJob.setCronExpression(((CronTrigger) trigger).getCronExpression());
                }
                scheduleJobList.add(scheduleJob);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Job对象参数校验
     *
     * @param scheduleJob
     * @return
     */
    private boolean jobParamCheck(ScheduleJob scheduleJob) {
        return !(StringUtils.isBlank(scheduleJob.getJobName()) || StringUtils.isBlank(scheduleJob.getCronExpression()) || StringUtils.isBlank(scheduleJob.getClazz()));
    }

    /**
     * Cron表达式校验
     *
     * @param cronExpression 表达式
     * @return boolean
     */
    public static boolean cronExpressionCheck(final String cronExpression) {
        try {
            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setCronExpression(cronExpression);
            Date date = trigger.computeFirstFireTime(null);
            return date != null && date.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * 根据规则生成TriggerKey
     *
     * @param scheduleJob
     * @return
     */
    private TriggerKey getTriggerKey(ScheduleJob scheduleJob) {
        String triggerName = SchedulerConstants.PREFIX_TRIGGER + scheduleJob.getJobName();
        return TriggerKey.triggerKey(triggerName, SchedulerConstants.TRIGGER_GROUP);
    }

    /**
     * 获取到任务Key
     *
     * @param scheduleJob
     * @return
     */
    private JobKey getJobKey(ScheduleJob scheduleJob) {
        String jobName = scheduleJob.getJobName();
        return JobKey.jobKey(jobName, SchedulerConstants.JOB_GROUP);
    }
}


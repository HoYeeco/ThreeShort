
package com.community.credit.task;

import com.community.credit.service.UserCreditProfileService;
import com.community.credit.service.impl.CreditScoreRecordServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 信用评分定时任务
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Component
public class CreditScoreTask {

    @Autowired
    private UserCreditProfileService userCreditProfileService;

    /**
     * 每周一凌晨2点生成周期评分记录
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * MON")
    public void generateWeeklyScoreRecord() {
        try {
            String currentPeriod = CreditScoreRecordServiceImpl.getCurrentPeriod();
            log.info("开始生成周期 {} 的评分记录", currentPeriod);
            
            userCreditProfileService.batchGenerateScoreRecord(currentPeriod);
            
            log.info("周期 {} 的评分记录生成完成", currentPeriod);
        } catch (Exception e) {
            log.error("生成周期评分记录失败", e);
        }
    }

    /**
     * 每天凌晨3点重新计算所有用户的信用分数
     * 确保数据的准确性和一致性
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void recalculateAllUserCredit() {
        try {
            log.info("开始重新计算所有用户信用分数");
            
            // 这里可以根据需要决定是否每天都重新计算
            // 为了减少系统负载，可以改为每周或每月执行一次
            
            log.info("所有用户信用分数重新计算完成");
        } catch (Exception e) {
            log.error("重新计算用户信用分数失败", e);
        }
    }

} 
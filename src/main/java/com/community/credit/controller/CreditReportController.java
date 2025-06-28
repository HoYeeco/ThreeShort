package com.community.credit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.credit.common.Result;
import com.community.credit.dto.CreditReportQueryRequest;
import com.community.credit.dto.CreditReportRequest;
import com.community.credit.dto.CreditReportReviewRequest;
import com.community.credit.entity.CreditReport;
import com.community.credit.service.CreditReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 信用行为上报控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Tag(name = "信用行为上报", description = "信用行为上报、审核等管理接口")
@RestController
@RequestMapping("/credit-report")
public class CreditReportController {

    @Autowired
    private CreditReportService creditReportService;

    @Operation(summary = "提交信用行为上报", description = "居民提交守信/失信行为上报")
    @PostMapping("/submit")
    public Result<String> submitReport(@Valid @RequestBody CreditReportRequest request) {
        creditReportService.submitReport(request);
        return Result.success("上报成功，等待审核");
    }

    @Operation(summary = "获取上报列表", description = "分页查询信用行为上报列表")
    @GetMapping("/list")
    public Result<IPage<CreditReport>> getReportList(@Valid CreditReportQueryRequest request) {
        IPage<CreditReport> reportPage = creditReportService.queryReports(request);
        return Result.success("查询成功", reportPage);
    }

    @Operation(summary = "获取上报详情", description = "根据ID获取上报详细信息")
    @GetMapping("/{reportId}")
    public Result<CreditReport> getReportDetail(@PathVariable Integer reportId) {
        CreditReport report = creditReportService.getReportDetail(reportId);
        if (report == null) {
            return Result.error(404, "上报记录不存在");
        }
        return Result.success("获取成功", report);
    }

    @Operation(summary = "审核上报", description = "管理员审核信用行为上报")
    @PutMapping("/{reportId}/review")
    public Result<String> reviewReport(@PathVariable Integer reportId,
                                      @Valid @RequestBody CreditReportReviewRequest request) {
        creditReportService.reviewReport(reportId, request);
        return Result.success("审核完成");
    }

    @Operation(summary = "撤回上报", description = "用户撤回自己的上报")
    @PutMapping("/{reportId}/withdraw")
    public Result<String> withdrawReport(@PathVariable Integer reportId,
                                        @RequestParam Integer userId) {
        creditReportService.withdrawReport(reportId, userId);
        return Result.success("撤回成功");
    }

    @Operation(summary = "获取待审核数量", description = "获取待审核的上报数量")
    @GetMapping("/pending-count")
    public Result<Long> getPendingCount() {
        Long count = creditReportService.getPendingCount();
        return Result.success("查询成功", count);
    }
} 
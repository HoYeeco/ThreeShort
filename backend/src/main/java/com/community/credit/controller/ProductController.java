package com.community.credit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.credit.common.Result;
import com.community.credit.dto.ProductQueryRequest;
import com.community.credit.dto.ProductRequest;
import com.community.credit.dto.ExchangeRequest;
import com.community.credit.entity.Product;
import com.community.credit.entity.User;
import com.community.credit.security.RequireRole;
import com.community.credit.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 商品管理控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Tag(name = "商品管理", description = "积分商城商品管理接口")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "获取商品列表", description = "分页查询商品列表，支持条件筛选")
    @GetMapping("/list")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<IPage<Product>> getProductList(ProductQueryRequest request) {
        IPage<Product> page = productService.getProductList(request);
        return Result.success("获取成功", page);
    }

    @Operation(summary = "获取可兑换商品列表", description = "根据用户信用等级获取可兑换的商品")
    @GetMapping("/available/{userId}")
    @RequireRole(value = {User.UserRole.RESIDENT, User.UserRole.ADMIN}, allowSelf = true)
    public Result<List<Product>> getAvailableProducts(@PathVariable Integer userId) {
        List<Product> products = productService.getAvailableProducts(userId);
        return Result.success("获取成功", products);
    }

    @Operation(summary = "获取商品详情", description = "根据商品ID获取详细信息")
    @GetMapping("/{id}")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Product> getProductById(@PathVariable Integer id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success("获取成功", product);
    }

    @Operation(summary = "创建商品", description = "创建新的积分商品")
    @PostMapping("/create")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Integer> createProduct(@Valid @RequestBody ProductRequest request) {
        Integer productId = productService.createProduct(request);
        return Result.success("创建成功", productId);
    }

    @Operation(summary = "更新商品", description = "更新商品信息")
    @PutMapping("/{id}")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> updateProduct(@PathVariable Integer id, 
                                       @Valid @RequestBody ProductRequest request) {
        productService.updateProduct(id, request);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除商品", description = "删除指定商品")
    @DeleteMapping("/{id}")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除商品", description = "批量删除多个商品")
    @DeleteMapping("/batch")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> batchDeleteProducts(@RequestBody List<Integer> ids) {
        productService.removeByIds(ids);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "更新商品库存", description = "更新商品库存数量")
    @PutMapping("/{id}/stock")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> updateStock(@PathVariable Integer id, 
                                     @RequestParam Integer quantity) {
        productService.updateStock(id, quantity);
        return Result.success("库存更新成功");
    }

    @Operation(summary = "积分兑换商品", description = "用户使用积分兑换商品")
    @PostMapping("/exchange")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<String> exchangeProduct(@Valid @RequestBody ExchangeRequest request) {
        try {
            productService.exchangeProduct(request);
            return Result.success("兑换成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "检查兑换资格", description = "检查用户是否有兑换资格")
    @GetMapping("/check-eligibility/{userId}/{productId}")
    @RequireRole(value = {User.UserRole.RESIDENT, User.UserRole.ADMIN}, allowSelf = true)
    public Result<Map<String, Object>> checkExchangeEligibility(@PathVariable Integer userId, 
                                                               @PathVariable Integer productId) {
        Map<String, Object> result = productService.checkExchangeEligibility(userId, productId);
        return Result.success("检查完成", result);
    }

    @Operation(summary = "获取商品统计", description = "获取商品相关统计数据")
    @GetMapping("/statistics")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Map<String, Object>> getProductStatistics() {
        Map<String, Object> stats = productService.getProductStatistics();
        return Result.success("获取成功", stats);
    }

    @Operation(summary = "启用/禁用商品", description = "切换商品的启用状态")
    @PutMapping("/{id}/toggle-status")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> toggleProductStatus(@PathVariable Integer id) {
        productService.toggleProductStatus(id);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "调试接口-获取单个商品详情", description = "用于调试eligibleLevels字段")
    @GetMapping("/debug/{id}")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Product> debugGetProduct(@PathVariable Integer id) {
        Product product = productService.getById(id);
        System.out.println("调试信息 - 商品ID: " + id);
        System.out.println("调试信息 - eligibleLevels: " + product.getEligibleLevels());
        System.out.println("调试信息 - imageUrl: " + product.getImageUrl());
        return Result.success("调试成功", product);
    }
} 
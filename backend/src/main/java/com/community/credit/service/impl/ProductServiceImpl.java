package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.dto.ExchangeRequest;
import com.community.credit.dto.ProductQueryRequest;
import com.community.credit.dto.ProductRequest;
import com.community.credit.entity.PointExchangeRecord;
import com.community.credit.entity.Product;
import com.community.credit.entity.UserCreditProfile;
import com.community.credit.mapper.ProductMapper;
import com.community.credit.service.PointExchangeRecordService;
import com.community.credit.service.ProductService;
import com.community.credit.service.SystemLogService;
import com.community.credit.service.UserCreditProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private UserCreditProfileService userCreditProfileService;
    
    @Autowired
    private PointExchangeRecordService pointExchangeRecordService;

    @Autowired
    private SystemLogService systemLogService;

    @Override
    public IPage<Product> getProductList(ProductQueryRequest request) {
        Page<Product> page = new Page<>(request.getCurrent(), request.getSize());
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();

        // 条件筛选
        if (StringUtils.hasText(request.getName())) {
            queryWrapper.like("name", request.getName());
        }
        
        if (StringUtils.hasText(request.getCategory())) {
            queryWrapper.eq("category", request.getCategory());
        }
        
        if (request.getMinPoints() != null) {
            queryWrapper.ge("points_required", request.getMinPoints());
        }
        
        if (request.getMaxPoints() != null) {
            queryWrapper.le("points_required", request.getMaxPoints());
        }
        
        if (request.getIsActive() != null) {
            queryWrapper.eq("is_active", request.getIsActive());
        }
        
        if (request.getHasStock() != null && request.getHasStock()) {
            queryWrapper.gt("stock_quantity", 0);
        }

        // 排序
        if ("points_required".equals(request.getSortField())) {
            if ("asc".equals(request.getSortOrder())) {
                queryWrapper.orderByAsc("points_required");
            } else {
                queryWrapper.orderByDesc("points_required");
            }
        } else if ("stock_quantity".equals(request.getSortField())) {
            if ("asc".equals(request.getSortOrder())) {
                queryWrapper.orderByAsc("stock_quantity");
            } else {
                queryWrapper.orderByDesc("stock_quantity");
            }
        } else {
            // 默认按创建时间排序
            queryWrapper.orderByDesc("created_time");
        }

        IPage<Product> result = this.page(page, queryWrapper);
        
        // 调试日志：检查eligibleLevels字段
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (Product product : result.getRecords()) {
                log.debug("商品 {} 的eligibleLevels: {}, imageUrl: {}", 
                    product.getName(), product.getEligibleLevels(), product.getImageUrl());
            }
        }
        
        return result;
    }

    @Override
    public List<Product> getAvailableProducts(Integer userId) {
        // 获取用户信用档案
        UserCreditProfile profile = userCreditProfileService.getByUserId(userId);
        if (profile == null) {
            return new ArrayList<>();
        }

        // 获取所有可用商品
        List<Product> products = lambdaQuery()
                .eq(Product::getIsActive, true)
                .gt(Product::getStockQuantity, 0)
                .orderByAsc(Product::getPointsRequired)
                .list();

        // 过滤用户积分不足的商品
        return products.stream()
                .filter(product -> profile.getRewardPoints() >= product.getPointsRequired())
                .collect(Collectors.toList());
    }

    @Override
    public Integer createProduct(ProductRequest request) {
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        product.setCreatedTime(LocalDateTime.now());
        product.setUpdatedTime(LocalDateTime.now());
        
        // 如果有minEligibleLevel，转换为eligibleLevels以保持兼容性
        if (request.getMinEligibleLevel() != null) {
            product.setMinEligibleLevel(request.getMinEligibleLevel());
            product.setEligibleLevels(convertMinLevelToEligibleLevels(request.getMinEligibleLevel()));
        }
        
        this.save(product);
        log.info("创建商品成功：{}", product.getName());
        return product.getId();
    }

    @Override
    @Transactional
    public void updateProduct(Integer id, ProductRequest request) {
        Product product = getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 更新商品信息
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPointsRequired(request.getPointsRequired());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        
        // 如果有minEligibleLevel，转换为eligibleLevels以保持兼容性
        if (request.getMinEligibleLevel() != null) {
            product.setMinEligibleLevel(request.getMinEligibleLevel());
            product.setEligibleLevels(convertMinLevelToEligibleLevels(request.getMinEligibleLevel()));
        } else {
            product.setEligibleLevels(request.getEligibleLevels());
        }

        updateById(product);
    }

    @Override
    @Transactional
    public void updateStock(Integer id, Integer quantity) {
        Product product = getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new RuntimeException("库存不足");
        }

        product.setStockQuantity(newStock);
        updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeProduct(ExchangeRequest request) {
        // 获取商品信息
        Product product = this.getById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        if (!product.getIsActive()) {
            throw new RuntimeException("商品已下架");
        }
        
        // 检查库存
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("库存不足");
        }
        
        // 获取用户信用档案
        UserCreditProfile profile = userCreditProfileService.getOrCreateUserProfile(request.getUserId());
        
        // 检查信用等级权限
        if (!isEligibleForLevel(product, profile.getCreditLevel())) {
            throw new RuntimeException("您的信用等级不足以兑换此商品");
        }
        
        // 计算所需积分
        int totalPointsRequired = product.getPointsRequired() * request.getQuantity();
        
        // 检查积分余额
        if (profile.getRewardPoints() < totalPointsRequired) {
            throw new RuntimeException("积分余额不足");
        }
        
        // 扣减积分
        profile.setRewardPoints(profile.getRewardPoints() - totalPointsRequired);
        userCreditProfileService.updateById(profile);
        
        // 清除用户信用档案缓存，确保数据一致性
        userCreditProfileService.clearUserProfileCache(request.getUserId());
        
        // 扣减库存
        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        this.updateById(product);
        
        // 创建兑换记录
        PointExchangeRecord record = new PointExchangeRecord();
        record.setUserId(request.getUserId());
        record.setProductId(request.getProductId());
        record.setProductName(product.getName());
        record.setPointsUsed(totalPointsRequired);
        record.setQuantity(request.getQuantity());
        record.setStatus(PointExchangeRecord.ExchangeStatus.SUCCESS);
        record.setExchangeTime(LocalDateTime.now());
        record.setRemarks(request.getRemarks());
        
        pointExchangeRecordService.save(record);
        
        // 记录积分兑换日志
        systemLogService.recordSuccessLog(
            request.getUserId(),
            "POINT_EXCHANGE",
            String.format("用户兑换商品：%s × %d，消耗积分：%d", product.getName(), request.getQuantity(), totalPointsRequired),
            "POST",
            "/product/exchange",
            String.format("{\"productId\":%d,\"quantity\":%d}", request.getProductId(), request.getQuantity()),
            String.format("{\"recordId\":%d,\"pointsUsed\":%d}", record.getId(), totalPointsRequired)
        );
        
        log.info("用户 {} 成功兑换商品 {}，数量：{}，消耗积分：{}", 
                request.getUserId(), product.getName(), request.getQuantity(), totalPointsRequired);
    }

    @Override
    public Map<String, Object> checkExchangeEligibility(Integer userId, Integer productId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取用户信用档案
        UserCreditProfile profile = userCreditProfileService.getByUserId(userId);
        if (profile == null) {
            result.put("eligible", false);
            result.put("reason", "用户信用档案不存在");
            return result;
        }

        // 获取商品信息
        Product product = getById(productId);
        if (product == null) {
            result.put("eligible", false);
            result.put("reason", "商品不存在");
            return result;
        }

        // 检查商品状态
        if (!product.getIsActive()) {
            result.put("eligible", false);
            result.put("reason", "商品已下架");
            return result;
        }

        // 检查库存
        if (product.getStockQuantity() <= 0) {
            result.put("eligible", false);
            result.put("reason", "商品库存不足");
            return result;
        }

        // 检查积分
        if (profile.getRewardPoints() < product.getPointsRequired()) {
            result.put("eligible", false);
            result.put("reason", "积分不足");
            result.put("required", product.getPointsRequired());
            result.put("current", profile.getRewardPoints());
            return result;
        }

        result.put("eligible", true);
        result.put("product", product);
        result.put("userPoints", profile.getRewardPoints());
        return result;
    }

    @Override
    public Map<String, Object> getProductStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 商品总数
        long totalProducts = this.count();
        stats.put("totalProducts", totalProducts);
        
        // 启用商品数
        long activeProducts = this.count(new QueryWrapper<Product>().eq("is_active", true));
        stats.put("activeProducts", activeProducts);
        
        // 有库存商品数
        long inStockProducts = this.count(new QueryWrapper<Product>().gt("stock_quantity", 0));
        stats.put("inStockProducts", inStockProducts);
        
        // 分类统计
        List<Map<String, Object>> categoryStats = baseMapper.getCategoryStatistics();
        stats.put("categoryStats", categoryStats);
        
        // 积分区间统计
        List<Map<String, Object>> pointsRangeStats = baseMapper.getPointsRangeStatistics();
        stats.put("pointsRangeStats", pointsRangeStats);
        
        return stats;
    }

    @Override
    @Transactional
    public void toggleProductStatus(Integer id) {
        Product product = getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        product.setIsActive(!product.getIsActive());
        updateById(product);
    }

    @Override
    public Product getProductDetail(Integer id) {
        Product product = getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return product;
    }

    @Override
    public Map<String, Object> getExchangeStats(Integer productId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取商品信息
        Product product = getById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 兑换次数统计
        long exchangeCount = pointExchangeRecordService.count(
            new QueryWrapper<PointExchangeRecord>()
                .eq("product_id", productId)
                .eq("status", PointExchangeRecord.ExchangeStatus.SUCCESS)
        );
        
        // 简化统计 - 只返回基本信息
        int totalQuantity = 0;
        int totalPoints = 0;
        
        stats.put("product", product);
        stats.put("exchangeCount", exchangeCount);
        stats.put("totalQuantity", totalQuantity);
        stats.put("totalPoints", totalPoints);
        
        return stats;
    }

    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 软删除 - 设置为不活跃状态
        product.setIsActive(false);
        updateById(product);
        
        log.info("商品已删除：{}", product.getName());
    }
    
    /**
     * 检查用户等级是否可以兑换商品
     */
    private boolean isEligibleForLevel(Product product, String userLevel) {
        // 优先使用新的minEligibleLevel字段
        if (product.getMinEligibleLevel() != null) {
            return isUserLevelEligible(product.getMinEligibleLevel(), userLevel);
        }
        
        // 兼容旧的eligibleLevels字段
        List<String> eligibleLevels = product.getEligibleLevels();
        return eligibleLevels == null || eligibleLevels.isEmpty() || eligibleLevels.contains(userLevel);
    }
    
    /**
     * 检查用户等级是否满足最低兑换要求
     * @param minLevel 最低兑换等级
     * @param userLevel 用户等级
     * @return 是否满足要求
     */
    private boolean isUserLevelEligible(String minLevel, String userLevel) {
        if (minLevel == null || userLevel == null) {
            return true;
        }
        
        // 等级优先级：D < C < B < A < AA < AAA
        Map<String, Integer> levelPriority = Map.of(
            "D", 1,
            "C", 2,
            "B", 3,
            "A", 4,
            "AA", 5,
            "AAA", 6
        );
        
        int minPriority = levelPriority.getOrDefault(minLevel, 1);
        int userPriority = levelPriority.getOrDefault(userLevel, 1);
        
        // 用户等级优先级 >= 最低要求等级优先级
        return userPriority >= minPriority;
    }
    
    /**
     * 将最低兑换等级转换为可兑换等级列表
     * @param minLevel 最低兑换等级
     * @return 可兑换等级列表
     */
    private List<String> convertMinLevelToEligibleLevels(String minLevel) {
        if (minLevel == null) {
            return List.of("D", "C", "B", "A", "AA", "AAA");
        }
        
        // 等级序列：D < C < B < A < AA < AAA
        List<String> allLevels = List.of("D", "C", "B", "A", "AA", "AAA");
        int minIndex = allLevels.indexOf(minLevel);
        
        if (minIndex == -1) {
            return List.of("D", "C", "B", "A", "AA", "AAA");
        }
        
        return allLevels.subList(minIndex, allLevels.size());
    }
} 
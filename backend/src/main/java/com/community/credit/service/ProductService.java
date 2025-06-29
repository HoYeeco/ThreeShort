package com.community.credit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.dto.ExchangeRequest;
import com.community.credit.dto.ProductQueryRequest;
import com.community.credit.dto.ProductRequest;
import com.community.credit.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品列表
     * 
     * @param request 查询条件
     * @return 商品分页列表
     */
    IPage<Product> getProductList(ProductQueryRequest request);

    /**
     * 根据用户信用等级获取可兑换商品
     * 
     * @param userId 用户ID
     * @return 可兑换商品列表
     */
    List<Product> getAvailableProducts(Integer userId);

    /**
     * 创建商品
     * 
     * @param request 商品信息
     */
    Integer createProduct(ProductRequest request);

    /**
     * 更新商品
     * 
     * @param id 商品ID
     * @param request 更新信息
     */
    void updateProduct(Integer id, ProductRequest request);

    /**
     * 更新商品库存
     * 
     * @param id 商品ID
     * @param quantity 库存数量
     */
    void updateStock(Integer id, Integer quantity);

    /**
     * 积分兑换商品
     * 
     * @param request 兑换请求
     */
    void exchangeProduct(ExchangeRequest request);

    /**
     * 检查兑换资格
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 检查结果
     */
    Map<String, Object> checkExchangeEligibility(Integer userId, Integer productId);

    /**
     * 获取商品统计数据
     * 
     * @return 统计数据
     */
    Map<String, Object> getProductStatistics();

    /**
     * 切换商品状态
     * 
     * @param id 商品ID
     */
    void toggleProductStatus(Integer id);

    /**
     * 获取商品详情
     */
    Product getProductDetail(Integer id);

    /**
     * 获取商品兑换统计
     */
    Map<String, Object> getExchangeStats(Integer productId);

    /**
     * 删除商品（软删除）
     */
    void deleteProduct(Integer id);
} 
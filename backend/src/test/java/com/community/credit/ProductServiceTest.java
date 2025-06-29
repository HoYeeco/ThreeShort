package com.community.credit;

import com.community.credit.dto.ExchangeRequest;
import com.community.credit.dto.ProductRequest;
import com.community.credit.entity.Product;
import com.community.credit.entity.UserCreditProfile;
import com.community.credit.service.ProductService;
import com.community.credit.service.UserCreditProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 积分兑换系统测试类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductServiceTest {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserCreditProfileService userCreditProfileService;

    @Test
    public void testCreateProduct() {
        ProductRequest request = new ProductRequest();
        request.setName("测试商品");
        request.setDescription("这是一个测试商品");
        request.setPointsRequired(100);
        request.setStockQuantity(50);
        request.setCategory("测试分类");
        request.setEligibleLevels(Arrays.asList("A", "AA", "AAA"));
        
        assertDoesNotThrow(() -> {
            productService.createProduct(request);
        });
    }

    @Test
    public void testCheckExchangeEligibility() {
        // 创建测试商品
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("测试商品");
        productRequest.setDescription("测试描述");
        productRequest.setPointsRequired(100);
        productRequest.setStockQuantity(10);
        productRequest.setCategory("测试");
        productRequest.setEligibleLevels(Arrays.asList("A", "AA", "AAA"));
        
        productService.createProduct(productRequest);
        
        // 获取创建的商品
        Product product = productService.list().get(0);
        
        // 创建测试用户档案
        UserCreditProfile profile = new UserCreditProfile();
        profile.setUserId(1);
        profile.setRewardPoints(150);
        profile.setCreditLevel("A");
        userCreditProfileService.save(profile);
        
        // 检查兑换资格
        Map<String, Object> result = productService.checkExchangeEligibility(1, product.getId());
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("eligible"));
    }

    @Test
    public void testExchangeProduct() {
        // 创建测试商品
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("测试兑换商品");
        productRequest.setDescription("测试兑换描述");
        productRequest.setPointsRequired(50);
        productRequest.setStockQuantity(5);
        productRequest.setCategory("测试");
        productRequest.setEligibleLevels(Arrays.asList("A", "AA", "AAA"));
        
        productService.createProduct(productRequest);
        Product product = productService.list().get(0);
        
        // 创建测试用户档案
        UserCreditProfile profile = new UserCreditProfile();
        profile.setUserId(1);
        profile.setRewardPoints(100);
        profile.setCreditLevel("A");
        userCreditProfileService.save(profile);
        
        // 执行兑换 - 使用新的方法签名
        assertDoesNotThrow(() -> {
            productService.exchangeProduct(1, product.getId(), 1);
        });
        
        // 验证库存减少
        Product updatedProduct = productService.getById(product.getId());
        assertEquals(4, updatedProduct.getStockQuantity());
        
        // 验证积分扣减
        UserCreditProfile updatedProfile = userCreditProfileService.getByUserId(1);
        assertEquals(50, updatedProfile.getRewardPoints());
    }

    @Test
    public void testExchangeProductInsufficientPoints() {
        // 创建测试商品
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("高价商品");
        productRequest.setDescription("需要大量积分");
        productRequest.setPointsRequired(200);
        productRequest.setStockQuantity(5);
        productRequest.setCategory("测试");
        productRequest.setEligibleLevels(Arrays.asList("A", "AA", "AAA"));
        
        productService.createProduct(productRequest);
        Product product = productService.list().get(0);
        
        // 创建积分不足的用户档案
        UserCreditProfile profile = new UserCreditProfile();
        profile.setUserId(1);
        profile.setRewardPoints(100);  // 积分不足
        profile.setCreditLevel("A");
        userCreditProfileService.save(profile);
        
        // 应该抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.exchangeProduct(1, product.getId(), 1);
        });
        
        assertTrue(exception.getMessage().contains("积分"));
    }

    @Test
    public void testExchangeProductInsufficientStock() {
        // 创建库存不足的商品
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("库存不足商品");
        productRequest.setDescription("库存很少");
        productRequest.setPointsRequired(50);
        productRequest.setStockQuantity(1);
        productRequest.setCategory("测试");
        productRequest.setEligibleLevels(Arrays.asList("A", "AA", "AAA"));
        
        productService.createProduct(productRequest);
        Product product = productService.list().get(0);
        
        // 创建用户档案
        UserCreditProfile profile = new UserCreditProfile();
        profile.setUserId(1);
        profile.setRewardPoints(200);
        profile.setCreditLevel("A");
        userCreditProfileService.save(profile);
        
        // 尝试兑换超过库存的数量
        // 应该抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.exchangeProduct(1, product.getId(), 2);  // 超过库存
        });
        
        assertTrue(exception.getMessage().contains("库存"));
    }

    @Test
    public void testGetProductStatistics() {
        // 创建几个测试商品
        for (int i = 1; i <= 3; i++) {
            ProductRequest request = new ProductRequest();
            request.setName("商品" + i);
            request.setDescription("描述" + i);
            request.setPointsRequired(i * 50);
            request.setStockQuantity(i * 10);
            request.setCategory("分类" + (i % 2 == 0 ? "A" : "B"));
            request.setEligibleLevels(Arrays.asList("A", "AA", "AAA"));
            
            productService.createProduct(request);
        }
        
        // 获取统计数据
        Map<String, Object> stats = productService.getExchangeStats(1);
        
        assertNotNull(stats);
        // 基本验证统计数据存在
        assertTrue(stats.size() > 0);
    }
} 
package com.example.technova_be.modules.product.service;

import com.example.technova_be.comom.response.GlobalResponse;
import com.example.technova_be.modules.product.dto.OrderItemRequest;
import com.example.technova_be.modules.product.dto.ProductVariantRequest;
import com.example.technova_be.modules.product.dto.PurchaseRequest;
import com.example.technova_be.modules.product.dto.ProductPriceResponse;
import com.example.technova_be.modules.product.dto.ProductResponse;
import com.example.technova_be.modules.product.dto.ProductVariantResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductVariantService {
    GlobalResponse<ProductResponse> createVariantToProduct(ProductVariantRequest variantRequest);

    GlobalResponse<ProductResponse> updateVariantProduct(UUID variantId, ProductVariantRequest variantRequest);

    GlobalResponse<String> deleteProductVariantById(UUID variantId);

    GlobalResponse<String> uploadImageToVariant(UUID variantId, MultipartFile image);

    Boolean checkStock(List<OrderItemRequest> requests);

    List<ProductPriceResponse> getPrices(PurchaseRequest request);

    Void updateStock(List<OrderItemRequest> requests);

    GlobalResponse<ProductVariantResponse> getProductVariantById(UUID variantId);
}

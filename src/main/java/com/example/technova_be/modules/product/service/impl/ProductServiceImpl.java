package com.example.technova_be.modules.product.service.impl;

import com.example.technova_be.comom.exception.NotFoundException;
import com.example.technova_be.comom.response.GlobalResponse;
import com.example.technova_be.modules.product.dto.ProductRequest;
import com.example.technova_be.modules.product.dto.ProductResponse;
import com.example.technova_be.modules.product.dto.UpdateProductRequest;
import com.example.technova_be.modules.product.entity.Category;
import com.example.technova_be.modules.product.entity.Product;
import com.example.technova_be.modules.product.entity.ProductImage;
import com.example.technova_be.modules.product.repository.CategoryRepository;
import com.example.technova_be.modules.product.repository.ProductImageRepository;
import com.example.technova_be.modules.product.repository.ProductRepository;
import com.example.technova_be.modules.product.service.ProductService;
import com.example.technova_be.modules.product.util.FileUtil;
import com.example.technova_be.modules.product.util.ProductMapperUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final FileUtil fileUtil;

    private final ProductMapperUtil productMapperUtil;

    @Override
    @Transactional
    public GlobalResponse<ProductResponse> createProduct(ProductRequest request){
        Category category=requireCategory(request.categoryId());
        Product product =Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .isActive(true)
                .category(category)
                .build();
        String username= SecurityContextHolder.getContext().getAuthentication().getName();
        product.setCreatedBy(username);
        product.setCreatorName(username);
        productRepository.save(product);
        saveProductImages(request.images(), product); // Gọi hàm helper
        return GlobalResponse.ok(productMapperUtil.toProductResponse(product));
    }
    @Override
    @Transactional
    public GlobalResponse<ProductResponse> updateProduct(UUID productId, UpdateProductRequest request){
        Product product=requireProduct(productId);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        if (request.categoryId() != null) {
            Category category = requireCategory(request.categoryId());
            product.setCategory(category); // <--- QUAN TRỌNG: Phải set vào đây!
        }

        if (request.existingImageUrls() != null) {
            List<ProductImage> toDelete = product.getImages().stream()
                    .filter(img -> !request.existingImageUrls().contains(img.getImageUrl()))
                    .toList();
            toDelete.forEach(img -> {
                fileUtil.deleteImage(img.getImageUrl());
                product.getImages().remove(img);
                productImageRepository.delete(img);
            });
        }
        saveProductImages(request.images(), product);
        return GlobalResponse.ok(productMapperUtil.toProductResponse(product));

    }

    private Category requireCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục với ID cung cấp"));
    }
    private Product requireProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID cung cấp"));
    }
    private void saveProductImages(List<MultipartFile> files, Product product) {
        if (files == null || files.isEmpty()) {
            return;
        }

        for (MultipartFile file : files) {
            // 1. Lưu file vật lý và lấy URL
            String url = fileUtil.saveImage(file);

            // 2. Tạo đối tượng Image gắn với Product
            ProductImage image = ProductImage.builder()
                    .imageUrl(url)
                    .product(product)
                    .build();

            // 3. Lưu vào DB và cập nhật danh sách của Product
            productImageRepository.save(image);
            product.getImages().add(image);
        }
    }
}

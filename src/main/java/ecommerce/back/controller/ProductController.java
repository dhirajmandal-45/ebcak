package ecommerce.back.controller;

import ecommerce.back.service.ProductService;
import ecommerce.back.entity.Product;
import ecommerce.back.service.CloudinaryService;
import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products")

public class ProductController {
    private final CloudinaryService cloudinaryService;
    private final ProductService productService;

    public ProductController(ProductService productService, CloudinaryService cloudinaryService) {
        this.productService = productService;
        this.cloudinaryService = cloudinaryService;
    }

    // @PostMapping("/upload")
    // public String uploadImage(@RequestParam("image") MultipartFile image) throws
    // IOException {
    // // Here you can implement the logic to handle the image upload
    // // For example, you can call a service method to upload the image to
    // Cloudinary
    // // and return the URL of the uploaded image.

    // return cloudinaryService.uploadImage(image); // Placeholder response, replace
    // with actual implementation
    // }

    // create product
    @PostMapping
    public Product createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("category") String category,
            @RequestParam("brand") String brand,
            @RequestParam("color") String color,
            @RequestParam("size") String size,
            @RequestParam("stock") Integer stock,
            @RequestParam("rating") Double rating,
            @RequestParam("discount") Double discount,
            @RequestParam("image") MultipartFile image) throws IOException {

        // Upload image to Cloudinary
        String imageUrl = cloudinaryService.uploadImage(image);

        // Create Product object
        Product product = new Product();

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setBrand(brand);
        product.setColor(color);
        product.setSize(size);
        product.setStock(stock);
        product.setRating(rating);
        product.setDiscount(discount);

        // Save Cloudinary URL
        product.setImageUrl(imageUrl);

        // Save product in MySQL
        return productService.createProduct(product);
    }

    // @GetMapping
    // public List<Product> getAllProducts() {
    // return productService.getAllProducts();
    // }
    @GetMapping
    public List<Product> getAllProducts(
            @RequestParam(required = false) String category) {

        if (category != null && !category.isEmpty()) {
            return productService.getProductsByCategory(category);
        }

        return productService.getAllProducts();
    }

    // GET PRODUCT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {

        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE PRODUCT
    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Integer id,
            @RequestBody Product product) {

        return productService.updateProduct(id, product);
    }

    // DELETE PRODUCT
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer id) {

        productService.deleteProduct(id);

        return ResponseEntity.ok("Product deleted successfully");
    }
}

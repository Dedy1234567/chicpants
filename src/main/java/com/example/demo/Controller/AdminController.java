package com.example.demo.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Model.History;
import com.example.demo.Model.Product;
import com.example.demo.Model.User;
import com.example.demo.Model.Role.ERole;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Repository.JustLoggedRepository;
import com.example.demo.Repository.ProductRepository;
import com.example.demo.Service.HistoryService;
import com.example.demo.Service.KeranjangService;
import com.example.demo.Service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

// Tambahkan import ini di bagian atas AdminController.java

import java.nio.file.Files;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JustLoggedRepository justLoggedRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private KeranjangService keranjangService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private CategoryRepository categoryRepository;

    // ================== ADMIN MENU ==================
    @GetMapping("/menu-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String menuAdmin(Model model) {
        // Total Users
        long totalUsers = userRepository.count();

        // Total Products
        long totalProducts = productRepository.count();

        // Total Orders
        long totalOrders = historyService.getAllHistory().size();

        // Total Revenue (harga * jumlah)
        double totalRevenue = historyService.getAllHistory()
                .stream()
                .mapToDouble(h -> h.getProduct().getHarga() * h.getJumlah())
                .sum();

        // Masukkan ke model
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);

        // Daftar produk (kalau masih mau ditampilkan di tabel)
        model.addAttribute("pro", productService.getAllProduct());

        return "menu-admin"; // ini akan render file menu-admin.html di templates
    }

    @GetMapping("/menu-sort")
    @PreAuthorize("hasRole('ADMIN')")
    public String menuSort(Model model) {
        List<Product> listProducts = productRepository.findAllByHargaAsc();
        model.addAttribute("pro", listProducts);
        return "menu-admin";
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public String search(@RequestParam("search") String query, Model model) {
        List<Product> products = productRepository.findByNameProductContainingIgnoreCase(query);
        model.addAttribute("pro", products);
        return "menu-admin";
    }

    // ================== PRODUCT ==================
    // ================== PRODUCT MANAGEMENT ==================
    @GetMapping("/manage-product")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageProduct(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("product", new Product()); // untuk form tambah
        model.addAttribute("categories", productService.getAllCatagories());

        return "manage-product";
    }

    // Key improvements needed in your AdminController:
    // Key improvements needed in your AdminController:

    // 1. Fix the edit product functionality (current issue: populateForm()
    // redirects instead of populating modal)
    @GetMapping("/edit-product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editProductPage(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("categories", productService.getAllCatagories());
        model.addAttribute("isEditMode", true); // Add this flag
        return "manage-product";
    }

    // 2. Add AJAX endpoint to get product data for modal (better UX)
    @GetMapping("/api/product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. Improve file upload handling with better validation
    @PostMapping("/add-product")
    @PreAuthorize("hasRole('ADMIN')")
    public String addProduct(@ModelAttribute Product product,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            if (file != null && !file.isEmpty()) {
                // --- Validasi ukuran dan tipe file (Kode Anda sudah bagus, tidak perlu diubah)
                // ---
                if (file.getSize() > 5 * 1024 * 1024) { // Max 5MB
                    redirectAttributes.addFlashAttribute("error", "File size must be less than 5MB");
                    return "redirect:/manage-product";
                }
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("error", "Only image files are allowed");
                    return "redirect:/manage-product";
                }

                // --> PERUBAHAN DI SINI: Menentukan path ke folder di luar 'src'
                // System.getProperty("user.dir") adalah direktori root tempat aplikasi Anda
                // berjalan.
                Path uploadDir = Paths.get(System.getProperty("user.dir"), "upload-dir", "image");

                // Membuat direktori jika belum ada
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                // --- Generate nama file unik (Kode Anda sudah bagus, tidak perlu diubah) ---
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = System.currentTimeMillis() + fileExtension;

                // Menyimpan file ke path tujuan
                Path targetPath = uploadDir.resolve(uniqueFilename);
                file.transferTo(targetPath.toFile());

                // Menyimpan nama file ke objek produk
                // PENTING: Nama file yang disimpan HANYA nama filenya saja, bukan path
                // lengkapnya.
                product.setGambar(uniqueFilename);
            }

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Produk berhasil ditambahkan!");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal mengunggah gambar: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal menambahkan produk: " + e.getMessage());
        }
        return "redirect:/manage-product";
    }

    // 4. Improve update product method
    @PostMapping("/update-product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProduct(@PathVariable Integer id,
            @ModelAttribute Product product,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                redirectAttributes.addFlashAttribute("error", "Produk tidak ditemukan");
                return "redirect:/manage-product";
            }

            // Update product fields
            existingProduct.setNameProduct(product.getNameProduct());
            existingProduct.setHarga(product.getHarga());
            existingProduct.setDes(product.getDes());
            existingProduct.setSize(product.getSize());
            existingProduct.setStok(product.getStok());
            existingProduct.setCategory(product.getCategory());

            // Handle file upload if new file provided
            if (file != null && !file.isEmpty()) {
                // Validate file (same as in add method)
                if (file.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "File size must be less than 5MB");
                    return "redirect:/manage-product";
                }

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("error", "Only image files are allowed");
                    return "redirect:/manage-product";
                }

                // Delete old image if exists
                if (existingProduct.getGambar() != null) {
                    Path oldImagePath = Paths.get(System.getProperty("user.dir"), "src", "main",
                            "resources", "static", "uploads", existingProduct.getGambar());
                    try {
                        Files.deleteIfExists(oldImagePath);
                    } catch (IOException e) {
                        System.err.println("Failed to delete old image: " + e.getMessage());
                    }
                }

                // Save new image
                Path uploadDir = Paths.get(System.getProperty("user.dir"), "src", "main",
                        "resources", "static", "uploads");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String originalFilename = file.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = System.currentTimeMillis() + "_" +
                        originalFilename.replaceAll("[^a-zA-Z0-9.]", "_");

                Path targetPath = uploadDir.resolve(uniqueFilename);
                file.transferTo(targetPath.toFile());
                existingProduct.setGambar(uniqueFilename);
            }

            productService.saveProduct(existingProduct);
            redirectAttributes.addFlashAttribute("success", "Produk berhasil diperbarui!");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal mengunggah gambar: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui produk: " + e.getMessage());
        }
        return "redirect:/manage-product";
    }

    // 5. Add search functionality for products
    @GetMapping("/manage-product/search")
    @PreAuthorize("hasRole('ADMIN')")
    public String searchProducts(@RequestParam("query") String query, Model model) {
        List<Product> products;
        if (query != null && !query.trim().isEmpty()) {
            products = productRepository.findByNameProductContainingIgnoreCase(query.trim());
        } else {
            products = productRepository.findAll();
        }

        model.addAttribute("products", products);
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCatagories());
        model.addAttribute("searchQuery", query);
        return "manage-product";
    }

    // 6. Add bulk actions endpoint
    @PostMapping("/manage-product/bulk-action")
    @PreAuthorize("hasRole('ADMIN')")
    public String bulkAction(@RequestParam("action") String action,
            @RequestParam("productIds") List<Integer> productIds,
            RedirectAttributes redirectAttributes) {
        try {
            switch (action) {
                case "delete":
                    for (Integer id : productIds) {
                        Product product = productService.getProductById(id);
                        if (product != null) {
                            keranjangService.hapusKeranjangDenganProduct(product);
                            historyService.hapusHistoryDenganProduct(product);
                            productRepository.deleteById(id);
                        }
                    }
                    redirectAttributes.addFlashAttribute("success",
                            productIds.size() + " produk berhasil dihapus");
                    break;

                case "activate":
                    // Implement if you have active/inactive status
                    redirectAttributes.addFlashAttribute("success",
                            productIds.size() + " produk berhasil diaktifkan");
                    break;

                default:
                    redirectAttributes.addFlashAttribute("error", "Aksi tidak valid");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal melakukan aksi: " + e.getMessage());
        }

        return "redirect:/manage-product";
    }

    // 7. Add product statistics endpoint
    @GetMapping("/api/product-stats")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public Map<String, Object> getProductStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Product> allProducts = productRepository.findAll();

        stats.put("totalProducts", allProducts.size());
        stats.put("lowStockProducts", allProducts.stream()
                .filter(p -> p.getStok() <= 10).count());
        stats.put("outOfStockProducts", allProducts.stream()
                .filter(p -> p.getStok() == 0).count());
        stats.put("totalValue", allProducts.stream()
                .mapToDouble(p -> p.getHarga() * p.getStok()).sum());

        return stats;
    }

    // 8. Serve images from external upload directory
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path imagePath = Paths.get("upload-dir", "image", filename);
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = Files.probeContentType(imagePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache for 1 hour
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/delete-product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(id);
        keranjangService.hapusKeranjangDenganProduct(product);
        historyService.hapusHistoryDenganProduct(product);
        productRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Produk berhasil dihapus.");
        return "redirect:/manage-product";
    }

    // ================== USER MANAGEMENT ==================
    @GetMapping("/manage-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageUser(@RequestParam(value = "search", required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        int pageSize = 5; // tampilkan 5 user per halaman
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<User> usersPage;

        if (search != null && !search.isEmpty()) {
            usersPage = userRepository.findByUsernameContainingIgnoreCase(search, pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("search", search);

        return "manage-user";
    }

    @GetMapping("/delete-user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User tidak ditemukan.");
            return "redirect:/manage-user";
        }

        User user = userOpt.get();

        boolean isAdmin = user.getRoles() != null && user.getRoles().stream()
                .map(role -> role.getName()) // jika Role.getName() => ERole
                .anyMatch(name -> name == ERole.ROLE_ADMIN);

        if (isAdmin) {
            redirectAttributes.addFlashAttribute("error", "Tidak bisa menghapus akun dengan role ADMIN.");
            return "redirect:/manage-user";
        }

        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "User berhasil dihapus.");
        return "redirect:/manage-user";
    }

    // ================== SALES HISTORY ==================
    @GetMapping("/data-penjualan")
    @PreAuthorize("hasRole('ADMIN')")
    public String history(Model model) {
        List<History> histories = historyService.getAllHistory();
        model.addAttribute("his", histories);
        return "data-pemesanan";
    }

    @GetMapping("/history-userr/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String histories(@PathVariable("id") Long id, Model model) {
        List<History> histories = historyService.getAllHistoryById(id);
        model.addAttribute("his", histories);
        return "histories";
    }
}
package com.example.demo.Service;

import com.example.demo.Model.*;
import com.example.demo.Model.Role.ERole;
import com.example.demo.Repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {

        // ======== ROLE ========
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(ERole.ROLE_USER);
            roleRepository.save(userRole);
            System.out.println("Membuat peran default: ROLE_USER");
        }

        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);
            System.out.println("Membuat peran default: ROLE_ADMIN");
        }

        // ======== ADMIN DEFAULT ========
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(encoder.encode("admin123")); // password default
            admin.setSaldo(0.0);

            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByName(ERole.ROLE_ADMIN).get());
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("✅ Akun admin default dibuat (username=admin, password=admin123)");
        }

        // ======== KATEGORI ========
        if (categoryRepository.count() == 0) {
            Category c1 = new Category();
            c1.setName("Celana");

            Category c2 = new Category();
            c2.setName("Kemeja");

            Category c3 = new Category();
            c3.setName("Sweater");

            Category c4 = new Category();
            c4.setName("jaket");

            Category c5 = new Category();
            c5.setName("Aksesoris");

            categoryRepository.saveAll(List.of(c1, c2, c3, c4, c5));
            System.out.println("✅ Kategori default berhasil dibuat.");
        }

        // ======== PRODUK CONTOH ========
        if (productRepository.count() == 0) {
            List<Category> categories = categoryRepository.findAll();
            List<Product> products = new ArrayList<>();

            // Atasan
            products.add(new ProductBuilder("Kaos Polos", "Kaos nyaman untuk sehari-hari", "M", 100_000, 50,
                    "kaos_polos.jpg", categories.get(0)).build());
            products.add(new ProductBuilder("Kemeja Lengan Panjang", "Kemeja formal pria", "L", 200_000, 30,
                    "kemeja.jpg", categories.get(0)).build());
            products.add(new ProductBuilder("Blouse Wanita", "Blouse cantik untuk kerja", "M", 150_000, 25,
                    "blouse.jpg", categories.get(0)).build());

            // Bawahan
            products.add(new ProductBuilder("Celana Jeans", "Celana jeans trendy", "L", 200_000, 30,
                    "celana_jeans.jpg", categories.get(1)).build());
            products.add(new ProductBuilder("Rok Pendek", "Rok modis wanita", "M", 120_000, 40,
                    "rok_pendek.jpg", categories.get(1)).build());
            products.add(new ProductBuilder("Celana Chino", "Celana chino santai", "L", 180_000, 20,
                    "celana_chino.jpg", categories.get(1)).build());

            // Sepatu
            products.add(new ProductBuilder("Sneakers", "Sepatu casual nyaman", "42", 350_000, 20,
                    "sneakers.jpg", categories.get(2)).build());
            products.add(new ProductBuilder("Sepatu Formal Pria", "Sepatu kulit formal", "43", 500_000, 15,
                    "sepatu_formal.jpg", categories.get(2)).build());
            products.add(new ProductBuilder("Sandal Wanita", "Sandal nyaman sehari-hari", "38", 80_000, 50,
                    "sandal.jpg", categories.get(2)).build());

            // Aksesoris
            products.add(new ProductBuilder("Topi Baseball", "Topi trendy", "L", 50_000, 40, "topi.jpg",
                    categories.get(3)).build());
            products.add(new ProductBuilder("Kacamata Hitam", "Kacamata stylish", "M", 120_000, 35,
                    "kacamata.jpg", categories.get(3)).build());
            products.add(new ProductBuilder("Jam Tangan", "Jam tangan casual", "One Size", 250_000, 20,
                    "jam.jpg", categories.get(3)).build());

            // Outerwear
            products.add(new ProductBuilder("Jaket Hoodie", "Jaket hangat kasual", "M", 200_000, 25,
                    "hoodie.jpg", categories.get(4)).build());
            products.add(new ProductBuilder("Coat Panjang", "Coat elegan", "L", 450_000, 10, "coat.jpg",
                    categories.get(4)).build());
            products.add(new ProductBuilder("Sweater Rajut", "Sweater nyaman", "M", 180_000, 20, "sweater.jpg",
                    categories.get(4)).build());

            productRepository.saveAll(products);
            System.out.println("✅ Produk contoh lengkap berhasil dibuat.");
        }

        System.out.println("=== Data awal berhasil diinisialisasi ===");
    }

    // ======== HELPER BUILDER ========
    private static class ProductBuilder {
        private final Product product;

        public ProductBuilder(String name, String des, String size, int harga, int stok, String gambar,
                Category category) {
            product = new Product();
            product.setNameProduct(name);
            product.setDes(des);
            product.setSize(size);
            product.setHarga(harga);
            product.setStok(stok);
            product.setGambar(gambar);
            product.setCategory(category);
        }

        public Product build() {
            return product;
        }
    }
}
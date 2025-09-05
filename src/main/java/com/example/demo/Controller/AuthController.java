package com.example.demo.Controller;

import com.example.demo.Model.JustLogged;
import com.example.demo.Model.Keranjang;
import com.example.demo.Model.Product;
import com.example.demo.Model.User;
import com.example.demo.Repository.JustLoggedRepository;
import com.example.demo.Repository.ProductRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.CheckOutService;
import com.example.demo.Service.HistoryService;
import com.example.demo.Service.JustLoggedService;
import com.example.demo.Service.KeranjangService;
import com.example.demo.Service.ProductService;
import com.example.demo.Service.UserService;
import com.example.demo.dto.LoginRequest;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.UUID;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

@Controller
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JustLoggedRepository justLoggedRepository;
    @Autowired
    private JustLoggedService justLoggedService;
    @Autowired
    private KeranjangService keranjangService;
    @Autowired
    private CheckOutService checkOutService;
    @Autowired
    private HistoryService historyService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private Map<String, PasswordResetToken> resetTokens = new ConcurrentHashMap<>();

    // Inner class untuk token
    private static class PasswordResetToken {
        private String email;
        private LocalDateTime expiryTime;

        public PasswordResetToken(String email, LocalDateTime expiryTime) {
            this.email = email;
            this.expiryTime = expiryTime;
        }

        public String getEmail() {
            return email;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    // ================ SIGN UP =================
 @GetMapping("/signup")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User()); // Pastikan User diimpor dari com.example.demo.Model.User
        return "sign-up";
    }

    @PostMapping("/signup")
    public String processRegistration(@Valid @ModelAttribute User user, 
                                      BindingResult bindingResult, 
                                      Model model) {
        
        // Cek apakah ada error validasi dasar dari anotasi
        if (bindingResult.hasErrors()) {
            // Jika ada error, kembali ke halaman sign-up dengan pesan error
            // Thymeleaf akan otomatis menampilkan error per-field jika Anda mengaturnya
            return "sign-up"; 
        }

        // --- Validasi Lanjutan (yang tidak bisa dilakukan dengan anotasi) ---
        
        // Cek apakah username sudah ada
        if (userRepository.existsByUsername(user.getUsername())) {
            // Tambahkan error manual ke BindingResult
            bindingResult.addError(new FieldError("user", "username", "Username sudah digunakan, silakan pilih yang lain."));
        }
        
        // Cek apakah email sudah ada
        if (userRepository.existsByEmail(user.getEmail())) {
            bindingResult.addError(new FieldError("user", "email", "Alamat email ini sudah terdaftar."));
        }

        // Jika setelah pengecekan lanjutan ada error, kembali lagi
        if (bindingResult.hasErrors()) {
            return "sign-up";
        }
        
        // Jika semua validasi lolos, lanjutkan proses registrasi
        try {
            userService.registerUser(user);
            return "redirect:/signin?success";
        } catch (Exception e) {
            // Tangkap error tak terduga dari service
            model.addAttribute("errorMessage", "Terjadi kesalahan saat registrasi. Silakan coba lagi.");
            return "sign-up";
        }
    }

    // ================ SEARCH =================
    @GetMapping("/search-user")
    public String search(@RequestParam("search-user") String query, Model model) {
        List<Product> products = productRepository.findByNameProductContainingIgnoreCase(query);
        model.addAttribute("products", products);
        return "search-user";
    }

    // ================ SIGN IN =================
    @GetMapping("/signin")
    public String showLoginForm(Model model, @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout) {
        model.addAttribute("loginRequest", new LoginRequest());
        if (error != null) {
            model.addAttribute("errorMessage", "Username atau password salah.");
        } else if (logout != null) {
            model.addAttribute("message", "Anda berhasil logout.");
        }
        return "sign-in";
    }

    @GetMapping("/product-detail/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        // Ambil data produk berdasarkan ID
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "product-detail"; // nama file template
    }

    @PostMapping("/signin")
    public String authenticateUser(@ModelAttribute LoginRequest loginRequest,
            HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Simpan di cookie HttpOnly
            ResponseCookie cookie = ResponseCookie.from("jwt-token", jwt)
                    .httpOnly(true)
                    .secure(false) // ubah ke true jika pakai HTTPS
                    .path("/")
                    .maxAge(24 * 60 * 60) // 1 hari
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Ambil user details (safe cast karena authentication dari
            // AuthenticationManager)
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Simpan/Catat login di just_logged (bersihkan dulu)
            justLoggedRepository.deleteAll();
            JustLogged loged = new JustLogged();
            loged.setUsername(userDetails.getUsername());
            // Jangan simpan plain password di production. Ini hanya mengikuti struktur yang
            // kamu pakai.
            loged.setPassword(loginRequest.getPassword());
            loged.setUser(userRepository.findByUsername(userDetails.getUsername()).orElseThrow());
            justLoggedRepository.save(loged);

            // Redirect sesuai role
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
            return isAdmin ? "redirect:/menu-admin" : "redirect:/menu-user";

        } catch (Exception e) {
            // Untuk debugging internal, cetak stacktrace ke console (jangan di produksi)
            e.printStackTrace();
            return "redirect:/signin?error";
        }
    }

    // ================ HOME / MENU-USER =================
   // ================ HOME / MENU-USER =================
@GetMapping({ "/", "/menu-user" })
public String showHomePage(Model model) {
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Cek apakah user sudah login dan bukan user anonim
        if (authentication != null && 
            !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) && 
            authentication.isAuthenticated()) {
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            model.addAttribute("username", userDetails.getUsername());
            model.addAttribute("isLoggedIn", true);

        } else {
            // Jika belum login atau sesi tidak valid
            model.addAttribute("isLoggedIn", false);
        }

        // Selalu kirim daftar produk
        model.addAttribute("pro", productService.getAllProduct());

    } catch (Exception e) {
        // Jika terjadi error tak terduga, cetak log dan kirim daftar kosong
        // untuk mencegah halaman error
        e.printStackTrace();
        model.addAttribute("pro", List.of());
        model.addAttribute("isLoggedIn", false); 
    }
    return "menu-user";
}

    // ================ AKSES DITOLAK =================
    @GetMapping("/akses-ditolak")
    public String aksesDitolak(Model model) {
        model.addAttribute("message", "Anda belum login, silakan login terlebih dahulu.");
        return "akses-ditolak";
    }

    // ================ PROFILE / CART / CHECKOUT / KERANJANG ================

    @GetMapping("/cart")
    public String cartPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            return "redirect:/akses-ditolak";
        }

        JustLogged justLogged = justLoggedService.getJustLoggedByfirst();
        if (justLogged == null || justLogged.getUser() == null) {
            return "redirect:/akses-ditolak";
        }

        Long userId = justLogged.getUser().getId();
        var cartList = keranjangService.getKeranjangByUserId(userId);
        model.addAttribute("cart", cartList);
        model.addAttribute("isEmpty", cartList.isEmpty());
        return "keranjang";
    }

    @GetMapping("/check-out-one/{id}")
    public String checkout(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            return "redirect:/akses-ditolak";
        }

        JustLogged justLogged = justLoggedService.getJustLoggedByfirst();
        if (justLogged == null || justLogged.getUser() == null) {
            return "redirect:/akses-ditolak";
        }

        Product product = productService.getProductById(id.intValue());
        model.addAttribute("checkout", product);
        return "checkout-langsung";
    }

    @GetMapping("/cancel-checkout")
    public String cancelChekout() {
        checkOutService.deleteAllCheckOut();
        return "redirect:/menu-user";
    }

    @GetMapping("/keranjang/{id}")
    public String keranjang(@PathVariable Integer id, Model model) {
        Product product = productRepository.getReferenceById(id);
        JustLogged justLogged = justLoggedService.getJustLoggedByfirst();

        if (justLogged == null || justLogged.getUser() == null) {
            return "redirect:/akses-ditolak";
        }

        Boolean cek = keranjangService.getKeranjangByProduct(id, justLogged.getUser().getId());
        if (cek) {
            Keranjang keranjang = new Keranjang();
            keranjang.setProduct(product);
            keranjang.setUser(justLogged.getUser());
            keranjangService.keranjang(keranjang);
            return "berhasil-add-keranjang";
        } else {
            return "gagal-keranjang";
        }
    }

    @GetMapping("/profile")
    public String profileUser(Model model, RedirectAttributes redirectAttributes) {
        try {
            JustLogged justLogged = justLoggedService.getJustLoggedByfirst();
            if (justLogged == null || justLogged.getUser() == null) {
                redirectAttributes.addFlashAttribute("error", "Please login first");
                return "redirect:/signin";
            }

            // Get history data
            var historyList = historyService.getHistoryByJustLoggedUser(justLogged);
            model.addAttribute("his", historyList);
            model.addAttribute("profile", justLogged);

            // Calculate total spent
            long totalSpent = 0;
            if (historyList != null && !historyList.isEmpty()) {
                totalSpent = historyList.stream()
                        .mapToLong(h -> h.getJumlah() * h.getProduct().getHarga())
                        .sum();
            }
            model.addAttribute("totalSpent", "Rp " + String.format("%,d", totalSpent));

            return "profile-user";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while loading profile");
            return "redirect:/menu-user";
        }
    }

    // ========== TAMBAHKAN METHOD-METHOD BARU INI ==========

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam(value = "saldo", required = false) Double saldo,
            RedirectAttributes redirectAttributes) {
        try {
            JustLogged justLogged = justLoggedService.getJustLoggedByfirst();
            if (justLogged == null || justLogged.getUser() == null) {
                redirectAttributes.addFlashAttribute("error", "Please login first");
                return "redirect:/signin";
            }

            User user = justLogged.getUser();

            // Validasi
            if (username.trim().isEmpty() || email.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Username and email cannot be empty");
                return "redirect:/profile";
            }

            // Cek username sudah ada
            Optional<User> existingUser = userRepository.findByUsername(username);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Username already taken");
                return "redirect:/profile";
            }

            // Cek email sudah ada
            Optional<User> existingEmailUser = userRepository.findByEmail(email);
            if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Email already used");
                return "redirect:/profile";
            }

            // Update user
            user.setUsername(username);
            user.setEmail(email);
            if (saldo != null && saldo >= 0) {
                user.setSaldo(saldo);
            }
            userRepository.save(user);

            // Update JustLogged
            justLogged.setUsername(username);
            justLoggedRepository.save(justLogged);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update profile");
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        try {
            JustLogged justLogged = justLoggedService.getJustLoggedByfirst();
            if (justLogged == null || justLogged.getUser() == null) {
                redirectAttributes.addFlashAttribute("error", "Please login first");
                return "redirect:/signin";
            }

            // Validasi password saat ini
            if (!currentPassword.equals(justLogged.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                return "redirect:/profile";
            }

            // Validasi password baru
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "New password must be at least 6 characters");
                return "redirect:/profile";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match");
                return "redirect:/profile";
            }

            // Update password
            User user = justLogged.getUser();
            user.setPassword(newPassword);
            userRepository.save(user);

            justLogged.setPassword(newPassword);
            justLoggedRepository.save(justLogged);

            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to change password");
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/request-password-reset")
    public String requestPasswordReset(@RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        try {
            // Cek email ada di database
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "No account found with this email");
                return "redirect:/profile";
            }

            User user = userOpt.get();

            // Generate token
            String resetToken = UUID.randomUUID().toString();
            LocalDateTime expiryTime = LocalDateTime.now().plusHours(1);

            // Simpan token
            resetTokens.put(resetToken, new PasswordResetToken(email, expiryTime));

            // Kirim email
            if (mailSender != null) {
                try {
                    String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;

                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(email);
                    message.setSubject("Password Reset - MyStore");
                    message.setText("Hi " + user.getUsername() + ",\n\n" +
                            "Click this link to reset your password:\n" + resetLink +
                            "\n\nThis link expires in 1 hour.\n\nBest regards,\nMyStore Team");

                    mailSender.send(message);

                    redirectAttributes.addFlashAttribute("success",
                            "Reset instructions sent to: " + email);
                } catch (Exception e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error",
                            "Failed to send email. Check email configuration.");
                }
            } else {
                // Jika email belum dikonfigurasi
                String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;
                redirectAttributes.addFlashAttribute("success",
                        "Email not configured. Test link: " + resetLink);
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to process request");
        }

        return "redirect:/profile";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        try {
            PasswordResetToken resetToken = resetTokens.get(token);

            if (resetToken == null || resetToken.isExpired()) {
                model.addAttribute("error", "Invalid or expired reset token");
                return "password-reset-error";
            }

            model.addAttribute("token", token);
            model.addAttribute("email", resetToken.getEmail());
            return "password-reset-form";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error processing request");
            return "password-reset-error";
        }
    }

    @PostMapping("/reset-password")
    public String processPasswordReset(@RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        try {
            PasswordResetToken resetToken = resetTokens.get(token);

            if (resetToken == null || resetToken.isExpired()) {
                redirectAttributes.addFlashAttribute("error", "Invalid or expired token");
                return "redirect:/signin";
            }

            // Validasi password
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters");
                return "redirect:/reset-password?token=" + token;
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/reset-password?token=" + token;
            }

            // Update password
            Optional<User> userOpt = userRepository.findByEmail(resetToken.getEmail());
            if (!userOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/signin";
            }

            User user = userOpt.get();
            user.setPassword(newPassword);
            userRepository.save(user);

            // Hapus token
            resetTokens.remove(token);

            redirectAttributes.addFlashAttribute("success",
                    "Password reset successfully! Please login with your new password.");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to reset password");
        }

        return "redirect:/signin";
    }
}

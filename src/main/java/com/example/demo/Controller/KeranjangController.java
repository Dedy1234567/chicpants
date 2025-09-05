package com.example.demo.Controller;

import com.example.demo.Model.CheckOut;
import com.example.demo.Model.JustLogged;
import com.example.demo.Model.Product;
import com.example.demo.Repository.KeranjangRepository;
import com.example.demo.Repository.ProductRepository;
import com.example.demo.Service.CheckOutService;
import com.example.demo.Service.HistoryService;
import com.example.demo.Service.JustLoggedService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class KeranjangController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JustLoggedService justLoggedService;

    @Autowired
    private KeranjangRepository keranjangRepository;

    @Autowired
    private CheckOutService checkOutService;

    @Autowired
    private HistoryService historyService;

    // @GetMapping("/keranjang/{id}")
    // public String keranjang(@PathVariable Integer id, Model model) {
    //     Product product = productRepository.getReferenceById(id);
    //     JustLogged justLogged = justLoggedService.getJustLoggedByfirst();
    //     Boolean cek = keranjangService.getKeranjangByProduct(id, justLogged.getUser().getId());
    //     if (cek) {
    //         Keranjang keranjang = new Keranjang();
    //         keranjang.setProduct(product);
    //         keranjang.setUser(justLogged.getUser());
    //         keranjangService.keranjang(keranjang);
    //         return "berhasil-add-keranjang";
    //     } else {
    //         return "gagal-keranjang";
    //     }
    // }

    @GetMapping("/delete-dikeranjang/{id}")
    public String deleteDiKeranjang(@PathVariable Integer id) {
        keranjangRepository.deleteById(id);
        return "redirect:/cart";
    }

    // @GetMapping("/cart")
    // public String cart(Model model) {
    //     // Ambil user yang terakhir login
    //     JustLogged justLogged = justLoggedService.getJustLoggedByfirst();

    //     // Jika belum login
    //     if (justLogged == null || justLogged.getUser() == null) {
    //         model.addAttribute("message", "Anda belum login, silahkan register jika belum punya akun.");
    //         return "akses-ditolak"; // arahkan ke not-logged.html
    //     }

    //     // Jika sudah login, ambil keranjang user
    //     List<Keranjang> keranjangList = keranjangService.getKeranjangByJustLoggedUser(justLogged);
    //     model.addAttribute("ker", keranjangList);
    //     model.addAttribute("isEmpty", keranjangList.isEmpty());

    //     return "keranjang";
    // }

    // @GetMapping("/check-out-one/{id}")
    // public String checkoutlangsung(@PathVariable Integer id) {

    //     // Membuat list produk dan menambahkan ID produk yang dipilih
    //     List<Integer> listProduct = new ArrayList<>();
    //     listProduct.add(id);

    //     // Membuat URL dengan query parameter 'items' yang berisi ID produk
    //     String itemsParam = listProduct.stream()
    //             .map(String::valueOf)
    //             .collect(Collectors.joining(","));

    //     // Mengarahkan ke URL '/check-out' dengan query parameter 'items'
    //     return "redirect:/check-out?items=" + itemsParam;
    // }

    @GetMapping("/check-out")
    public String checkout(@RequestParam("items") List<Integer> items) {
        checkOutService.deleteAllCheckOut();
        System.out.println("berhasil ke checkout " + items);
        JustLogged justLogged = justLoggedService.getJustLoggedByfirst();
        for (Integer x : items) {
            CheckOut checkOut = new CheckOut();
            System.out.println("banyak1");
            Product product = productRepository.getReferenceById(x);
            checkOut.setProduct(product);
            checkOut.setUser(justLogged.getUser());
            checkOut.setJumlah(0);
            checkOutService.saveCheckOut(checkOut);
        }
        // Proses logika checkout dengan ID yang diterima
        return "redirect:/get-check-out"; // Tampilkan halaman checkout
    }

    @GetMapping("/batal-check-out")
    public String batalCheckout() {
        checkOutService.deleteAllCheckOut();
        return "redirect:/cart";
    }

    @GetMapping("/success")
    public String processCheckout(@RequestParam("quantity") List<Integer> requestData,
            @RequestParam("productId") List<Integer> productId) {
        // System.out.println("productIds: " + productIds);
        System.out.println("apakah keluar: " + requestData);
        System.out.println("productId: " + productId);

        int idx = 0;
        for (Integer idProduct : productId) {
            if (productRepository.getReferenceById(idProduct).getStok() < requestData.get(idx)) {
                return "pembayaran-gagal";
            }

        }
        for (Integer idProduct : productId) {

            System.out.println();
            historyService.saveHistory(requestData.get(idx), idProduct);
            idx++;
        }
        checkOutService.deleteAllCheckOut();

        return "pembayaran-berhasil"; // Halaman setelah pembayaran berhasil
    }

    // @GetMapping("/history")
    // public String history(@RequestParam("items") List<Integer> items) {
    // System.out.println("data :" + items);
    // return "pembayaran-berhasil";
    // }

    @GetMapping("/get-check-out")
    public String viewCheckOut(Model model) {

        List<CheckOut> checkOutList = checkOutService.getAllCheckOut(); // Ambil semua data CheckOut
        System.out.println("pantekll" + checkOutList);
        JustLogged user = justLoggedService.getJustLoggedByfirst();
        model.addAttribute("checkOut", checkOutList);
        model.addAttribute("saldo", user.getUser().getSaldo());
        return "check-out-barang"; // Nama template Thymeleaf
    }

    

}
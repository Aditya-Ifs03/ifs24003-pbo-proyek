package org.delcom.app.views;

import org.delcom.app.entities.Customer;
import org.delcom.app.entities.User;
import org.delcom.app.services.CustomerService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeView {

    private final CustomerService customerService;

    public HomeView(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // 1. Cek User Login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        if (auth != null && auth.getPrincipal() instanceof User) {
            user = (User) auth.getPrincipal();
            model.addAttribute("auth", user);
        } else {
            return "redirect:/auth/logout";
        }

        // ================= SAFE MODE START =================
        // Kita bungkus logic database dalam try-catch agar halaman tidak error 500
        try {
            List<Customer> allCustomers = customerService.getAllByUserId(user.getId());
            if (allCustomers == null) allCustomers = new ArrayList<>();

            // Statistik
            model.addAttribute("totalCustomers", allCustomers.size());
            
            long vipCount = allCustomers.stream()
                    .filter(c -> c.getType() != null && "VIP".equalsIgnoreCase(c.getType()))
                    .count();
            model.addAttribute("vipCount", vipCount);
            model.addAttribute("newMemberCount", allCustomers.size() - vipCount);

            // Chart Data
            Map<String, Long> chartData = customerService.getChartData(user.getId());
            if (chartData == null) chartData = new HashMap<>();
            model.addAttribute("chartData", chartData);
            
            // Recent Data
            List<Customer> recentCustomers = allCustomers.stream()
                    .filter(c -> c.getCreatedAt() != null)
                    .sorted(Comparator.comparing(Customer::getCreatedAt).reversed())
                    .limit(5)
                    .toList();
            model.addAttribute("recentCustomers", recentCustomers);

        } catch (Exception e) {
            // JIKA ERROR: Tampilkan pesan di Console, tapi jangan bikin halaman crash
            System.err.println("==================================================");
            System.err.println("ERROR DI DASHBOARD: " + e.getMessage());
            e.printStackTrace();
            System.err.println("==================================================");

            // Kirim data kosong agar HTML tidak error
            model.addAttribute("totalCustomers", 0);
            model.addAttribute("vipCount", 0);
            model.addAttribute("newMemberCount", 0);
            model.addAttribute("chartData", new HashMap<>());
            model.addAttribute("recentCustomers", new ArrayList<>());
            model.addAttribute("errorMsg", "Gagal memuat data: " + e.getMessage());
        }
        // ================= SAFE MODE END =================

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }
}
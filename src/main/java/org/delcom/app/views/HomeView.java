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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class HomeView {

    private final CustomerService customerService;

    public HomeView(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // 1. Ambil User dari Security Context
        User user = getAuthUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("auth", user);

        // 2. Ambil Data Pelanggan
        List<Customer> allCustomers = Optional.ofNullable(customerService.getAllByUserId(user.getId()))
                .orElse(Collections.emptyList());

        // 3. Hitung Statistik (Total, VIP, Regular)
        long totalCustomers = allCustomers.size();
        long vipCount = allCustomers.stream()
                .filter(c -> "VIP".equalsIgnoreCase(c.getType()))
                .count();
        long regularCount = totalCustomers - vipCount;

        // 4. Ambil 5 Data Terbaru
        List<Customer> recentCustomers = allCustomers.stream()
                .filter(c -> c.getCreatedAt() != null)
                .sorted(Comparator.comparing(Customer::getCreatedAt).reversed())
                .limit(5)
                .toList();

        // 5. Ambil Data Chart
        Map<String, Long> chartData = customerService.getChartData(user.getId());

        // 6. Masukkan ke Model
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("vipCount", vipCount);
        model.addAttribute("newMemberCount", regularCount); // Menggunakan regular sebagai 'New/Other'
        model.addAttribute("recentCustomers", recentCustomers);
        model.addAttribute("chartData", chartData);

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }

    // Helper method agar pengecekan user lebih rapi
    private User getAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }
}
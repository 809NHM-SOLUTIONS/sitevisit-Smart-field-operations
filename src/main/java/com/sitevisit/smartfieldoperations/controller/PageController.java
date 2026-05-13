package com.sitevisit.smartfieldoperations.controller;

import com.sitevisit.smartfieldoperations.entity.User;
import com.sitevisit.smartfieldoperations.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class PageController {

    private final UserRepository userRepository;
    private final SiteVisitRepository siteVisitRepository;
    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final ReportRepository reportRepository;

    public PageController(
            UserRepository userRepository,
            SiteVisitRepository siteVisitRepository,
            MemberRepository memberRepository,
            CompanyRepository companyRepository,
            ReportRepository reportRepository
    ) {
        this.userRepository = userRepository;
        this.siteVisitRepository = siteVisitRepository;
        this.memberRepository = memberRepository;
        this.companyRepository = companyRepository;
        this.reportRepository = reportRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User user = getLoggedInUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        addUserToModel(model, user);

        model.addAttribute("totalSiteVisits", siteVisitRepository.count());
        model.addAttribute("totalMembers", memberRepository.count());
        model.addAttribute("totalCompanies", companyRepository.count());
        model.addAttribute("totalReports", reportRepository.count());

        model.addAttribute("siteVisits", siteVisitRepository.findAll());
        model.addAttribute("reports", reportRepository.findAll());

        return "dashboard";
    }

    @GetMapping("/companies")
    public String companiesPage(Model model, HttpSession session) {
        User user = getLoggedInUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        addUserToModel(model, user);
        return "companies";
    }

    @GetMapping("/members")
    public String membersPage(Model model, HttpSession session) {
        User user = getLoggedInUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        addUserToModel(model, user);
        return "members";
    }

    @GetMapping("/site-visits")
    public String siteVisitsPage(Model model, HttpSession session) {
        User user = getLoggedInUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        addUserToModel(model, user);
        model.addAttribute("companies", companyRepository.findAll());
        model.addAttribute("siteVisits", siteVisitRepository.findAll());

        return "site-visits";
    }

    @GetMapping("/reports")
    public String reportsPage(Model model, HttpSession session) {
        User user = getLoggedInUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        addUserToModel(model, user);
        model.addAttribute("reports", reportRepository.findAll());

        return "reports";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, HttpSession session) {
        User user = getLoggedInUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        addUserToModel(model, user);
        model.addAttribute("email", user.getEmail());

        return "profile";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model, HttpSession session) {
        User user = getLoggedInUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        addUserToModel(model, user);
        return "change-password";
    }

    @GetMapping("/reminders-notifications")
    public String remindersNotificationsPage(Model model, HttpSession session) {
        User user = getLoggedInUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        addUserToModel(model, user);
        model.addAttribute("email", user.getEmail());

        return "reminders-notifications";
    }

    private User getLoggedInUser(HttpSession session) {
        String email = (String) session.getAttribute("loggedInUserEmail");

        if (email == null || email.isBlank()) {
            return null;
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        return optionalUser.orElse(null);
    }

    private void addUserToModel(Model model, User user) {
        String fullName = user.getFullName();

        model.addAttribute("fullName", fullName);
        model.addAttribute("role", user.getRole());
        model.addAttribute("initial", fullName != null && !fullName.isBlank()
                ? fullName.substring(0, 1).toUpperCase()
                : "U");
    }
}
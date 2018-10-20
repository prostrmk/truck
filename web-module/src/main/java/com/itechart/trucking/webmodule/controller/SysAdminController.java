package com.itechart.trucking.webmodule.controller;

import com.itechart.trucking.company.entity.Company;
import com.itechart.trucking.company.repository.CompanyRepository;
import com.itechart.trucking.stock.entity.Stock;
import com.itechart.trucking.stock.repository.StockRepository;
import com.itechart.trucking.token.entity.Token;
import com.itechart.trucking.token.repository.TokenRepository;
import com.itechart.trucking.user.entity.User;
import com.itechart.trucking.user.repository.UserRepository;
import com.itechart.trucking.webmodule.model.util.EmailUtil;
import com.itechart.trucking.webmodule.model.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Secured("ROLE_SYS_ADMIN")
public class SysAdminController {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping(value = "/createAdmin")
    public String addNewAdmin() {
        return "addAdminBySysAdmin";//input email to send link
    }

    @PostMapping(value = "/createAdmin")
    @ResponseBody
    public Object processAdminAndSendRequestToEmail(@RequestParam String email, @Value("${server.email}") String username, @Value("${server.password}") String password, HttpServletRequest request) {
        String token = TokenUtil.generateToken(40);
        try {
            EmailUtil.sendMail(username, password, email, "Registration",
                    String.format("<h1>Welcome to our system!</h1><br/><h4>To complete registration you need to add your account in our system. Please, visit this <a href=\"%s:%s/regAdmin?token=%s\">link</a> to finish </h4>", request.getServerName(), request.getServerPort(), token)
            );
            tokenRepository.save(new Token(email,token));
            return HttpStatus.OK;
        } catch (Exception e) {
            e.printStackTrace();
            return HttpStatus.BAD_REQUEST;
        }
    }



}

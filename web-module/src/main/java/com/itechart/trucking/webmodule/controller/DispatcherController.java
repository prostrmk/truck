package com.itechart.trucking.webmodule.controller;

import com.itechart.trucking.auto.entity.Auto;
import com.itechart.trucking.client.entity.Client;
import com.itechart.trucking.client.repository.ClientRepository;
import com.itechart.trucking.company.entity.Company;
import com.itechart.trucking.company.repository.CompanyRepository;
import com.itechart.trucking.consignment.entity.Consignment;
import com.itechart.trucking.consignment.repository.ConsignmentRepository;
import com.itechart.trucking.driver.entity.Driver;
import com.itechart.trucking.order.dto.OrderDto;
import com.itechart.trucking.order.entity.Order;
import com.itechart.trucking.order.repository.OrderRepository;
import com.itechart.trucking.order.service.OrderService;
import com.itechart.trucking.stock.entity.Stock;
import com.itechart.trucking.stock.repository.StockRepository;
import com.itechart.trucking.user.entity.User;
import com.itechart.trucking.user.repository.UserRepository;
import com.itechart.trucking.waybill.entity.Waybill;
import com.itechart.trucking.waybill.repository.WaybillRepository;
import com.itechart.trucking.webmodule.config.UserDetailsServiceImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
@CrossOrigin
@RestController
@RequestMapping(value = "/api")
public class DispatcherController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private WaybillRepository waybillRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ConsignmentRepository consignmentRepository;

    @RequestMapping(value = "/orders/createOrder/getDrivers",method = RequestMethod.GET)
    public List<Driver> getDrivers(){
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-M-yyyy");

        /*Заглушка*/
        String strdatefrom = "28-10-2018";
        String strdateto = "29-10-2018";
        Long companyId = 1L;

        java.util.Date datedep = null;
        java.util.Date datearr = null;
        try {
            datedep = dateformat.parse(strdatefrom);
            datearr = dateformat.parse(strdateto);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return waybillRepository.findCustomQueryDriverByDate(datedep,datearr,companyId);

    }

    @RequestMapping(value = "/orders/createOrder/getAutos",method = RequestMethod.GET)
    public List<Auto> getAutos(){
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-M-yyyy");

        /*Заглушка*/
        String strdatefrom = "28-10-2018";
        String strdateto = "29-10-2018";
        Long companyId = 1L;

        java.util.Date datedep = null;
        java.util.Date datearr = null;
        try {
            datedep = dateformat.parse(strdatefrom);
            datearr = dateformat.parse(strdateto);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return waybillRepository.findCustomQueryAutoByDate(datedep,datearr,companyId);
    }

    @RequestMapping(value = "/orders/{id}",method = RequestMethod.GET)
    public Order editOrder(@PathVariable Long id){
        /*        String name = SecurityContextHolder.getContext().getAuthentication().getName();*/
        System.out.println(id);
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        System.out.println(credentials);
        String name = "user6";
        Company company = userRepository.findUserByUsername(name).getCompany();

        Optional<Order> order = orderRepository.findById(id);
        if(order.isPresent() && order.get().getCompany().getId()==company.getId())
        return order.get();
        else{
            System.out.println("access dined");
            return null;
        }
    }

    /*Method use old dto !*/
/*    @PostMapping(value = "/orders/createOrder")
    public Order createOrder(OrderDto orderDto){
        *//*        String name = SecurityContextHolder.getContext().getAuthentication().getName();*//*
        Order order = null;
        try {
            order = orderService.getOrderFromDto(orderDto);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(orderDto + " - order dto ");
        System.out.println(order);
        waybillRepository.save(order.getWaybill());
        orderRepository.save(order);
        return order;
    }*/

    @GetMapping(value = "/clients/findClientsByNameLike")
    public List<Client> findClientsByNameLike(@RequestParam String name){
        return clientRepository.findClientsByNameLikeIgnoreCase(String.format("%%%s%%", name));
    }

    @GetMapping(value = "/companies/findCompaniesByNameLike")
    public List<Company> findCompaniesByNameLikeRest(@RequestParam String name){
        name = String.format("%%%s%%", name);
        return companyRepository.findTop10CompaniesByNameLikeIgnoreCase(name);
    }

    @GetMapping(value = "/companies/{companyId}/stocks")
    public List<Stock> findStocksByCompany(@PathVariable Long companyId){
        Company companyById = companyRepository.findCompanyById(companyId);
        return stockRepository.findStocksByCompany(companyById);
    }

    @GetMapping(value = "/companies/stocks/findStocksByAddressLike")
    public List<Stock> findStocksByNameLike(@RequestParam String address){
        address = String.format("%%%s%%", address);
        return stockRepository.findStocksByAddressLike(address);
    }

    /*Method use old dto !*/
/*    @PostMapping(value = "/companies/orders/edit")
    public Object editOrder(OrderDto orderDto, Long orderId, Long waybillId, HttpServletRequest request) throws ParseException {
        Order orderFromDto = orderService.getOrderFromDto(orderDto);
        orderFromDto.setId(orderId);
        System.out.println(orderFromDto);
        Waybill waybill = orderFromDto.getWaybill();
        waybill.setId(waybillId);
        waybillRepository.save(waybill);
        return orderRepository.save(orderFromDto);
    }*/

    @GetMapping(value = "/companies/findStocksByUsername")
    public Object findCompanyByUsername() throws JSONException {
        try {
            System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
            Company company = userRepository.findUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getCompany();
            return stockRepository.findStocksByCompany(company);
        }catch (NullPointerException e){
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put("error", "No stocks");
            return json.toString();
        }
    }

    @GetMapping(value = "/testUser")
    public Object getUserDetails() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", SecurityContextHolder.getContext().getAuthentication().getName());
        json.put("details", SecurityContextHolder.getContext().getAuthentication().getDetails());
        json.put("principal", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        json.put("credentials", SecurityContextHolder.getContext().getAuthentication().getCredentials());
        json.put("authorities", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        return json.toString();
    }

    @PostMapping(value = "/orders/createConsignment")
    public Object createConsignment(Long orderId, @RequestParam(value = "consignments")String consignments) throws JSONException {
        JSONObject json = new JSONObject();
        String[] split = consignments.split("`");
        Order orderById = orderRepository.findOrderById(orderId);
        if (orderById==null){
            json.put("error", "no such order");
        }else{
            List<Consignment> consignmentList = new LinkedList<>();
            for (String s : split) {
                consignmentList.add(consignmentRepository.save(new Consignment(s, orderById)));
            }
            json.put("status", "saved");
            json.put("consignments", consignmentList);
        }
        return json.toString();
    }

}

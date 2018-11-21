package com.itechart.trucking.webmodule.controller;

import com.itechart.trucking.cancellationAct.entity.CancellationAct;
import com.itechart.trucking.cancellationAct.repository.CancellationActRepository;
import com.itechart.trucking.company.entity.Company;
import com.itechart.trucking.consignment.entity.Consignment;
import com.itechart.trucking.consignment.repository.ConsignmentRepository;
import com.itechart.trucking.order.entity.Order;
import com.itechart.trucking.order.repository.OrderRepository;
import com.itechart.trucking.product.entity.Product;
import com.itechart.trucking.product.entity.ProductState;
import com.itechart.trucking.product.repository.ProductRepository;
import com.itechart.trucking.routeList.entity.RouteList;
import com.itechart.trucking.routeList.repository.RouteListRepository;
import com.itechart.trucking.waybill.entity.Waybill;
import com.itechart.trucking.waybill.repository.WaybillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.itechart.trucking.formData.WaybillFormData;
import com.itechart.trucking.odt.Odt;
import com.itechart.trucking.order.dto.OrderDto;
import com.itechart.trucking.order.entity.Order;
import com.itechart.trucking.order.repository.OrderRepository;
import com.itechart.trucking.product.dto.ProductDto;
import com.itechart.trucking.product.entity.Product;
import com.itechart.trucking.product.entity.ProductState;
import com.itechart.trucking.product.repository.ProductRepository;
import com.itechart.trucking.routeList.dto.RouteListDto;
import com.itechart.trucking.routeList.entity.RouteList;
import com.itechart.trucking.routeList.repository.RouteListRepository;
import com.itechart.trucking.user.entity.User;
import com.itechart.trucking.user.repository.UserRepository;
import com.itechart.trucking.waybill.dto.WaybillDto;
import com.itechart.trucking.waybill.entity.Waybill;
import com.itechart.trucking.waybill.repository.WaybillRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@PreAuthorize("hasAuthority('ROLE_MANAGER')")
@CrossOrigin
@RestController
@RequestMapping(value = "/api")
public class ManagerController {


    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ConsignmentRepository consignmentRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RouteListRepository routeListRepository;
    @Autowired
    private CancellationActRepository cancellationActRepository;
    @Autowired
    private WaybillRepository waybillRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "/manager/orders")//todo findByStatus change on number value(Murat, please)
    public Object findActiveOrders(@RequestParam(value = "page") int pageId) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User userByUsername = userRepository.findUserByUsername(name);

        Page<Order> orderPage = orderRepository.findAllByStatusAndCompanyId(1,userByUsername.getCompany().getId(), PageRequest.of(pageId-1, 5));
        return orderPage.map(order -> new OrderDto(order));
    }

    @GetMapping(value = "/manager/orders/{id}")
    public Order findOrderById(@PathVariable Long id) {
        System.out.println(orderRepository.findOrderById(id));
        return orderRepository.findOrderById(id);
    }

    @GetMapping(value = "/manager/products/{id}")
    public Object findProductsByOrderId(@PathVariable Long id,@RequestParam(value = "page") int pageId) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User userByEmail = userRepository.findUserByUsername(name);

        Optional<Order> order = orderRepository.findById(id);
        if(!order.isPresent() || order.get().getCompany().getId()!=userByEmail.getCompany().getId()) return null;

        Page<Product> productPage = productRepository.findAllByConsignment(consignmentRepository.findConsignmentByOrder(order.get()),PageRequest.of(pageId-1, 5));

        return productPage.map(product -> new ProductDto(product));
    }

    @GetMapping(value = "/manager/routeList/{id}")
    public List<RouteListDto> getRouteList(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);
        return order.map(order1 -> Odt.RouteListToDtoList(order1.getWaybill().getRouteListList())).orElse(null);
    }

    @PostMapping(value = "/manager/updateProductStatus/{id}")
    public Object changeStatus(@PathVariable Long id, @RequestBody String status) throws JSONException {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            product.get().setStatus(Integer.valueOf(status));
            Product save = productRepository.save(product.get());
            return new ProductDto(save);
        } else {
            JSONObject json = new JSONObject();
            json.put("error", "invalid data");
            return json;
        }
    }

    @DeleteMapping(value = "/manager/deletePoint/{id}")
    public boolean deletePoint(@PathVariable Long id) {
        try {
            routeListRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping(value = "/manager/{orderId}/createPoint")
    public boolean createPoint(@PathVariable Long orderId, @RequestBody RouteList routeList) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (!order.isPresent())
            return false;
        routeList.setWaybill(order.get().getWaybill());
        return routeListRepository.save(routeList) != null;

    }

    @GetMapping(value = "/manager/{productId}/cancelProduct/{orderId}")
    public ProductDto cancelProduct(@PathVariable Long productId, @PathVariable Long orderId, @RequestParam("cancel") int cancel) {
        Consignment consignment = orderRepository.findOrderById(orderId).getConsignment();
        CancellationAct cancellationAct = consignment.getCancellationAct();
        if(cancellationAct == null) {
            cancellationAct = new CancellationAct(new Date((new java.util.Date().getTime())), 0, new Double(0), consignment);
            cancellationActRepository.save(cancellationAct);
        }

        Optional<Product> product = productRepository.findById(productId);
        if (!product.isPresent())
            return null;

        if (cancel > 0) {
            product.get().setCancelledCount(product.get().getCancelledCount() + cancel);
            product.get().setCount(product.get().getCount() - cancel);

            if (product.get().getCount() == 0) {
                product.get().setStatus(4);
            } else {
                product.get().setStatus(5);
            }

            cancellationAct.setPrice(product.get().getPrice() * cancel + cancellationAct.getPrice());
            Integer amount = cancellationAct.getAmount() + cancel;
            cancellationAct.setAmount(amount);
            product.get().setCancellationAct(cancellationAct);
        } /*else {
            product.get().setStatus(1);
            cancellationAct.setPrice(cancellationAct.getPrice() - product.get().getPrice());
            Integer amount = cancellationAct.getAmount() - 1;
            cancellationAct.setAmount(amount);
            product.get().setCancellationAct(null);
        }*/



        productRepository.save(product.get());
        cancellationActRepository.save(cancellationAct);

        return new ProductDto(product.get());
    }

    @GetMapping(value = "/manager/{productId}/restoreProduct/{orderId}")
    public ProductDto restoreProduct(@PathVariable Long productId, @PathVariable Long orderId, @RequestParam("restore") int restore) {
        Consignment consignment = orderRepository.findOrderById(orderId).getConsignment();
        CancellationAct cancellationAct = consignment.getCancellationAct();
        if(cancellationAct == null) {
            cancellationAct = new CancellationAct(new Date((new java.util.Date().getTime())), 0, new Double(0), consignment);
            cancellationActRepository.save(cancellationAct);
        }

        Optional<Product> product = productRepository.findById(productId);
        if (!product.isPresent())
            return null;

        if (restore > 0) {
            product.get().setCancelledCount(product.get().getCancelledCount() - restore);
            product.get().setCount(product.get().getCount() + restore);

            if (product.get().getCancelledCount() == 0) {
                product.get().setStatus(1);
            } else {
                product.get().setStatus(5);
            }

            cancellationAct.setPrice(cancellationAct.getPrice() - product.get().getPrice() * restore);
            Integer amount = cancellationAct.getAmount() - restore;
            cancellationAct.setAmount(amount);
            product.get().setCancellationAct(cancellationAct);
        } /*else {
            product.get().setStatus(1);
            cancellationAct.setPrice(cancellationAct.getPrice() - product.get().getPrice());
            Integer amount = cancellationAct.getAmount() - 1;
            cancellationAct.setAmount(amount);
            product.get().setCancellationAct(null);
        }*/



        productRepository.save(product.get());
        cancellationActRepository.save(cancellationAct);

        return new ProductDto(product.get());
    }

    @GetMapping(value = "/manager/finishChecking/{orderId}")
    public OrderDto finishChecking(@PathVariable Long orderId) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User userByUsername = userRepository.findUserByUsername(name);

        Optional<Order> order = orderRepository.findById(orderId);
        Waybill waybill = order.get().getWaybill();
        waybill.setStatus(2);
        waybill.setCheckDate(new Date((new java.util.Date()).getTime()));
        waybill.setUser(userByUsername);

        CancellationAct cancellationAct = order.get().getConsignment().getCancellationAct();
        if(cancellationAct != null) {
            cancellationAct.setDate(new Date((new java.util.Date()).getTime()));
            cancellationActRepository.save(cancellationAct);
        }
        waybillRepository.save(waybill);
        order.get().setWaybill(waybill);

        return new OrderDto(order.get());
    }

    @GetMapping(value = "/manager/cancelChecking/{orderId}")
    public OrderDto cancelWaybillCheck(@PathVariable Long orderId, @RequestParam("status") String status) {
        System.out.println(orderId);
        System.out.println(status);
        Optional<Order> order = orderRepository.findById(orderId);
        System.out.println(order.isPresent());
        Waybill waybill = order.get().getWaybill();
        waybill.setStatus(Integer.valueOf(status));
        waybill.setCheckDate(null);
        waybill.setUser(null);

        CancellationAct cancellationAct = order.get().getConsignment().getCancellationAct();
        if(cancellationAct != null) {
            cancellationAct.setDate(null);
            cancellationActRepository.save(cancellationAct);
        }
        waybillRepository.save(waybill);
        order.get().setWaybill(waybill);

        return new OrderDto(order.get());
    }

    @PostMapping(value = "/manager/createRouteList")
    public Object createRouteList(RouteList routeListDto, WaybillFormData waybill) {
        //todo CREATE FORM SAVING IN DB
        return null;//!!!
    }

}

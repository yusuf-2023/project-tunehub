package com.kodnest.tunehub.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kodnest.tunehub.entity.User;
import com.kodnest.tunehub.service.UserService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentController {
    @Autowired
    UserService userService;

    @GetMapping("/pay")
    public String pay() {
        return "pay";
    }

    @PostMapping("/createOrder")
    @ResponseBody
    public String createOrder(HttpSession session) {
        int amount = 1;
        Order order = null;
        try {
            RazorpayClient razorpay = new RazorpayClient("rzp_test_zbmsDeii4nZP2i", "uBxQzxv7LMfBK8Rjkm0lCVXS");

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount * 100); // amount in the smallest currency unit
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcptid_11");

            // Create the order
            order = razorpay.orders.create(orderRequest);
        } catch (RazorpayException e) {
            e.printStackTrace();
            // Handle the exception (e.g., log the error, return an error message)
            return "Failed to create order";
        }

        // Check if the order was successfully created
        if (order != null) {
            // Return the order details as a JSON string
            return order.toString();
        } else {
            // Handle the case where order is null (e.g., return an error message)
            return "Failed to create order";
        }
    }

    @PostMapping("/verify")
    @ResponseBody
    public boolean verifyPayment(@RequestParam String orderId, @RequestParam String paymentId,
                                 @RequestParam String signature) {
        try {
            // Initialize Razorpay client with your API key and secret
            RazorpayClient razorpayClient = new RazorpayClient("rzp_test_zbmsDeii4nZP2i", "uBxQzxv7LMfBK8Rjkm0lCVXS");
            // Create a signature verification data string
            String verificationData = orderId + "|" + paymentId;

            // Use Razorpay's utility function to verify the signature
            return Utils.verifySignature(verificationData, signature, "uBxQzxv7LMfBK8Rjkm0lCVXS");
        } catch (RazorpayException e) {
            e.printStackTrace();
            // Handle the exception (e.g., log the error, return false)
            return false;
        }
    }

    @GetMapping("/Payment-success")
    public String paymentSuccess(HttpSession session) {
        String mail = (String) session.getAttribute("email");
        User user = userService.getUser(mail);
        user.setispremium(true);
        userService.updateUser(user);
        return "customerhome";
    }

    @GetMapping("/Payment-failure")
    public String paymentFailure() {
        return "customerhome";
    }
}

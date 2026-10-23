package org.moustapha.customerservice.service;

import org.moustapha.customerservice.entities.Customer;
import org.moustapha.customerservice.repository.CustomerRepository;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @McpTool(description = "Get all cusstomer")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @McpTool(description = "find customer by id")
    public Customer findCustomerById(@McpToolParam( description = "the customer id") Long id){
        return customerRepository.findById(id).orElseThrow(()->new RuntimeException("Customer not found"));
    }

    @McpTool(description = "save a new customer")
    public Customer saveCustomer(@McpToolParam(description = "the customer to save(name, email)") Customer customer) {
        return customerRepository.save(customer);
    }
}

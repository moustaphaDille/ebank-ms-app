package org.moustapha.ebankservice.service;

import org.moustapha.ebankservice.entities.BankAccount;
import org.moustapha.ebankservice.feign.CustomerRestClient;
import org.moustapha.ebankservice.model.Customer;
import org.moustapha.ebankservice.repository.BankAccountRepository;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class EbankService {
    private BankAccountRepository bankAccountRepository;
    private CustomerRestClient customerRestClient;

    public EbankService(BankAccountRepository bankAccountRepository, CustomerRestClient customerRestClient) {
        this.bankAccountRepository = bankAccountRepository;
        this.customerRestClient = customerRestClient;
    }


    @McpTool(description = "Get all bank account")
    public List<BankAccount> getAllBankAccount(){
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        bankAccounts.forEach(bankAccount ->
                bankAccount.setCustomer(customerRestClient.getCustomerById(bankAccount.getCustomerId()))
                );
        return bankAccounts;
    }

    @McpTool(description = "Get bank account by id")
    public  BankAccount getBankAccountById(@McpToolParam(description = "the bank account id") String id){
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Account not found"));
        bankAccount.setCustomer(
                customerRestClient.getCustomerById(bankAccount.getCustomerId()));
        return bankAccount;
    }

    @McpTool(description = "save a new bank account")
    public BankAccount saveBankAccount(@McpToolParam(description = "the bank account to save(balance, type, customerId)") BankAccount bankAccount){
        try {
            Customer customer = customerRestClient.getCustomerById(bankAccount.getCustomerId());
            bankAccount.setId(UUID.randomUUID().toString());
            bankAccount.setCreatedAt(new Date());
            return bankAccountRepository.save(bankAccount);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }
}

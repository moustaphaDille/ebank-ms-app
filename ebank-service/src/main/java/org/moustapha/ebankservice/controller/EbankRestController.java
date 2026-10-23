package org.moustapha.ebankservice.controller;
import org.moustapha.ebankservice.entities.BankAccount;
import org.moustapha.ebankservice.service.EbankService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EbankRestController {
    private EbankService ebankService;

    public EbankRestController(EbankService ebankService) {
        this.ebankService = ebankService;
    }

    @GetMapping("/accounts")
    public List<BankAccount> getAllBankAccount(){
        return ebankService.getAllBankAccount();
    }

    @GetMapping("/accounts/{id}")
    public  BankAccount getBankAccountById(@PathVariable String id){
        return ebankService.getBankAccountById(id);
    }

    @PostMapping("/accounts")
    public BankAccount saveBankAccount(@RequestBody BankAccount bankAccount){
        return ebankService.saveBankAccount(bankAccount);
    }
}

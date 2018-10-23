package com.kiss.account.client;


import com.kiss.account.input.CreateClientInput;
import com.kiss.account.input.UpdateClientInput;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import output.ResultOutput;

@RequestMapping
public interface ClientClient {

    @GetMapping("/clients")
    ResultOutput getClients();

    @GetMapping("/client")
    ResultOutput getClient (@RequestParam("clientID") String clientID);

    @PostMapping("/client")
    ResultOutput createClient (@Validated @RequestBody CreateClientInput clientInput);

    @PutMapping("/client")
    ResultOutput updateClient (@Validated @RequestBody UpdateClientInput updateClientInput);

    @DeleteMapping("/client")
    ResultOutput deleteClient (@RequestParam("clientID") String clientID);
}

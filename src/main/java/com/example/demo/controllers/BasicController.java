package com.example.demo.controllers;

import org.apache.logging.log4j.message.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BasicController {

    @GetMapping("/api/hello")
    public Map<String, String> sayHello(){
        return Map.of("message", "Welcome to the VM HAHAHAHA");
    }

}

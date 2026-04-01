package com.example.demo;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "working";
    }
}

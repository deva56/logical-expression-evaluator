package com.leapwise.logicalexpressionevaluator.api_rest.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/")
    public String getLandingPage() {
        return "Logical evaluator app is live.";
    }

}

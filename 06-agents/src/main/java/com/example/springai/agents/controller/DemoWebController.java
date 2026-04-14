package com.example.springai.agents.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoWebController {

    @GetMapping("/")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/patterns/chain")
    public String chain() {
        return "patterns/chain";
    }

    @GetMapping("/patterns/parallelization")
    public String parallelization() {
        return "patterns/parallelization";
    }

    @GetMapping("/patterns/routing")
    public String routing() {
        return "patterns/routing";
    }

    @GetMapping("/patterns/orchestrator")
    public String orchestrator() {
        return "patterns/orchestrator";
    }

    @GetMapping("/patterns/evaluator")
    public String evaluator() {
        return "patterns/evaluator";
    }
}

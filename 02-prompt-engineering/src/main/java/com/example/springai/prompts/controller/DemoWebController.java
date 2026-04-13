package com.example.springai.prompts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoWebController {

    @GetMapping("/")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/patterns/focused")
    public String focused() {
        return "patterns/focused";
    }

    @GetMapping("/patterns/autonomous")
    public String autonomous() {
        return "patterns/autonomous";
    }

    @GetMapping("/patterns/task")
    public String task() {
        return "patterns/task";
    }

    @GetMapping("/patterns/code")
    public String code() {
        return "patterns/code";
    }

    @GetMapping("/patterns/analyze")
    public String analyze() {
        return "patterns/analyze";
    }

    @GetMapping("/patterns/chat")
    public String chat() {
        return "patterns/chat";
    }

    @GetMapping("/patterns/reason")
    public String reason() {
        return "patterns/reason";
    }

    @GetMapping("/patterns/constrained")
    public String constrained() {
        return "patterns/constrained";
    }
}

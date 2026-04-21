package com.example.springai.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller for serving Thymeleaf views.
 * Uses client-side JavaScript to call REST APIs.
 */
@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/simple-chat")
    public String simpleChat() {
        return "simple-chat";
    }

    @GetMapping("/conversation-chat")
    public String conversationChat() {
        return "conversation-chat";
    }
}

package com.example.springai.tools.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller to serve the frontend UI.
 */
@Controller
public class WebController {

    /**
     * Serve the main page.
     *
     * @return template name
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}

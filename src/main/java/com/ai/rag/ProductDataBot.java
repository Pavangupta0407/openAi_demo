package com.ai.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.services.OpenAiService;

@Controller
public class ProductDataBot {

	@Autowired
	private OpenAiService service;

	@GetMapping("/showProductDataBot")
	public String showProductDataBot() {
		return "productDataBot";

	}

	@PostMapping("/productDataBot")
	public String productDataBot(@RequestParam String query, Model model) {
		model.addAttribute("response",service.productData(query));
		return "productDataBot";

	}

}
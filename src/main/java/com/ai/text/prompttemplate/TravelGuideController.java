package com.ai.text.prompttemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.services.OpenAiService;

@Controller
public class TravelGuideController {
	@Autowired
	private OpenAiService chatService;

	@GetMapping("/showTravelGuide")
	public String showChatPage() {
		return "travelGuide";
	}

	@PostMapping("/travelGuide")
	public String getChatResponse(@RequestParam("city") String city, @RequestParam("month") String month,
			@RequestParam("language") String language, @RequestParam("budget") String budget, Model model) {
		String result = chatService.getTravelGuidance(city,month,language,budget);
		model.addAttribute("city", city);
		model.addAttribute("response", result);
		return "travelGuide";
	}

}

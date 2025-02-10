package com.ai.text;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.ai.services.OpenAiService;

@RestController
public class AnswerAnyThingStreamingController {

	@Autowired
	OpenAiService service;

}
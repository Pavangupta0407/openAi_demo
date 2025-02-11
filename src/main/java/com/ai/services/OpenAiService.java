package com.ai.services;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import com.ai.text.prompttemplate.dto.CountryCuisines;

@Service
public class OpenAiService {

	private ChatClient chatClient;
	
	public OpenAiService(ChatClient.Builder builder) {
		//Advisor is used to store chat history using in memory space
		chatClient = builder.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory())).build();
	}
	
	public ChatResponse generateAnswer(String question) {
		//To configure chatoptions in code ,same can be done in app.prop file as well
//		OpenAiChatOptions chatOptions = new OpenAiChatOptions();
//		chatOptions.setModel("gpt-4o-mini");
//		chatOptions.setTemperature(0.7);
//		chatOptions.setMaxTokens(20);
//		return chatClient.prompt(new Prompt(question, chatOptions)).call().chatResponse();
		return chatClient.prompt(question).call().chatResponse();
	}

	public String getTravelGuidance(String city, String month, String language, String budget) {
		PromptTemplate promptTemplate = new PromptTemplate("Welcome to the {city} travel guide!\n"
				+ " If you're visiting in {month}, here's what you can do:\n"
				+ " 1. Must-visit attractions.\n"
				+ " 2. Local cuisine you must try.\n"
				+ " 3. Useful phrases in {language}.\n"
				+ " 4. Tips for traveling on a {budget} budget.\n"
				+ " Enjoy your trip!");
		Prompt prompt = promptTemplate.create(Map.of("city",city,"month",month,"language",language,"budget",budget));
		return chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getContent();
	}

	public CountryCuisines getCuisines(String country, String numCuisines, String language) {
		PromptTemplate promptTemplate = new PromptTemplate("You are an expert in traditional cuisines.\n"
				+ "You provide information about a specific dish from a specific country.\n"
				+ "Answer the question: What is the traditional cuisine of {country}?\n"
				+ "Avoid giving information about fictional places. If the country is fictional or non-existent answer: I don't know."
				+ "Return a list of {numCuisines} in {language}.");
		Prompt prompt = promptTemplate.create(Map.of("country",country,"numCuisines",numCuisines,"language",language));
		return chatClient.prompt(prompt).call().entity(CountryCuisines.class);
	}

}

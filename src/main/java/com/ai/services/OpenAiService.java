package com.ai.services;

import java.util.List;
import java.util.Map;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.moderation.Moderation;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.ai.openai.api.OpenAiAudioApi.TranscriptResponseFormat;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import com.ai.text.prompttemplate.dto.countryCuisines;

import reactor.core.publisher.Flux;

@Service
public class OpenAiService {

	private ChatClient chatClient;

	@Autowired
	private EmbeddingModel embeddingModel;

//	@Autowired
//	private VectorStore vectorStore;

	@Autowired
	private OpenAiImageModel openAiImageModel;
	
	@Autowired
	private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

	@Autowired
	private OpenAiAudioSpeechModel openAiAudioSpeechModel;
	
	@Autowired
	private OpenAiChatModel openAiChatModel;
	
	@Autowired
	private OpenAiModerationModel moderationModel;
	
	public OpenAiService(ChatClient.Builder builder) {
		// Advisor is used to store chat history using in memory space
		chatClient = builder.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory())).build();
	}

	public ChatResponse generateAnswer(String question) {
		// To configure chatoptions in code ,same can be done in app.prop file as well
//		OpenAiChatOptions chatOptions = new OpenAiChatOptions();
//		chatOptions.setModel("gpt-4o-mini");
//		chatOptions.setTemperature(0.7);
//		chatOptions.setMaxTokens(20);
//		return chatClient.prompt(new Prompt(question, chatOptions)).call().chatResponse();
		return chatClient.prompt(question).call().chatResponse();
	}
	
	public Flux<String> streamAnswer(String message) {
		return chatClient.prompt(message).stream().content();
	}

	public ChatResponse generateAnswerWithSystem(String question) {

		return chatClient.prompt().system("You are a helpful assistant that can answer any question.").user(question)
				.call().chatResponse();
	}

	public String getTravelGuidance(String city, String month, String language, String budget) {
		PromptTemplate promptTemplate = new PromptTemplate("Welcome to the {city} travel guide!\n"
				+ " If you're visiting in {month}, here's what you can do:\n" + " 1. Must-visit attractions.\n"
				+ " 2. Local cuisine you must try.\n" + " 3. Useful phrases in {language}.\n"
				+ " 4. Tips for traveling on a {budget} budget.\n" + " Enjoy your trip!");
		Prompt prompt = promptTemplate
				.create(Map.of("city", city, "month", month, "language", language, "budget", budget));
		return chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getContent();
	}

	public countryCuisines getCuisines(String country, String numCuisines, String language) {
		PromptTemplate promptTemplate = new PromptTemplate("You are an expert in traditional cuisines.\n"
				+ "You provide information about a specific dish from a specific country.\n"
				+ "Answer the question: What is the traditional cuisine of {country}?\n"
				+ "Avoid giving information about fictional places. If the country is fictional or non-existent answer: I don't know."
				+ "Return a list of {numCuisines} in {language}.");
		Prompt prompt = promptTemplate
				.create(Map.of("country", country, "numCuisines", numCuisines, "language", language));
		return chatClient.prompt(prompt).call().entity(countryCuisines.class);
	}

	public float[] embed(String text) {
		float[] response = embeddingModel.embed(text);
		return response;
	}

	public double findSimilarity(String text1, String text2) {
		List<float[]> response = embeddingModel.embed(List.of(text1, text2));
		return cosineSimilarity(response.get(0), response.get(1));
	}

	private double cosineSimilarity(float[] vectorA, float[] vectorB) {
		if (vectorA.length != vectorB.length) {
			throw new IllegalArgumentException("Vectors must be of the same length");
		}

		// Initialize variables for dot product and magnitudes
		double dotProduct = 0.0;
		double magnitudeA = 0.0;
		double magnitudeB = 0.0;

		// Calculate dot product and magnitudes
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			magnitudeA += vectorA[i] * vectorA[i];
			magnitudeB += vectorB[i] * vectorB[i];
		}

		// Calculate and return cosine similarity
		return dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
	}

	public List<Document> searchJobs(String query) {
		// return vectorStore.similaritySearch(query);
		return null;
	}

	// Uses RAG revelant augmented generation
	public String productData(String query) {
		// return chatClient.prompt(query).advisors(new
		// QuestionAnswerAdvisor(vectorStore)).call().content();
		return null;

	}

	public String generateImage(String prompt) {
		ImageResponse imageResponse = openAiImageModel.call(new ImagePrompt(prompt,
				OpenAiImageOptions.builder().withQuality("hd").withHeight(1024).withWidth(1024).withN(1).build()));
		return imageResponse.getResult().getOutput().getUrl();
	}

	public String explainImage(String prompt, String path) {
		String response = chatClient.prompt()
				.user(u -> u.text(prompt).media(MimeTypeUtils.IMAGE_JPEG, new FileSystemResource(path))).call()
				.content();
		return response;
	}

	public String explainDiet(String prompt, String path1, String path2) {
		String response = chatClient.prompt()
				.user(u -> u.text(prompt)
						.media(MimeTypeUtils.IMAGE_JPEG, new FileSystemResource(path1))
						.media(MimeTypeUtils.IMAGE_JPEG, new FileSystemResource(path2))).call()
				.content();
		return response;
	}

	@SuppressWarnings("removal")
	public String speechToText(String path) {
		OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder().withLanguage("fr").withResponseFormat(TranscriptResponseFormat.VTT).build();
		AudioTranscriptionPrompt audioTranscriptionPrompt = new AudioTranscriptionPrompt(new FileSystemResource(path),options);
		return openAiAudioTranscriptionModel.call(audioTranscriptionPrompt).getResult().getOutput();
	}
	
	public byte[] textToSpeech(String text) {
		return openAiAudioSpeechModel.call(text);
	}
	
	@SuppressWarnings("removal")
	public String getStockPrice(String company) {
		Prompt prompt = new Prompt("Get stock symbol and stock price"+company,OpenAiChatOptions.builder().
				withFunction("stockRetrievalFunction").build());
		return openAiChatModel.call(prompt).getResult().getOutput().getContent();
	}
	
	public ModerationResult moderate(String text) {
		Moderation moderation = moderationModel.call(new ModerationPrompt(text)).getResult().getOutput();
		return moderation.getResults().get(0);
	}
}

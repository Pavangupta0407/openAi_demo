package com.ai.functioncalling;

import java.util.function.Function;

import com.ai.functioncalling.StockRetrievalService.Request;
import com.ai.functioncalling.StockRetrievalService.Response;

public class StockRetrievalService implements Function<Request, Response> {

	public record Request(String symbol) {
	}

	public record Response(Double price) {
	}

	@Override
	public Response apply(Request t) {
		//Here its a dummy call instead we can make a call to any finance api
		return new Response(5000D);
	}

}
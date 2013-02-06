package com.fineshambles.stormplay.resources;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.fineshambles.stormplay.DefinitionsRepository;

@Path("/words/{word}")
public class DefinitionsResource {
	@GET
	@Produces("text/plain")
	public String getDefinitions(@PathParam("word") String word) {
		try {
			List<String> definitions = DefinitionsRepository.get(word);
			StringBuilder sb = new StringBuilder();
			for (String definition : definitions) {
				sb.append(definition);
				sb.append("\n");
			}
			return sb.toString();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			return "Riak fail";
		}
	}
	
}

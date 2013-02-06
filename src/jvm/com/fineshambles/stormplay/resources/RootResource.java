package com.fineshambles.stormplay.resources;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.fineshambles.stormplay.DefinitionsRepository;

@Path("/")
public class RootResource {
	@GET
	@Produces("text/html")
	public String getStuff() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head></head><body>");
		sb.append("<ul>");
		for (String word : DefinitionsRepository.getWords()) {
			sb.append("<li><a href=\"/words/" + word + "\">" + word + "</a></li>");
		}
		sb.append("</ul>");
		sb.append("</body></html>");
		return sb.toString();
	}
	
}

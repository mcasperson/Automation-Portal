package com.redhat.automationportal.rest;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;


import com.redhat.automationportal.scripts.BugzillaReportGenerator;
import com.redhat.automationportal.scripts.ParseToc;
import com.redhat.automationportal.scripts.RegenSplash;

@Path("/")
public class AutomationPortalREST
{
	@GET
	@Consumes("text/plain")
	@Produces("application/json")
	@Path("/BugzillaReportGenerator/get/json")
	public Response BugzillaReportGeneratorGetJson(
			@QueryParam("bugzillaUsername") final String bugzillaUsername, 
			@QueryParam("bugzillaPassword") final String bugzillaPassword,
			@HeaderParam("Referer") final String refererHeader,
			@HeaderParam("Origin") final String originHeader)
	{
		final Logger logger = Logger.getLogger("com.redhat.automationportal");

		try
		{
			logger.info("-> AutomationPortalREST.BugzillaReportGeneratorGetJson()");
			
			final BugzillaReportGenerator script = new BugzillaReportGenerator();
			script.setBugzillaPassword(bugzillaPassword);
			script.setBugzillaUsername(bugzillaUsername);
			final boolean result = script.run();

			final String message = script.getMessage();			
			final String output = script.getOutput();
			
			logger.info("AutomationPortalREST.BugzillaReportGeneratorGetJson() message: " + message);
			logger.info("AutomationPortalREST.BugzillaReportGeneratorGetJson() output: " + output);
		
			return Response.status(result ? 200 : 500)
					/* CORS header allowing cross-site requests */
					.header("Access-Control-Allow-Origin", originHeader)
					.entity(new AutomationPortalResponseData(message, output))
					.build();
		}
		finally
		{
			logger.info("<- AutomationPortalREST.BugzillaReportGeneratorGetJson()");
		}

	}
	
	@GET
	@Consumes("text/plain")
	@Produces("application/json")
	@Path("/ParseTOC/get/json")
	public Response ParseTOCGetJson(
			@HeaderParam("Referer") final String refererHeader,
			@HeaderParam("Origin") final String originHeader)
	{
		final Logger logger = Logger.getLogger("com.redhat.automationportal");

		try
		{
			logger.info("-> AutomationPortalREST.ParseTOCGetJson()");
			
			final ParseToc script = new ParseToc();
			final boolean result = script.run();

			final String message = script.getMessage();			
			final String output = script.getOutput();
			
			logger.info("AutomationPortalREST.ParseTOCGetJson() message: " + message);
			logger.info("AutomationPortalREST.ParseTOCGetJson() output: " + output);
		
			return Response.status(result ? 200 : 500)
					/* CORS header allowing cross-site requests */
					.header("Access-Control-Allow-Origin", originHeader)
					.entity(new AutomationPortalResponseData(message, output))
					.build();
		}
		finally
		{
			logger.info("<- AutomationPortalREST.ParseTOCGetJson()");
		}

	}
	
	@GET
	@Consumes("text/plain")
	@Produces("application/json")
	@Path("/ParseTOC/get/json")
	public Response RegenSplashGetJson(
			@QueryParam("tocUrl") final String tocUrl, 
			@QueryParam("productName") final String productName,
			@QueryParam("username") final String username, 
			@QueryParam("password") final String password,
			@HeaderParam("Referer") final String refererHeader,
			@HeaderParam("Origin") final String originHeader)
	{
		final Logger logger = Logger.getLogger("com.redhat.automationportal");

		try
		{
			logger.info("-> AutomationPortalREST.RegenSplashGetJson()");
			
			final RegenSplash script = new RegenSplash();
			script.setUsername(username);
			script.setPassword(password);
			script.setProduct(productName);
			script.setSelectedSite(tocUrl);
			
			final boolean result = script.run();

			final String message = script.getMessage();			
			final String output = script.getOutput();
			
			logger.info("AutomationPortalREST.ParseTOCGetJson() message: " + message);
			logger.info("AutomationPortalREST.ParseTOCGetJson() output: " + output);
		
			return Response.status(result ? 200 : 500)
					/* CORS header allowing cross-site requests */
					.header("Access-Control-Allow-Origin", originHeader)
					.entity(new AutomationPortalResponseData(message, output))
					.build();
		}
		finally
		{
			logger.info("<- AutomationPortalREST.RegenSplashGetJson()");
		}

	}

}

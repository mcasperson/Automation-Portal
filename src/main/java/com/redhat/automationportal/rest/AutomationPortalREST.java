package com.redhat.automationportal.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.redhat.automationportal.scripts.BugzillaReportGenerator;

@Path("/")
public class AutomationPortalREST
{
	@GET
	@Path("/BugzillaReportGenerator/get/json")
	public Response BugzillaReportGeneratorGetJson(@QueryParam("bugzillaUsername") final String bugzillaUsername, @QueryParam("bugzillaPassword") final String bugzillaPassword)
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
			
			if (result)			
				return Response.status(200).entity(message).build();
			else
				return Response.status(500).entity(message).build();
		}
		finally
		{
			logger.info("<- AutomationPortalREST.BugzillaReportGeneratorGetJson()");
		}

	}
}

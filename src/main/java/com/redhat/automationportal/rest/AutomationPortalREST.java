package com.redhat.automationportal.rest;

import java.util.Calendar;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/")
public class AutomationPortalREST
{
	@GET
	@Path("/BugzillaReportGenerator/get/json")
	public Response printMessage(@QueryParam("bugzillaUsername") final String bugzillaUsername, @QueryParam("bugzillaPassword") final String bugzillaPassword) 
	{
		final String result = Calendar.getInstance().getTimeInMillis() + ""; 
		return Response.status(200).entity(result).build();

	}
}

package com.redhat.automationportal.rest;

import java.util.Calendar;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/")
public class AutomationPortalREST
{
	@GET
	@Path("/BugzillaReportGenerator/get/json")
	public Response printMessage(@PathParam("bugzillaUsername") final String bugzillaUsername, @PathParam("bugzillaPassword") final String bugzillaPassword) 
	{
		final String result = Calendar.getInstance().getTimeInMillis() + ""; 
		return Response.status(200).entity(result).build();

	}
}

package com.redhat.automationportal.rest;

public class AutomationPortalResponseData
{
	private String message;
	private String output;

	public String getMessage()
	{
		return message;
	}

	public void setMessage(final String message)
	{
		this.message = message;
	}

	public String getOutput()
	{
		return output;
	}

	public void setOutput(final String output)
	{
		this.output = output;
	}
	
	public AutomationPortalResponseData()
	{
		
	}
	
	public AutomationPortalResponseData(final String message, final String output)
	{
		this.message = message;
		this.output = output;
	}
}

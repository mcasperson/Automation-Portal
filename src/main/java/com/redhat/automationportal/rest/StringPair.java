package com.redhat.automationportal.rest;

/**
 * A class to hold two strings, usually as a name/value pair for combo boxes or
 * other similar UI elements.
 */
public class StringPair
{
	private String firstString;
	private String secondString;

	public String getFirstString()
	{
		return firstString;
	}

	public void setFirstString(final String firstString)
	{
		this.firstString = firstString;
	}

	public String getSecondString()
	{
		return secondString;
	}

	public void setSecondString(final String secondString)
	{
		this.secondString = secondString;
	}
	
	public StringPair()
	{
		
	}
	
	public StringPair(final String firstString, final String secondString)
	{
		this.firstString = firstString;
		this.secondString = secondString;
	}

}

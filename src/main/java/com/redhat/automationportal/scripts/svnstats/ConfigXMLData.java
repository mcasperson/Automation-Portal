package com.redhat.automationportal.scripts.svnstats;

import java.util.Date;

public class ConfigXMLData
{
	private Date fromDate;
	private String path;
	private String entry;
	
	public ConfigXMLData()
	{
		this.fromDate = null;
		this.path = "";
		this.entry = "";
	}
	
	public ConfigXMLData(final ConfigXMLData source)
	{
		this.fromDate = source.fromDate;
		this.path = source.path;
		this.entry = source.entry;
	}
	
	public ConfigXMLData(final Date fromDate, final String path, final String entry)
	{
		this.fromDate = fromDate;
		this.path = path;
		this.entry = entry;
	}

	public Date getFromDate()
	{
		return fromDate;
	}

	public void setFromDate(final Date fromDate)
	{
		this.fromDate = fromDate;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(final String path)
	{
		this.path = path;
	}

	public String getEntry()
	{
		return entry;
	}

	public void setEntry(final String entry)
	{
		this.entry = entry;
	}
	
	public boolean isValid()
	{
		if (this.fromDate == null || fromDate.after(new Date()))
			return false;
		if (this.entry == null || this.entry.trim().length() == 0)
			return false;
		if (this.path == null || this.path.trim().length() == 0)
			return false;
		return true;
	}
}
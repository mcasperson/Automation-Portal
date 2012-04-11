package com.redhat.automationportal.scripts.svnstats;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.redhat.automationportal.base.AutomationBase;
import com.redhat.ecs.commonutils.FileUtilities;
import com.redhat.ecs.commonutils.HTMLUtilities;
import com.redhat.ecs.commonutils.HTTPUtilities;
import com.redhat.ecs.commonutils.MIMEUtilities;
import com.redhat.ecs.commonutils.ZipUtilities;

public class SvnStats extends AutomationBase
{
	public static final String BUILD = "20111031-1013";
	private static final String TEMPLATE_DIR = "/opt/automation-interface/svn_stats";
	private static final String HTML_SINGLE_BUILD_DIR = "/tmp/en-US/html-single";
	private final SimpleDateFormat xmlFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private ConfigXMLData configData = new ConfigXMLData();
	private List<ConfigXMLData> configDataItems = new ArrayList<ConfigXMLData>();

	public void deleteConfig(final ConfigXMLData configData)
	{
		this.configDataItems.remove(configData);
	}

	public String getBuild()
	{
		return BUILD;
	}

	public ConfigXMLData getConfigData()
	{
		return configData;
	}

	public List<ConfigXMLData> getConfigDataItems()
	{
		return configDataItems;
	}

	private String getConfigXml()
	{
		String configXml = "<config>";

		for (final ConfigXMLData data : this.configDataItems)
		{
			configXml += "<entry from_date=\"" + xmlFormatter.format(data.getFromDate()) + "\" " + "path=\"" + data.getPath() + "\">" + data.getEntry() + "</entry>";
		}

		configXml += "</config>";

		return configXml;
	}

	public void run(final boolean download)
	{
		final Integer randomInt = this.generateRandomInt();
		
		this.message = "";
		
		if (this.configDataItems.size() != 0)
		{
			final String script =
			// copy the template files
			"cp -R \\\"" + TEMPLATE_DIR + "/\\\"* \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +

			// dump the new config.xml file
			"&& echo '" + cleanStringForBash(getConfigXml()) + "' > \\\"" + this.getTmpDirectory(randomInt) + "/config.xml\\\" " +

			// enter the scripts directory
			"&& cd \\\"" + this.getTmpDirectory(randomInt) + "/scripts\\\" " +

			// run the python script
			"&& python run.py " + "&& cd \\\"" + this.getTmpDirectory(randomInt) + "\\\" " + "&& publican build --formats=html-single --langs=en-US";
			
			runScript(script, randomInt);

			if (this.success)
			{
				if (download)
					buildZip(randomInt);
				else
					displayHtml(randomInt);
			}

			// cleanup the temp dir
			cleanup(randomInt);
		}
		else
		{
			this.message = "Please add some config data.";
		}
	}

	private void displayHtml(final Integer randomInt)
	{
		final File htmlSingle = new File(this.getTmpDirectory(randomInt) + HTML_SINGLE_BUILD_DIR + "/index.html");
		if (htmlSingle.exists())
		{
			final String indexString = FileUtilities.readFileContents(htmlSingle);
			final String inlinedIndexString = HTMLUtilities.inlineHtmlPage(indexString, this.getTmpDirectory(randomInt) + HTML_SINGLE_BUILD_DIR);
			HTTPUtilities.writeOutToBrowser(inlinedIndexString.getBytes(), MIMEUtilities.HTML_MIME_TYPE);
		}
		else
		{
			this.output = "=== OUTPUT LOG ===\n" + this.output;
			HTTPUtilities.writeOutToBrowser(this.output.getBytes(), MIMEUtilities.TEXT_MIME_TYPE);
		}
	}

	private void buildZip(final Integer randomInt)
	{
		final String publicanBuildOutput = this.getTmpDirectory(randomInt);// +
																	// HTML_SINGLE_BUILD_DIR;
		final HashMap<String, byte[]> fileMap = new HashMap<String, byte[]>();

		// add a copy of the output to the zip file
		fileMap.put(AutomationBase.STANDARD_LOG_FILENAME, this.output != null ? this.output.getBytes() : new byte[]
		{});

		ZipUtilities.createZipMap(new File(publicanBuildOutput), publicanBuildOutput, fileMap);
		final byte[] zipFile = ZipUtilities.createZip(fileMap);

		HTTPUtilities.writeOutContent(zipFile, "SVNStats.zip", MIMEUtilities.ZIP_MIME_TYPE);
	}

	public void saveConfig()
	{
		if (this.configData.isValid())
		{
			this.configDataItems.add(new ConfigXMLData(this.configData));
			this.message = "";
		}
		else
		{
			this.message = "Config entry is invalid.";
		}
	}

	public void setConfigData(final ConfigXMLData configData)
	{
		this.configData = configData;
	}

	public void setConfigDataItems(List<ConfigXMLData> configDataItems)
	{
		this.configDataItems = configDataItems;
	}

}




package com.redhat.automationportal.scripts;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redhat.automationportal.base.AutomationBase;
import com.redhat.automationportal.base.Constants;
import com.redhat.ecs.commonutils.ExecUtilities;
import com.redhat.ecs.commonutils.PropertyUtils;

public class FlagSearch extends AutomationBase
{
	private static String BUILD = "20120514-1344";
	private static final String ALIAS_RE = "alias \"(?<Alias>.*?)\".*";
	private static final Pattern ALIAS_RE_PATTERN = Pattern.compile(ALIAS_RE);
	private static final String TEMPLATE_DIR = "/opt/automation-interface/Flag_search";
	private static final String SAVE_DATA_FOLDER = "FlagSearch";
	private static final String PERSIST_FILENAME = "saved_searches.txt";

	private String bugzillaUsername;
	private String bugzillaPassword;
	private String productName;
	private String component;
	private String loadSearch;
	private String saveSearch;

	public String getBugzillaPassword()
	{
		return bugzillaPassword;
	}

	public void setBugzillaPassword(String bugzillaPassword)
	{
		this.bugzillaPassword = bugzillaPassword;
	}

	public String getBugzillaUsername()
	{
		return bugzillaUsername;
	}

	public void setBugzillaUsername(final String bugzillaUsername)
	{
		this.bugzillaUsername = bugzillaUsername;
	}

	public String getProductName()
	{
		return productName;
	}

	public void setProductName(String productName)
	{
		this.productName = productName;
	}

	public String getComponent()
	{
		return component;
	}

	public void setComponent(String component)
	{
		this.component = component;
	}

	@Override
	public String getBuild()
	{
		return BUILD;
	}

	public List<String> getSavedSearches()
	{
		Process process = null;
		try
		{
			final String catCommand = "/bin/su " + (this.username == null ? "automation-user" : this.username) + " -c \"" +
			/* check to see if the saved file exists */
			"if [ -f ~/" + AutomationBase.SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "/" + PERSIST_FILENAME + "  ]; then " +
			/* dump the contents of the file */
			"cat ~/" + AutomationBase.SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "/" + PERSIST_FILENAME + "; " +
			/* exit the statement */
			"fi; \"";

			final String[] command = new String[]
			{ "/bin/bash", "-c", catCommand };
			process = Runtime.getRuntime().exec(command, ExecUtilities.getEnvironmentVars());
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			this.success = ExecUtilities.runCommand(process, outputStream);
			final String output = outputStream.toString();

			final List<String> retValue = new ArrayList<String>();

			for (final String alias : output.split("\n"))
			{
				final Matcher matcher = ALIAS_RE_PATTERN.matcher(alias);
				if (matcher.matches())
				{
					retValue.add(matcher.group("Alias"));
				}
			}

			return retValue;
		}
		catch (final Exception ex)
		{
			return new ArrayList<String>();
		}
		finally
		{
			process.destroy();
		}
	}

	public boolean run()
	{
		if (this.bugzillaUsername != null && this.bugzillaPassword != null && this.bugzillaUsername.trim().length() != 0 && this.bugzillaPassword.trim().length() != 0)
		{
			final Integer randomInt = this.generateRandomInt();
			final String randomString = this.generateRandomString(10);

			if (randomInt == null)
			{
				this.message = "BugzillaReportGenerator.run() " + PropertyUtils.getProperty(Constants.ERROR_FPROPERTY_FILENAME, "AMPT0001", this.getClass());
				return false;
			}

			if (randomString == null)
			{
				this.message = "BugzillaReportGenerator.run() " + PropertyUtils.getProperty(Constants.ERROR_FPROPERTY_FILENAME, "AMPT0002", this.getClass());
				return false;
			}

			this.message = "";

			final String[] environment = new String[]
			{ randomString + "=" + this.bugzillaPassword };

			String command = null;

			if (this.loadSearch != null && !this.loadSearch.isEmpty())
			{
				command = "&& perl flag_search7.pl --login=" + bugzillaUsername + " --password=${" + randomString + "} --load --alias=\\\"" + this.loadSearch + "\\\"";
			}
			else if (this.productName != null && !this.productName.isEmpty() && this.component != null && !this.component.isEmpty() && (this.saveSearch == null || this.saveSearch.isEmpty()))
			{
				command = "&& perl flag_search7.pl --login=" + bugzillaUsername + " --password=${" + randomString + "} --product_name=\\\"" + this.productName + "\\\" --component=\\\"" + this.component + "\\\" ";
			}
			else if (this.productName != null && !this.productName.isEmpty() && this.component != null && !this.component.isEmpty() && this.saveSearch != null && !this.saveSearch.isEmpty())
			{
				command = "&& perl flag_search7.pl --login=" + bugzillaUsername + " --password=${" + randomString + "} --product_name=\\\"" + this.productName + "\\\" --component=\\\"" + this.component + "\\\" --save --alias=\\\"" + this.saveSearch + "\\\" ";
			}

			if (command != null)
			{

				final String script =
				// copy the template files
				"cp -R \\\"" + TEMPLATE_DIR + "/\\\"* \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +

				// make sure the data folder exists
						"&& if [ ! -d ~" + (this.username == null ? "automation-user" : this.username) + "/" + SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + " ]; then " +

						// create it if it doesn't
						"mkdir -p ~" + (this.username == null ? "automation-user" : this.username) + "/" + SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "; " +

						/* exit the statement */
						"fi " +

						// If the saved file exists
						"&& if [ -f ~" + (this.username == null ? "automation-user" : this.username) + "/" + SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "/" + PERSIST_FILENAME + " ]; then " +

						/* copy the saved file */
						"cp ~" + (this.username == null ? "automation-user" : this.username) + "/" + SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "/" + PERSIST_FILENAME + " \\\"" + this.getTmpDirectory(randomInt) + "/\\\"; " +

						/* exit the statement */
						"fi " +

						// enter the scripts directory
						"&& cd \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +

						// run the python script
						command +

						// copy the save_searches.txt to the data folder
						"&& cp \\\"" + this.getTmpDirectory(randomInt) + "/" + PERSIST_FILENAME + "\\\" ~" + (this.username == null ? "automation-user" : this.username) + "/" + SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "/ ";

				runScript(script, randomInt, true, true, true, null, environment);

				// cleanup the temp dir
				cleanup(randomInt);
				
				return true;
			}
			else
			{
				this.message = "Please enter a product name and component or a saved search";
				return false;
			}

			
		}
		else
		{
			this.message = "Please enter a username and password";
			return false;
		}

	}

	public String getLoadSearch()
	{
		return loadSearch;
	}

	public void setLoadSearch(String loadSearch)
	{
		this.loadSearch = loadSearch;
	}

	public String getSaveSearch()
	{
		return saveSearch;
	}

	public void setSaveSearch(String saveSearch)
	{
		this.saveSearch = saveSearch;
	}

}

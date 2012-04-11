package com.redhat.automationportal.scripts;

import com.redhat.automationportal.base.AutomationBase;

public class BugzillaReportGenerator extends AutomationBase
{
	private static String BUILD = "20111125-0710";
	private static final String PASSWORD_ENV_VARIABLE_NAME = "PASSWORD";
	private static final String TEMPLATE_DIR = "/opt/automation-interface/Report_Generator";
	
	private String bugzillaUsername;
	private String bugzillaPassword;
	
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

	@Override
	public String getBuild()
	{
		return BUILD;
	}

	public void run()
	{
		if (this.bugzillaUsername.trim().length() != 0 && this.getPassword().trim().length() != 0)
		{
			final Integer randomInt = this.generateRandomInt();

			this.message = "";

			final String[] environment = new String[] { PASSWORD_ENV_VARIABLE_NAME + "=" + this.getBugzillaPassword() };

			final String script =
			// copy the template files
			"cp -R \\\"" + TEMPLATE_DIR + "/\\\"* \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +

			// enter the scripts directory
			"&& cd \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +

			// run the python script
			"&& perl report_generator.pl -login=" + bugzillaUsername + " -password=${" + PASSWORD_ENV_VARIABLE_NAME + "}";

			runScript(script, randomInt, true, true, true, null, environment);

			// cleanup the temp dir
			cleanup(randomInt);
		}
		else
		{
			this.message = "Please enter a username and password";
		}

	}

	
}

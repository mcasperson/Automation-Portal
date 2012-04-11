package com.redhat.automationportal.scripts;

import com.redhat.automationportal.base.AutomationBase;

public class ParseToc extends AutomationBase
{
	private static String BUILD = "20111031-1138";
	private static final String TEMPLATE_DIR = "/opt/automation-interface/parse_toc";

	@Override
	public String getBuild()
	{
		return BUILD;
	}
	
	public void run(final boolean download)
	{
		final Integer randomInt = this.generateRandomInt();
		
		this.message = "";

		final String script =
			// copy the template files
			"cp -R \\\"" + TEMPLATE_DIR + "/\\\"* \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +

			// enter the scripts directory
			"&& cd \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +

			// run the python script
			"&& python report.py";
			
		runScript(script, randomInt);

		// cleanup the temp dir
		cleanup(randomInt);

	}
}

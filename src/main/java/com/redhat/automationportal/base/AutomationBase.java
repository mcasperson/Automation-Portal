package com.redhat.automationportal.base;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.ecs.commonutils.ExecUtilities;
import com.redhat.ecs.commonutils.FileUtilities;

public abstract class AutomationBase
{
	protected static final String STANDARD_LOG_FILENAME = "AutomationInterface.log";
	protected String message;
	protected String output;
	protected String password;
	protected String username;
	protected boolean success;

	public AutomationBase()
	{
		generateRandomInt();
	}

	protected Integer generateRandomInt()
	{
		Integer randomInt = 123456789;
		
		try
		{
			final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			randomInt = random.nextInt();

			// files and directories that start with a dash are a pain, so don't
			// use negative numbers
			if (randomInt < 0)
				randomInt *= -1;
		}
		catch (Exception ex)
		{
			
		}
		
		return randomInt;
	}

	/**
	 * Escape characters that cause problems for Bash scripts by enclosing the string in single quotes, 
	 * and enclosing single quotes in double quotes. See http://www.gnu.org/software/bash/manual/bashref.html#Single-Quotes
	 * 
	 * @param input
	 *            The string to be fixed
	 * @return The fixed string
	 */
	protected String cleanStringForBash(final String input)
	{
		String retValue = "";
		int lastIndex = 0;
		/* loop through looking for single quotes */
		while (input.indexOf('\'', lastIndex) != -1)
		{
			final int thisIndex = input.indexOf('\'', lastIndex);
			retValue += input.substring(lastIndex, thisIndex) + "\"'\"";
			lastIndex = thisIndex + 1;
		}
		retValue += input.substring(lastIndex);
		return "'" + retValue + "'";
	}

	protected boolean createTempDirectory(final Integer randomInt)
	{
		// create the temp folder
		final File tmpDir = new File(getTmpDirectory(randomInt));

		this.success = tmpDir.mkdir();
		if (!this.success)
		{
			reportOutcome();
			this.output = "Could not create directory " + this.getTmpDirectory(randomInt);
			return false;
		}

		return true;
	}

	/**
	 * Deletes a directory, along will all files and subdirectories
	 * 
	 * @param path
	 *            The directory to be deleted
	 * @return true if the operation was successful, false otherwise
	 */
	private boolean deleteDirectory(final File path)
	{
		if (path.exists())
		{
			final File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					deleteDirectory(files[i]);
				}
				else
				{
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	/**
	 * Deletes the standard temporary directory, and generated a new random
	 * number
	 * 
	 * @return true if the operation was successful, false otherwise
	 */
	protected boolean cleanup(final Integer randomInt)
	{
		final boolean retValue = deleteDirectory(new File(getTmpDirectory(randomInt)));

		return retValue;
	}

	public abstract String getBuild();

	public String getMessage()
	{
		return message;
	}

	public String getOutput()
	{
		return output;
	}

	public String getPassword()
	{
		return password;
	}

	public String getTmpDirectory(final Integer randomInt)
	{
		return "/tmp/" + randomInt;
	}

	public String getUsername()
	{
		return username;
	}

	protected void reportOutcome()
	{
		if (this.success)
			this.message = "The command was successfully executed.";
		else
			this.message = "ERROR! The command was not successfully executed.";
	}

	private boolean checkStringQuotesAreEscaped(final String input)
	{
		/*
		 * As a safety precaution, we won't run any script that includes
		 * unescaped quotes. A script with unescaped quotes could be used to
		 * exit the script being run by su, and run arbitrary code as the
		 * application server user (which should be root).
		 * 
		 * We can't just test that the quote is preceded by a backslash, because
		 * that preceding backslash could itself be escaped by yet another
		 * Preceding backslash e.g. \\" is still an unescaped quote. The only
		 * way to be sure is to make sure that the quote is preceded by an odd
		 * number of backslashes.
		 */
		int lastPosition = 0;
		while (lastPosition != -1 && lastPosition + 1 < input.length())
		{
			lastPosition = input.indexOf("\"", lastPosition + 1);
			if (lastPosition != -1)
			{
				int preceedingBackSlashCount = 0;
				for (int i = lastPosition - 1; i >= 0; --i)
				{
					if (input.charAt(i) == '\\')
						++preceedingBackSlashCount;
					else
						break;
				}

				if (preceedingBackSlashCount % 2 != 1)
					return false;
			}
		}

		return true;
	}

	protected void runScript(final String script, final Integer randomInt)
	{
		runScript(script, randomInt, true, true, true, new LinkedHashMap<String, String>(), new String[]
		{});
	}

	protected void runScript(final String script, final Integer randomInt,  final boolean rootRequired)
	{
		runScript(script, randomInt,  rootRequired, true, true, new LinkedHashMap<String, String>(), new String[]
		{});
	}

	protected void runScript(final String script, final Integer randomInt,  final boolean rootRequired, final boolean modifyTempDirPermissions)
	{
		runScript(script, randomInt,  rootRequired, modifyTempDirPermissions, true, new LinkedHashMap<String, String>(), new String[]
		{});
	}

	protected void runScript(final String script, final Integer randomInt,  final boolean rootRequired, final boolean modifyTempDirPermissions, final boolean runAsUser)
	{
		runScript(script, randomInt,  rootRequired, modifyTempDirPermissions, runAsUser, new LinkedHashMap<String, String>(), new String[]
		{});
	}

	protected void runScript(final String script, final Integer randomInt,  final boolean rootRequired, final boolean modifyTempDirPermissions, final boolean runAsUser, final LinkedHashMap<String, String> waitResponses)
	{
		runScript(script, randomInt,  rootRequired, modifyTempDirPermissions, runAsUser, waitResponses, new String[]
		{});
	}

	/**
	 * Run a bash script.
	 * 
	 * @param script
	 *            The bash script to run
	 * @param rootRequired
	 *            true if the script is required to be run under the root
	 *            account. if runAsUser is true, then rootRequired should be
	 *            true. Set to false to debug under a non-root account.
	 * @param modifyTempDirPermissions
	 *            true if the permissions on the temporary directory should be
	 *            modified to be exclusively owned by username. Set to false to debug under a non-root account.
	 * @param runAsUser
	 *            true if all script operations should be run under the username
	 *            account. Set to false to debug under a non-root account.
	 * @param waitResponses
	 *            The challenge / response pairs. Order is important, which is
	 *            why we use a LinkedHashMap.
	 * @param environment
	 *            Environment variables in the form VARIABLE=setting
	 */
	protected void runScript(final String script, final Integer randomInt, final boolean rootRequired, final boolean modifyTempDirPermissions, final boolean runAsUser, final LinkedHashMap<String, String> waitResponses, final String[] environment)
	{
		try
		{			
			if (!checkStringQuotesAreEscaped(script))
				return;
			
			if (waitResponses != null)
			{
				for (final String match : waitResponses.keySet())
				{
					final String response = waitResponses.get(match);
					if (!checkStringQuotesAreEscaped(response) || !checkStringQuotesAreEscaped(match))
						return;
				}
			}

			if (!this.createTempDirectory(randomInt))
				return;

			// generate some random temporary files to serve
			// as the input and output files for the empty command
			// and as the log file
			final String inFifo = this.getTmpDirectory(randomInt) + "/in" + randomInt + ".fifo";
			final String outFifo = this.getTmpDirectory(randomInt) + "/out" + randomInt + ".fifo";
			final String log = this.getTmpDirectory(randomInt) + "/log" + randomInt + ".txt";

			// make a note of the strings that should not be in any log files
			final List<String> doNotPrintStrings = new ArrayList<String>();
			if (password != null)
			{
				doNotPrintStrings.add(password);
			}

			/*
			 * Either execute the script under the user's profile with su (or
			 * use the default user account automation-user when no username is
			 * specified), or just use the local AS user.
			 */
			final String runAsUserCommand = runAsUser ? "/bin/su " + (this.username == null ? "automation-user" : this.username) + " -c " : "/bin/bash -c ";

			/*
			 * Make sure we are running the server under the root account,
			 * otherwise we may end up just sitting at the su password prompt.
			 */
			final String rootRequiredCommand = rootRequired ? "whoami | grep -q root && " : "";

			final String changeTempDirPermissionsCommand = !modifyTempDirPermissions ? "" :
			/*
			 * Set the permissions on the temporary directory. This is because
			 * user accounts created against the LDAP/Kerberos database don't
			 * have a valid group name. This in turn leads to
			 * "error: Bad owner/group" errors when using rpmbuild.
			 * 
			 * If a temporary directory is created using the
			 * createTempDirectory() function by an extending class, it will be
			 * owned by root:root. So before we call su the permissions are
			 * fixed for the user running this script.
			 */

			/* test for the existence of the temp directory */
			"if [ -d \"" + this.getTmpDirectory(randomInt) + "\" ]; then " +

			/* set the owner to the user, or the default automation-user */
			"chown " + (this.username == null ? "automation-user" : this.username) + " \"" + this.getTmpDirectory(randomInt) + "\" " +

			/* set the group to automation-user */
			"&& chgrp automation-user \"" + this.getTmpDirectory(randomInt) + "\" " +

			/*
			 * Prevent access to any other users
			 */
			"&& chmod og-wrx \"" + this.getTmpDirectory(randomInt) + "\" " +

			/*
			 * make the group sticky, preventing any new files created in this
			 * directory from having the invalid user group by default
			 */
			"&& chmod g+s \"" + this.getTmpDirectory(randomInt) + "\"; " +

			/* exit the if statement */
			"fi && ";

			// run bash
			String mainCommand = rootRequiredCommand + changeTempDirPermissionsCommand +

			/*
			 * Use empty to create a pseudo terminal that su will run in.
			 * Otherwise su will complain about not being run in a terminal.
			 */
			"/usr/local/bin/empty -f -v -i " + inFifo + " -o " + outFifo + " " +

			runAsUserCommand +

			/* add the script to the sudo -c command */
			"\"" + script + "\" ";

			/*
			 * An extending class can define a number of challenge / response
			 * pairs for applications like kinit that usually expect input from
			 * the keyboard. We deal with these by starting a number of empty
			 * commands to watch for the challenge, and supply the appropriate
			 * response.
			 * 
			 * The empty command will actually return a non zero value if it
			 * successfully matches a challenge. The return value matches the
			 * number of challenges that were matched. Since we are only doing a
			 * single match here for each entry in the waitResponses collection,
			 * we know that a successful return value is actually 1. We append
			 * the command [[ $? -eq 1 ]] (which returns 0 if the last return
			 * value was 1) to allow the script to continue using && to string
			 * commands together depending on the outcome of the last command.
			 */
			if (waitResponses != null)
				for (final String match : waitResponses.keySet())
					mainCommand += "&& /usr/local/bin/empty -w -i " + outFifo + " -o " + inFifo + " \"" + match + "\" \"" + waitResponses.get(match) + "\\n\"; [[ $? -eq 1 ]] ";

			/*
			 * Dump the output of the escape out fifo file into a log file. The
			 * escape application will clean up the fifo files when it shuts
			 * down, so this way we can keep a copy of the output before it is
			 * deleted.
			 * 
			 * You can run tail -f on this log file to watch the progress of the
			 * script on the server.
			 */
			mainCommand += "&& cat " + outFifo + " > " + log;

			final String[] command = new String[]
			{ "/bin/bash", "-c", mainCommand };

			// dump the command we are running to stdout, excluding any
			// passwords
			for (final String commandElement : command)
			{
				String sanatizedLine = commandElement;
				for (final String replace : doNotPrintStrings)
					sanatizedLine = sanatizedLine.replace(replace, "****");
				System.out.print(" " + sanatizedLine);
			}
			System.out.print("\n");

			// run the command
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			this.success = false;
			Process process = null;
			try
			{
				/*
				 * Supplying any kind of environment variables (even an empty
				 * array) overrides things like path, which kills most scripts.
				 * So we use ExecUtilities.getEnvironmentVars() to get a copy of
				 * the existing environment variables, and then append our
				 * custom ones.
				 */
				process = Runtime.getRuntime().exec(command, CollectionUtilities.concat(ExecUtilities.getEnvironmentVars(), environment));
				this.success = ExecUtilities.runCommand(process, outputStream, doNotPrintStrings);
			}
			finally
			{
				process.destroy();
				System.out.println("Command has finished.");
			}

			reportOutcome();

			// get the contents of the log file
			this.output = FileUtilities.readFileContents(new File(log));
		}
		catch (Exception ex)
		{
			this.success = false;
			reportOutcome();
			ExceptionUtilities.handleException(ex);
		}
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public void setOutput(String output)
	{
		this.output = output;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	protected boolean validateInput()
	{
		this.message = "";

		if (username == null || username.length() == 0)
		{
			this.message = "You need to enter your Kerberos username.";
			return false;
		}

		if (password == null || password.length() == 0)
		{
			this.message = "You need to enter your Kerberos password.";
			return false;
		}

		if (!validateUser(this.username, this.password))
		{
			this.message = "The entered username and password was incorrect.";
			return false;
		}

		return true;
	}

	private boolean validateUser(final String username, final String password)
	{
		LoginContext lc = null;
		try
		{
			lc = new LoginContext("jaas", new CallbackHandler()
			{
				public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
				{
					for (int i = 0; i < callbacks.length; i++)
					{
						if (callbacks[i] instanceof PasswordCallback)
						{
							final PasswordCallback pc = (PasswordCallback) callbacks[i];
							final char[] passwordCharArray = new char[password.length()];
							for (int j = 0; j < password.length(); ++j)
								passwordCharArray[j] = password.charAt(j);
							pc.setPassword(passwordCharArray);
						}
						else if (callbacks[i] instanceof NameCallback)
						{
							final NameCallback nc = (NameCallback) callbacks[i];
							nc.setName(username);
						}
					}
				}
			});
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
			return false;
		}

		try
		{
			lc.login();
			return true;
		}
		catch (final LoginException ex)
		{
			// Authentication failed
			ExceptionUtilities.handleException(ex);
			return false;
		}
	}
}
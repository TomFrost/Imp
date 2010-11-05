/**
 * Information.java
 * Tom Frost
 * Aug 13, 2006
 *
 * A module to give information about the bot, loaded modules, and commands.
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.*;

import org.jibble.pircbot.Colors;


/**
 * @author Tom Frost
 *
 */
public class Information extends Module {
	
	private UserManager userManager;
	private ModuleManager moduleManager;
	private String[] modList;
	
	private String infoHelp = "Usage: " + PREFIX + "info <module>\n" +
		"Information can be gotten about the bot itself by omitting a module, or about a module by " +
		"specifying which.  A list of modules can be obtained from the information page.";
	private String helpHelp = "Usage: " + PREFIX + "help <command>\n" + 
		"Offers a command list when used alone, or help with a command when " +
		"a command is specified.";
	
	private Command[] commands = new Command[] {
		new Command("info", 'p', 'b', infoHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				int userAccess = userManager.getUserLevel(sender);
				if (request.equals("")) {
					sendMessage(sender, Colors.BOLD + "IMP: IRC Modularized Processor");
					sendMessage(sender, Colors.BOLD + "Programmed in Java by Tom Frost on August 10, 2006.");
					sendMessage(sender, " ");
					sendMessage(sender, "IMP is a java IRC bot using PircBot for its IRC-api implementation.  Coded to be " +
						"completely modular, IMP itself does nothing but connect to a server without loading and " +
						"executing the attached modules.  In fact, you're interacting with the 'Information' module " +
						"right now.");
					sendMessage(sender, " ");
					String nextLine = Colors.BOLD + "Loaded modules: " + Colors.NORMAL + modList[0];
					sendMessage(sender, " ");
					for (int i = 1; i < modList.length; i++)
						nextLine += ", " + modList[i];
					sendMessage(sender, nextLine);
					sendMessage(sender, "Message '" + PREFIX + "info [module] to list information " +
						"for that module.");
				}
				else {
					ModuleData curMod = moduleManager.getModuleData(request);
					if (curMod == null)
						sendMessage(sender, "I'm sorry, the module '" + request + "' does not exist.");
					else {
						int dotIndex;
						String versionStr = curMod.getVersion() + "";
						String version = versionStr.substring(0, theLesserPositive(versionStr.length(),
								(dotIndex = versionStr.indexOf('.')) + 2));
						if (dotIndex == -1)
							version += ".0";
						sendMessage(sender, Colors.BOLD + curMod.getName() + " v" + version);
						sendMessage(sender, Colors.BOLD + "created by " + curMod.getAuthor());
						sendMessage(sender, " ");
						sendMessage(sender, curMod.getAbout());
						sendMessage(sender, " ");
						sendMessage(sender, Colors.BOLD + curMod.getName() + " provides the following commands:");
						String commandList = "";
						Command[] commands = curMod.getCommands();
						for (int i = 0; i < commands.length; i++) {
							if (userAccess >= commands[i].getCommandLevel())
								commandList += ", " + commands[i].getName();
						}
						if (commands.equals(""))
							sendMessage(sender, "None available.");
						else
							sendMessage(sender, commandList.substring(2));
						sendMessage(sender, " ");
						sendMessage(sender, "Message " + PREFIX + "help [command] for help on " +
								"a certain command.");
					}
				}
			}
		},
		new Command("help", 'p', 'b', helpHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				int userAccess = userManager.getUserLevel(sender);
				if (request.equals("")) {
					sendMessage(sender, Colors.BOLD + "Help is available for the following commands.  Message " + PREFIX + "help [command] to get help for that command.");
					String[] allCommands = getAllCommands();
					switch (userAccess) {
					case 6:
						if (!allCommands[6].equals(""))
							sendMessage(sender, Colors.BOLD + "Master commands: " + Colors.NORMAL + allCommands[6]);
					case 5:
						if (!allCommands[5].equals(""))
							sendMessage(sender, Colors.BOLD + "Owner commands: " + Colors.NORMAL + allCommands[5]);
					case 4:
						if (!allCommands[4].equals(""))
							sendMessage(sender, Colors.BOLD + "Admin commands: " + Colors.NORMAL + allCommands[4]);
					case 3:
						if (!allCommands[3].equals(""))
							sendMessage(sender, Colors.BOLD + "Op commands: " + Colors.NORMAL + allCommands[3]);
					case 2:
						if (!allCommands[2].equals(""))
							sendMessage(sender, Colors.BOLD + "Half-Op commands: " + Colors.NORMAL + allCommands[2]);
					case 1:
						if (!allCommands[1].equals(""))
							sendMessage(sender, Colors.BOLD + "Voiced user commands: " + Colors.NORMAL + allCommands[1]);
					case 0:
						if (!allCommands[0].equals(""))
							sendMessage(sender, Colors.BOLD + "Public commands: " + Colors.NORMAL + allCommands[0]);
						break;
					case -1:
						sendMessage(sender, "Sorry, you must join " + CONFIG.getValue(CONF_CHANNEL) + " to communicate with me.");
					}
				}
				else {
					if (request.startsWith(PREFIX)) {
						try {
							request = request.substring(PREFIX.length());
						}
						catch (Exception e) {}
					}
					ModuleData curMod = moduleManager.getModuleDataForCommand(request);
					if (curMod == null) {
						sendMessage(sender, "I'm sorry, I can't find that command.  Message " + PREFIX + "help to get a complete list of commands.");
						return;
					}
					Command temp = moduleManager.getCommand(request);
					if (userManager.getUserLevel(sender) >= temp.getCommandLevel()) {
						char commMethod = temp.getMethod();
						String help = temp.getHelp();
						sendMessage(sender, Colors.BOLD + "Command [ " + Colors.NORMAL + temp.getName() + Colors.BOLD + " ]");
						sendMessage(sender, Colors.BOLD + "handled by module " + curMod.getName() + ".  Message " + PREFIX + "info " + curMod.getName() + " for more information.");
						sendMessage(sender, " ");
						sendMessage(sender, help);
						switch (commMethod) {
						case 'b':
							sendMessage(sender, "This command can be spoken in the channel or messaged privately."); break;
						case 's':
							sendMessage(sender, "This command must be spoken in the channel."); break;
						case 'p':
							sendMessage(sender, "This command must be called with /msg " + BotFactory.getBot().getNick() + " " + temp.getName() + " [options]");
						}
					}
					else {
						sendMessage(sender, "I'm sorry.  You do not have access to the help file for that command.");
					}
				}
			}
		}
	};

	public ModuleData getModuleData() {
		return new ModuleData("Information", (float)2.0, "Tom Frost", "A module to give information about the " +
				"bot, loaded modules, and commands. Information also provides access to all command help " +
				"documentation.", new String[] {"User Manager"}, new float[] {(float)2.1}, commands);
	}
	
	public void initialize() {
		moduleManager = BotFactory.getModuleManager();
		userManager = ((UserManager)moduleManager.getModule("User Manager"));
		modList = moduleManager.getModuleList();
	}
	
	private String[] getAllCommands() {
		String[] commands = new String[] {"", "", "", "", "", "", ""};
		Command[] curCommands;
		for (int i = 0; i < modList.length; i++) {
			curCommands = moduleManager.getModuleData(modList[i]).getCommands();
			for (int q = 0; q < curCommands.length; q++) {
				if (commands[curCommands[q].getCommandLevel()].equals(""))
					commands[curCommands[q].getCommandLevel()] += PREFIX + curCommands[q];
				else
					commands[curCommands[q].getCommandLevel()] += ", " + PREFIX + curCommands[q];
			}
		}
		return commands;
	}
	
	private int theLesserPositive(int a, int b) {
		int min = Math.min(a, b);
		int max = Math.max(a, b);
		if (max < 0)
			return -1;
		if (min < 0)
			return -1;
		return min;
	}
}

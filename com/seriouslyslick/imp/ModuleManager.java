/**
 * ModuleManager.java
 * Tom Frost
 * August 12, 2006
 * 
 * Does what the name says!
 */
package com.seriouslyslick.imp;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;

import com.seriouslyslick.imp.modules.ModuleFactory;
import com.seriouslyslick.imp.modules.UserManager;

/**
 * ModuleManager is simply responsible for keeping track of all of the installed modules, their
 * associated {@link ModuleData} objects, their commands, and their {@link ConfigurationItem} objects.
 * <p>
 * All spoken text in the IRC channel is passed through this class, which them determines whether the
 * text is a legal command that was called by someone in the correct method with the appropriate permissions.
 * ModuleManager then interfaces with the command and executes it.
 * 
 * @author Tom Frost
 * @since 1.0
 * @version 1.0
 */
public class ModuleManager implements ConfigurationConstants {
	// Hashtables to keep track of the modules and associated ModuleData objects.
	private Hashtable<String, Module> modules = new Hashtable<String, Module>();
	private Hashtable<String, ModuleData> moduleData = new Hashtable<String, ModuleData>();
	
	// A list of the module-submitted ConfigurationItems, and a list of their names.
	private ArrayList<String> itemNames = new ArrayList<String>();
	private ArrayList<ConfigurationItem> configurationItems = new ArrayList<ConfigurationItem>();
	
	// The full list of loaded modules
	private ArrayList<String> moduleList = new ArrayList<String>();
	
	// Link to the required modules
	private UserManager userManager;
	
	// A Hashtable of command objects, as well as a Hashtable linking commands to their
	// parent modules.
	private Hashtable<String, Command> commands = new Hashtable<String, Command>();
	private Hashtable<String, Module> commandModules = new Hashtable<String, Module>();
	
	// Utility variables
	private boolean initialized = false;
	
	/**
	 * Constructs a new ModuleManager and prepares it for initialization.  The modules objects are created
	 * (but not yet initialized) when this constructor is called, and their (@link ConfigurationItem} objects
	 * are prepared to be read by the {@link ConfigurationManager}.
	 * 
	 * @throws ModuleParseException if a module duplicates a {@link ConfigurationItem} name already in use by
	 * another module.
	 */
	public ModuleManager() throws ModuleParseException {
		Module[] mods = ModuleFactory.getModules();
		ConfigurationItem[] confList;
		for (int i = 0; i < mods.length; i++) {
			confList = mods[i].getConfigurationItems();
			if (confList != null) {
				for (int q = 0; q < confList.length; q++) {
					if (itemNames.contains(confList[q].getName()))
						throw new ModuleParseException("Module '" + mods[i].getModuleData().getName() + "' has a duplicate " +
								"configuration item: " + confList[q].getName() + ".");
					else {
						itemNames.add(confList[q].getName());
						configurationItems.add(confList[q]);
					}
				}
			}
		}
	}
	
	/**
	 * To be called before interacting with the ModuleManager class.  The class populates itself with the Modules
	 * listed in {@link com.seriouslyslick.imp.ModuleFactory}.  The modules are parsed immediately for
	 * use.
	 * <p>
	 * This method should not be called before the {@link ConfigurationManager} has been initialized.
	 * 
	 * @throws ModuleParseException If any modules are found with duplicate names, or containing
	 * commands with duplicate names.  Also thrown if a module is loaded before its dependancy.
	 */
	public void initialize() {
		ModuleFactory.reloadModules();
		Module[] mods = ModuleFactory.getModules();
		String index, name;
		for (int i = 0; i < mods.length; i++) {
			System.out.print("Loading module '" + mods[i].getModuleData().getName() + "'...    ");
			name = mods[i].getModuleData().getName();
			index = name.toLowerCase();
			if (!modules.containsKey(index)) {
				boolean allow = true;
				for (int q = 0; q < mods[i].getModuleData().getRequiredMods().length; q++) {
					if (modules.get(mods[i].getModuleData().getRequiredMods()[q].toLowerCase()) == null ||
							moduleData.get(mods[i].getModuleData().getRequiredMods()[q].toLowerCase()).getVersion() < 
							mods[i].getModuleData().getRequiredModVersions()[q])
						allow = false;
				}
				if (allow) {
					Command[] cmds = mods[i].getModuleData().getCommands();
					for (int q = 0; q < cmds.length; q++) {
						if (commands.containsKey(cmds[q].getName()))
								allow = false;
					}
					if (allow) {
						modules.put(index, mods[i]);
						moduleData.put(index, mods[i].getModuleData());
						for (int q = 0; q < cmds.length; q++) {
							if (cmds[q].getAccess() == 'a'
									&& BotFactory.getConfigurationManager().getValue(CONF_ALLOWADMIN).toLowerCase().equals("false"))
								cmds[q].setAccess('o');
							if (cmds[q].getAccess() == 'o'
									&& BotFactory.getConfigurationManager().getValue(CONF_ALLOWOWNER).toLowerCase().equals("false"))
								cmds[q].setAccess('m');
							commands.put(cmds[q].getName(), cmds[q]);
							commandModules.put(cmds[q].getName(), mods[i]);
						}
						moduleList.add(name);
						System.out.println("[OK]");
					}
					else {
						System.out.println("[FAIL]");
						throw new ModuleParseException("A command contained in module '" + name +
								"' has already been registered.  Module skipped.");
					}
				}
				else {
					System.out.println("[FAIL]");
					throw new ModuleParseException("Dependency error with mod '" + 
							name + "'. Module skipped.");
				}
			}
			else {
				System.out.println("[FAIL]");
				throw new ModuleParseException("Cannot load modules with the same name: " + 
						name + ". Module skipped.");
			}
		}
		
		mods = modules.values().toArray(new Module[] {});
		for (int i = 0; i < mods.length; i++)
			mods[i].initialize();
		userManager = (UserManager)getModule("User Manager");
		initialized = true;
	}
	
	/**
	 * To be called with the appropriate information whenever someone in the IRC channel either speaks or
	 * private messages the bot.
	 * 
	 * @param channel The channel (or sender, if a PM) that the text was spoken in.
	 * @param sender The sender of the text.
	 * @param login The login of the sender of the text.
	 * @param hostname The hostname of the sender of the text.
	 * @param message The text.
	 * @param privmsg <code>true</code> if the text was sent via private message; <code>false</code> otherwise.
	 */
	public void messageHook(String channel, String sender, String login, String hostname, String message, boolean privmsg) {
		if (message != null && !sender.equals(BotFactory.getBot().getNick())) {
			boolean handled = false;
			if (message.length() > 0 && message.startsWith(BotFactory.getConfigurationManager().getValue(CONF_COMMANDPREFIX)) && !userManager.isBlacklisted(sender)) {
				message = message.substring(1);
				String[] data = message.split(" ", 2);
				data[0] = data[0].trim().toLowerCase();
				if (data.length < 2)
					data = new String[] {data[0], ""};
				if (commands.containsKey(data[0])) {
					Command command = commands.get(data[0]);
					int commandAccess = commands.get(data[0]).getCommandLevel();
					int userAccess = userManager.getUserLevel(sender);
					char method = commands.get(data[0]).getMethod();
					if (userAccess >= commandAccess) {
						if (privmsg) {
							if (method == 'b' || method == 'p') {
								command.run(channel, sender, login, hostname, data[1]);
								Enumeration<Module> mods = modules.elements();
								while (mods.hasMoreElements())
									mods.nextElement().onCommand(channel, sender, login, hostname, data[0], data[1], true);
								handled = true;
							}
							else
								BotFactory.getBot().sendMessage(channel, "Sorry, that command can only be spoken from within " + BotFactory.getConfigurationManager().getValue(CONF_CHANNEL) + ".");
						}
						else {
							if (method == 'b' || method == 's') {
								command.run(channel, sender, login, hostname, data[1]);
								Enumeration<Module> mods = modules.elements();
								while (mods.hasMoreElements())
									mods.nextElement().onCommand(channel, sender, login, hostname, data[0], data[1], false);
								handled = true;
							}
							else
								BotFactory.getBot().sendMessage(channel, "Sorry, that command can only be sent to me privately!");
						}
					}
				}
				else {
					if (privmsg)
						BotFactory.getBot().sendMessage(channel, "Sorry, I don't have that command.");
				}
			}
			if (!handled) {
				if (privmsg) {
					Enumeration<Module> mods = modules.elements();
					while (mods.hasMoreElements())
						mods.nextElement().onPrivateMessage(sender, login, hostname, message);
				}
				else {
					Enumeration<Module> mods = modules.elements();
					while (mods.hasMoreElements())
						mods.nextElement().onMessage(channel, sender, login, hostname, message);
				}
			}
		}
	}
	
	/**
	 * Gets a module matching the supplied name.
	 * 
	 * @param modName The case-insensitive name of the module desired.
	 * @return the module if modName exists in the module table; <code>null</code> if it does not exist.
	 */
	public Module getModule(String modName) {
		return modules.get(modName.toLowerCase());
	}
	
	/**
	 * Gets the module responsible for the supplied command name.
	 * 
	 * @param cmd The name of the command to get the module for.
	 * @return the module responsible for cmd if cmd exists; <code>null</code> if cmd does not exist.
	 */
	public Module getModuleByCommandName(String cmd) {
		return commandModules.get(cmd);
	}
	
	/**
	 * Gets the {@link ModuleData} object associated with the named module.
	 * 
	 * @param modName the name of the module to retrieve the ModuleData object for.
	 * @return the ModuleData object associated with modName if the modName module exists; <code>null</code> if modName does not exist.
	 */
	public ModuleData getModuleData(String modName) {
		return moduleData.get(modName.toLowerCase());
	}
	
	/**
	 * Gets the {@link ModuleData} object associated with the module responsible for the given command name.
	 * 
	 * @param command the command name.
	 * @return The ModuleData of the responsible module if command exists; <code>null</code> if command does not exist.
	 */
	public ModuleData getModuleDataForCommand(String command) {
		if (commands.containsKey(command.trim().toLowerCase()))
			return getModuleByCommandName(command.trim().toLowerCase()).getModuleData();
		return null;
	}
	
	/**
	 * Gets an array of all the loaded module objects' names.
	 * 
	 * @return an array of loaded modules' names.
	 */
	public String[] getModuleList() {
		return moduleList.toArray(new String[] {});
	}
	
	/**
	 * Gets an array of all the loaded modules.  This array corresponds directly with the module
	 * names array provided by the {@link #getModuleList()} method.
	 * 
	 * @return an array of the loaded modules.
	 */
	public Module[] getModules() {
		Module[] modArray = new Module[moduleList.size()];
		for (int i = 0; i < moduleList.size(); i++)
			modArray[i] = modules.get(moduleList.get(i).toLowerCase());
		return modArray;
	}
	
	/**
	 * Gets the list of module-requested {@link ConfigurationItem} objects to add to the bot's default set.
	 * This method can be called before the ModuleManager is initialized.
	 * 
	 * @return The modules' {@link ConfigurationItem} objects.
	 */
	public ConfigurationItem[] getConfigurationItems() {
		return configurationItems.toArray(new ConfigurationItem[] {});
	}
	
	/**
	 * Gets the {@link Command} object matching the supplied command name.
	 * @param cmd The command name to look up.
	 * @return The Command object associated with cmd if cmd exists; <code>null</code> if cmd does not exist.
	 */
	public Command getCommand(String cmd) {
		return commands.get(cmd.trim().toLowerCase());
	}
	
	/**
	 * Gets the current status of the ModuleManager object.
	 * @return <code>true</code> if the ModuleManager has been initialized; <code>false</code> otherwise.
	 */
	public boolean isInitialized() {
		return initialized;
	}
}

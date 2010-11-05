/**
 * ConfigurationManager.java
 * Tom Frost
 * Sep 19, 2006
 *
 * 
 */
package com.seriouslyslick.imp;

import java.util.ArrayList;
import java.util.Hashtable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;

/**
 *
 * The ConfigurationManager pulls the base {@link ConfigurationItem} objects from {@link Imp} as well as the
 * installed modules.  It provides an API for reading and writing these objects, including retreiving
 * them on the fly.
 * 
 * @author Tom Frost
 * @since 1.0
 * @version 1.1
 */
public class ConfigurationManager implements ConfigurationConstants {
	// An ArrayList of our current ConfigurationItem objects
	private ArrayList<ConfigurationItem> items = new ArrayList<ConfigurationItem>();
	
	// A Hashtable mapping the names of the ConfigurationItem objects to their index in the ArrayList.
	private Hashtable<String, Integer> nums = new Hashtable<String, Integer>();
	
	// A Hashtable of IDs and Objects, populated by modules in need of storage.
	private Hashtable<String, Object> objects = new Hashtable<String, Object>();
	
	// The file we're working with
	private File configFile;
	
	private boolean initialized = false;
	
	// Base framework configuration
	private ConfigurationItem[] configurationItems = new ConfigurationItem[] {
			new ConfigurationItem(CONF_BOTNICK, "The nickname of the bot.  Must be a valid IRC name.", "Imp") {
				public boolean isLegal(String val) {
					return val.matches("^(\\w|\\||_|\\-|`|~)*$");
				}
			},
			new ConfigurationItem(CONF_AUTONICKCHANGE, "Set to 'true', a new nickname will be automatically chosen (by adding numbers to the supplied nickname) if the supplied nickname is already taken on the server.  Must be either 'true' or 'false'.", "true") {
				public boolean isLegal(String val) {
					return val.matches("(?i)(true|false)");
				}
			},
			new ConfigurationItem(CONF_BOTLOGIN, "The username of the bot.  Must be a valid IRC username.", "Imp") {
				public boolean isLegal(String val) {
					return val.matches("^(\\w|\\||_|\\-|`|~)*$");
				}
			},
			new ConfigurationItem(CONF_FINGERREPLY, "The String to supply when Imp receives a FINGER request.", "Contact: tom@frosteddesign.com") {
				public boolean isLegal(String val) {
					return true;
				}
			},
			new ConfigurationItem(CONF_VERSIONREPLY, "The String to supply when Imp receives a VERSION request.", "IMP :: IRC Modularized Processor :: 1.0") {
				public boolean isLegal(String val) {
					return true;
				}
			},
			new ConfigurationItem(CONF_SERVERADDRESS, "The server to connect to.", "irc.freenode.net") {
				public boolean isLegal(String val) {
					return val.matches(".+");
				}
			},
			new ConfigurationItem(CONF_SERVERPORT, "The port to connect to on the server. Must be between 2 and 6 digits.", "6667") {
				public boolean isLegal(String val) {
					return val.matches("\\d{2,6}");
				}
			},
			new ConfigurationItem(CONF_SERVERPASS, "The password required to get into the server.  Leave this blank if no password is necessary.", "") {
				public boolean isLegal(String val) {
					return true;
				}
			},
			new ConfigurationItem(CONF_CHANNEL, "The channel that the bot will operate on.", "#ImpBot") {
				public boolean isLegal(String val) {
					return val.matches("\\S*");
				}
			},
			new ConfigurationItem(CONF_CHANNELPASS, "Password for the bot's operating channel.  Leave this blank for channels that aren't password-protected.", "") {
				public boolean isLegal(String val) {
					return true;
				}
			},
			new ConfigurationItem(CONF_COMMANDPREFIX, "The character(s) that must preceed any command sent to the bot.", "!") {
				public boolean isLegal(String val) {
					return val.matches(".+");
				}
			},
			new ConfigurationItem(CONF_DISPLAYVERBOSE, "Set to 'true', Imp will output client/server IO to stdout.  Set to 'false', these transmissions are hidden.", "true") {
				public boolean isLegal(String val) {
					return val.matches("(?i)(true|false)");
				}
			},
			new ConfigurationItem(CONF_MESSAGEDELAY, "The number of milliseconds to wait between each message transmission.  Setting this too high will result in lag; too low may result in being kicked (or worse) for spamming.  Min: 0, Max: 60000", "200") {
				public boolean isLegal(String val) {
					if (val.matches("\\d+")) {
						int value = Integer.parseInt(val);
						if (value >= 0 && value <= 60000)
							return true;
					}
					return false;
				}
			},
			new ConfigurationItem(CONF_MASTERNICK, "The nickname of the bot's master. Must be a valid IRC nickname.", "Owner") {
				public boolean isLegal(String val) {
					return val.matches("^(\\w|\\||_|\\-|`|~)*$");
				}
			},
			new ConfigurationItem(CONF_AUTORECONNECT, "Set to 'true', Imp will automatically reconnect to the server and rejoin the channel when disconnected.  Set to 'false', he stays offline.", "true") {
				public boolean isLegal(String val) {
					return val.matches("(?i)(true|false)");
				}
			},
			new ConfigurationItem(CONF_AUTOREJOIN, "Set to 'true', Imp will automatically rejoin his channel if he is kicked.  Set to 'false', he stays out.", "true") {
				public boolean isLegal(String val) {
					return val.matches("(?i)(true|false)");
				}
			},
			new ConfigurationItem(CONF_MASTERBYHOST, "Set to 'true', one can obtain Master status by calling the !master command from a name that shares the same hostname as the bot.  If 'false', a password must be provided.", "true") {
				public boolean isLegal(String val) {
					return val.matches("(?i)(true|false)");
				}
			},
			new ConfigurationItem(CONF_MASTERPASS, "The password used to gain Master status.  Must be between 5-20 characters, and only letters and numbers can be used.  Case-sensitive.", "password") {
				public boolean isLegal(String val) {
					return val.matches("[\\w\\d]+") && val.length() >= 5 && val.length() <= 20;
				}
			},
			new ConfigurationItem(CONF_ALLOWADMIN, "Set to 'true', commands can be set to only be executed by admins and up.  If 'false', admin commands will instead be assigned to the next highest level of power.", "true") {
				public boolean isLegal(String val) {
					return val.matches("(?i)(true|false)");
				}
			},
			new ConfigurationItem(CONF_ALLOWOWNER, "Set to 'true', commands can be set to only be executed by owners and masters.  If 'false', owner commands will instead be assigned to the master level.", "true") {
				public boolean isLegal(String val) {
					return val.matches("(?i)(true|false)");
				}
			}
	};
	
	/**
	 * Attempts to initialize the ConfigurationManager with the supplied configuration name.  This method should only be called once per instance.
	 * 
	 * @param config The configuration set to initialize with.
	 * @return <code>true</code> if the configuration file exists; <code>false</code> if a new file had to be created.
	 * @throws IOException If the supplied file cannot be read/created.
	 * 
	 * @since 1.0
	 */
	@SuppressWarnings(value={"unchecked"})
	public final boolean initialize(String config) throws IOException {
		// Java specs guarantee that this will return a valid folder.
		// The goal is to put this file in ~/.imp on a UNIX machine, and ...somewhere legal on Windows.
		String configPath = (String)System.getProperty("user.home") + 
					(String)System.getProperty("file.separator") + ".imp" + 
					(String)System.getProperty("file.separator");
		new File(configPath).mkdirs();
		
		if (config == null)
			configFile = new File(configPath + "default.imp");
		else
			configFile = new File(configPath + config.trim().toLowerCase() + ".imp");
		if (configFile.isFile()) {
			if (configFile.canRead()) {
				// The file exists and can be read.. so let's read it!
				Hashtable<String, String> saved = new Hashtable<String, String>();
				ObjectInputStream inStr = new ObjectInputStream(new FileInputStream(configFile));
				try {
					saved = (Hashtable<String, String>)inStr.readObject();
					objects = (Hashtable<String, Object>)inStr.readObject();
				}
				catch (ClassNotFoundException e) {
					if (configFile.delete())
						throw new IOException("Configuration Manager: The configuration file is corrupt.  It has been deleted.  Please try again.");
					else
						throw new IOException("Configuration Manager: The configuration file is corrupt, and could not be deleted.  Please try again with a different configuration name.");
				}
				catch (NotSerializableException e) {
					System.out.println("Your configuration file is corrupt, but some data was able to be salvaged.  Some of your preferences and saved data may have been lost.");
				}
				processConfiguration(saved);
				initialized = true;
				return true;
			}
			else
				throw new IOException("Configuration Manager: Configuration file exists, but cannot be read!  Please check your permissions.");
		}
		else {
			if (configFile.createNewFile() && configFile.canWrite()) {
				// The file exists and we can write to it.. let's do that :)
				processConfiguration(null);
				initialized = true;
				return false;
			}
			else
				throw new IOException("Configuration Manager: New configuration file could not be created and written to.  Please check your permissions for the folder: " + configPath);
		}
	}
	
	
	/**
	 * Provides information on the current status of the ConfigurationManager.  This method should be
	 * called by {@link BotFactory} and confirmed to be true before the object is supplied to any
	 * other class.
	 * 
	 * @return <code>true</code> if the {@link initialize()} method has been called and the object is
	 * 		ready for use; <code>false</code> otherwise.
	 * 
	 * @since 1.0
	 */
	public boolean isInitialized() {
		return initialized;
	}
	
	
	/**
	 * Saves any arbitrary object with the specified reference ID.  Names must be unique, as all modules
	 * share this resource as a means of saving data.  Objects saved through this method are saved permanently
	 * until the objects are explicity removed with the {@link removeObject(String)} method, and so each
	 * module must be responsible for the data it saves.
	 * <p>
	 * The saved object is saved within the main configuration file being used, and as such, will last no matter
	 * how many times the bot is restarted.  Objects can be retrieved with the {@link loadObject(String)} command.
	 * 
	 * @param id A unique, case-sensitive ID that can be usedd later to retrieve or delete the supplied object.
	 * @param toSave the Object to be saved.
	 */
	public void saveObject(String id, Object toSave) {
		objects.put(id, toSave);
		saveConfig();
	}
	
	
	/**
	 * Retrieves the saved object with the specified ID.
	 * 
	 * @param id The ID of the Object to be loaded.
	 * @return the requested Object if it exists; <code>null</code> otherwise.
	 */
	public Object loadObject(String id) {
		return objects.get(id);
	}
	
	
	/**
	 * Removes the Object with the specified ID from the saved file permanently.
	 * 
	 * @param id The ID of the Object to be removed.
	 * @return the removed Object if it exists; <code>null</code> otherwise.
	 */
	public Object removeObject(String id) {
		return objects.remove(id);
	}
	
	
	/**
	 * Saves the configuration file that the ConfigurationManager was initialized with.
	 *
	 * @since 1.0
	 */
	public synchronized void saveConfig() {
		if (initialized) {
			Hashtable<String, String> toSave = new Hashtable<String, String>();
			for (int i = 0; i < items.size(); i++)
				toSave.put(items.get(i).getName(), items.get(i).getValue());
			
			try {
				ObjectOutputStream outStr = new ObjectOutputStream(new FileOutputStream(configFile));
				outStr.writeObject(toSave);
				outStr.writeObject(objects);
			}
			catch (IOException e) {
				System.err.println("Configuration error: " + e.getMessage() + "  Config file not saved.");
			}
		}
	}
	
	/**
	 * Gets the value of the configuration item with the supplied name.
	 * 
	 * @param name The name of the configuration item to read.
	 * @return The value of the named configuration item.
	 * @since 1.0
	 */
	public String getValue(String name) {
		if (nums.containsKey(name))
			return items.get(nums.get(name)).getValue();
		return null;
	}
	
	/**
	 * Gets the configuration item of the specified name.  The item returned is mutable, so
	 * any changes made to it will also be made internally in the ConfigurationManager.
	 * 
	 * @param name The name of the configuration item to retrieve.
	 * @return The speicifed configuration item.  If no item by the given name exists,
	 * 			<code>null</code> is returned.
	 */
	public ConfigurationItem getConfigurationItem(String name) {
		if (nums.containsKey(name))
			return items.get(nums.get(name));
		return null;
	}
	
	/**
	 * Gets the current list of configuration items with their most recent values.  This list
	 * is mutable, so any changes made to its elements will also be made internally in the
	 * ConfigurationManager.
	 * 
	 * @return an ArrayList of ConfigurationItems.
	 * @since 1.0
	 */
	public ArrayList<ConfigurationItem> getItemList() {
		return items;
	}

	/**
	 * Either builds up a new configuration file with all of the defaults, or takes an old configuration file
	 * and populates the ConfigurationManager with its saved settings.
	 *
	 * @param saved The contents of the saved configuration file to load.  If a new file is to be made, this
	 * should be <code>null</code>.
	 * @since 1.0
	 */
	private void processConfiguration(Hashtable<String, String> saved) {
		ConfigurationItem[] moduleItems = BotFactory.getModuleManager().getConfigurationItems();
		items = new ArrayList<ConfigurationItem>();
		String value;
		for (int i = 0; i < configurationItems.length; i++) {
			if (saved != null) {
				value = saved.get(configurationItems[i].getName());
				if (value != null)
					configurationItems[i].setValue(value);
			}
			items.add(configurationItems[i]);
			nums.put(configurationItems[i].getName(), i);
		}
		for (int i = 0; i < moduleItems.length; i++) {
			if (saved != null) {
				value = saved.get(moduleItems[i].getName());
				if (value != null)
					moduleItems[i].setValue(value);
			}
			if (!nums.containsKey(moduleItems[i].getName())) {
				items.add(moduleItems[i]);
				nums.put(moduleItems[i].getName(), items.size() - 1);
			}
			else
				throw new ModuleParseException("Module attempted to overwrite existing Configuration Item: " 
						+ moduleItems[i].getName() + ".  Skipped.");
		}
		saveConfig();
	}
}

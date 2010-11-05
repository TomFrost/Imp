/**
 * ModuleData.java
 * Tom Frost
 * August 11, 2006
 * 
 * Compiles all the data needed in the module manager.
 */
package com.seriouslyslick.imp;

/**
 * The ModuleData class is one self-contained unit which holds all of the necessary information associated
 * with one Imp module.  It provides access to every aspect of the module, as well as author information.
 * 
 * @author Tom Frost
 * @version 1.2
 */
public class ModuleData {
	
	private String name, author, about;
	private float version;
	private String[] requiredMods;
	private float[] requiredModVersions;
	private Command[] commands;

	/**
	 * Constructs a data package that defines the Imp module in question.
	 * 
	 * @param name The name of this module.  Cannot contain newlines, tabs, etc.  Alphanumeric characters only.
	 * @param version The version of this module.  Must be greater than 0.
	 * @param author The module author's name.
	 * @param about A brief description of what this mod does.
	 * @param requiredMods An array of mods that must be loaded before this mod.
	 * @param requiredModVersions An array of required minimum versions to go with the required mods.
	 * @param commands an array of {@link Command} objects associated with this module.
	 * @throws IllegalArgumentException if any of the above argument limitations are violated.
	 */
	public ModuleData(String name, float version, String author, String about,
			String[] requiredMods, float[] requiredModVersions,
			Command[] commands) throws IllegalArgumentException {
		if ((this.name = name).length() == 0)
			throw new IllegalArgumentException("ModuleData: Modules must have names!");
		if (!name.matches("(\\w|\\s)*"))
			throw new IllegalArgumentException("ModuleData: Illegal characters in name: " + name);
		if ((this.version = version) <= 0)
			throw new IllegalArgumentException("ModuleData: Version number must be greater than 0 for module " + name);
		if (!(this.author = author).matches("(\\w|\\s)*"))
			throw new IllegalArgumentException("ModuleData: Illegal characters in author name: " + author);
		if ((this.about = about).length() == 0)
			throw new IllegalArgumentException("ModuleData: Module " + name + " needs an 'about' segment!");
		this.requiredMods = requiredMods;
		if ((this.requiredModVersions = requiredModVersions).length != requiredMods.length)
			throw new IllegalArgumentException("ModuleData: All required mods must have a corresponding minimum required version for mod: " + name);
		this.commands = commands;
	}

	public String getName() {
		return name;
	}
	public String getAuthor() {
		return author;
	}
	public String getAbout() {
		return about;
	}
	public float getVersion() {
		return version;
	}
	public String[] getRequiredMods() {
		return requiredMods;
	}
	public Command[] getCommands() {
		return commands;
	}
	public float[] getRequiredModVersions() {
		return requiredModVersions;
	}
}

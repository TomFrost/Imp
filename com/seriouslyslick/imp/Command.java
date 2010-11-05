/**
 * Command.java
 * Tom Frost
 * Sep 12, 2006
 *
 * The command class!
 */
package com.seriouslyslick.imp;


/**
 * The command class is the standard unit for implementing a command within an Imp module.
 * <p>
 * Commands were not intended to be defined in a separate class file and then referred to from within
 * the associated module class, regardless of the abstract method here.  That would defeat the purpose of the
 * command, because then the command would have no interaction with the API that the module might supply. Rather,
 * use inline definitions, like this:
 * <p>
 * <code>
 * new Command("demo", 'p', 'b', "Just a demo command. It makes the bot speak whatever text you supply!") {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;public void run(String channel, String sender, String login, String hostname, String request) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sendMessage(channel, request);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br>
 * };
 * </code>
 * <p>
 * Viola!
 * 
 * @author Tom Frost
 * @since 1.0
 * @version 1.0
 */
public abstract class Command implements ConfigurationConstants {
	private final String command, help;
	private final char method;
	private int level = 0;
	private char access;
	
	/**
	 * Constructs a command to be managed by a Module.
	 * 
	 * @param command The name of the command.
	 * @param access A character representing the access level ('p'ublic, 'v'oiced user, 'h'alf-op, 'o'perator, 'a'dministrator, 'q' (channel owner) or 'm'aster) that denotes who can use this command.
	 * @param method A character ('s'poken, 'p'rivate message, 'b'oth) denoting how this command is allowed to be invoked.
	 * @param help A string giving very a brief desciption of how to use this command.
	 * @throws IllegalArgumentException if any of the above argument limitations are violated.
	 */
	public Command(String command, char access, char method, String help) throws IllegalArgumentException {
		// Checks
		if (command == null || !command.matches("(\\w|\\d)*"))
			throw new IllegalArgumentException("Illegal command name.");
		if ("pvhoaqm".indexOf(access) == -1)
			throw new IllegalArgumentException("Command access for command " + command + 
					"can only be one of these characters: 'p', 'v', 'h', 'o', 'a', 'q', or 'm'.");
		if ("bsp".indexOf(method) == -1)
			throw new IllegalArgumentException("Command method for command " + command +
					"can only be one of these characters: 'b', 's', or 'p'.");
		if (help == null || help.trim().equals(""))
			throw new IllegalArgumentException("Help field for command " + command + " cannot be blank!");
		
		// Assignments
		this.command = command.toLowerCase();
		this.access = access;
		this.method = method;
		this.help = help;
		switch (access) {
		case 'm': level++;
		case 'q': level++;
		case 'a': level++;
		case 'o': level++;
		case 'h': level++;
		case 'v': level++;
		}
	}
	public final String getName() { return command; }
	public final char getAccess() { return access; }
	public final int getCommandLevel() { return level; }
	public final char getMethod() { return method; }
	public final String getHelp() { return help; }
	
	/**
	 * When the command is executed in the appropriate way by someone with the appropriate permissions, this
	 * method is called to execute the command.  It is to be overridden to take the appropriate action.
	 * 
	 * @param channel The channel the command was spoken in, or, if the command was sent privately, the name of the sender.
	 * @param sender The user who spoke or messaged the command.
	 * @param login The login of the command executor.
	 * @param hostname The hostname of the command executor.
	 * @param request The arguments supplied with the command. (i.e. if a user called the command '!info Information', 'Information' would be the contents of request.)
	 */
	public abstract void run(String channel, String sender, String login, String hostname, String request);
	
	/**
	 * Sets a new access level for this command.
	 * 
	 * @param access A character representing the access level ('p'ublic, 'v'oiced user, 'h'alf-op, 'o'perator, 'a'dministrator, 'q' (channel owner) or 'm'aster) that denotes who can use this command.
	 */
	public void setAccess(char access) {
		if ("pvhoaqm".indexOf(access) == -1)
			throw new IllegalArgumentException("Command access for command " + command + 
					"can only be one of these characters: 'p', 'v', 'h', 'o', 'a', 'q', or 'm'.");
		this.access = access;
		level = 0;
		switch (access) {
		case 'm': level++;
		case 'q': level++;
		case 'a': level++;
		case 'o': level++;
		case 'h': level++;
		case 'v': level++;
		}
	}
	
	/**
	 * @return The name of this module.
	 */
	public final String toString() {
		return getName();
	}
}

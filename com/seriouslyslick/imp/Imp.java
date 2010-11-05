/**
 * Imp.java
 * Tom Frost
 * August 9, 2006
 * 
 * Java bot built from PircBot.  Intended for #impsvillage on
 * irc.rizon.net.
 */
package com.seriouslyslick.imp;

import org.jibble.pircbot.*;
import java.io.IOException;

/**
 * 
 * Imp, post-acronymed the IRC Modularized Processor, is a framework which operates on top of
 * a modified version of PircBot (Paul James Mutton, <a href="http://www.jibble.org/">http://www.jibble.org/</a>).
 * <p>
 * As the name suggest, Imp is modular.  All it takes to put the bot online is to supply a configuration.
 * From there, the modules loaded into Imp do all the work.  The required module {@link UserManager} keeps track
 * of the channel's current users, as well as their statuses and permission levels.  While supplying a few
 * routine commands, {@link UserManager} provides an API that quickly and easily allows other modules to traverse
 * the framework to exchange information from this manager.  In the same way, other modules can list completely
 * different modules as dependencies, and will only be loaded if its dependency has been loaded prior to it.  The
 * module is then able to traverse the framework and interact with that module's API as well.
 * <p>
 * Command permissions and bot security is handled internally in Imp and no security checks are required when
 * developing a module -- making the framework both secure and very easy to develop module units for.  The
 * Configuration sub-framework is also accessible to modules, allowing them to insert configuration items that
 * are to be defined by the user before the bot's initial load.
 * <p>
 * With the assistance of the {@link Information} module, command help is also provided automatically to the
 * users of the bot's IRC channel depending upon their permission level (normal, voiced, half-op, op, owner, etc).
 * <p>
 * Imp is in active development, and module submissions are encouraged. 
 *
 * @author Tom Frost
 * @version 0.87
 */
public class Imp extends PircBot implements ConfigurationConstants {
	
	// Framework managers
	private ModuleManager moduleManager;
	private ConfigurationManager configurationManager;
	
	// Configuration Interface
	private final ConfigurationUI configInterface = new BasicTextUI();
	
	// Loaded modules
	private Module[] modules;

	/**
	 * Creates a basic Imp object.
	 * 
	 * @since 1.0
	 */
	public Imp() {
		super();
	}
	
	/**
	 * Imp's main method, called upon execution.  This should not be called by any other method.
	 * <p>
	 * Imp's first legal argument is a configuration name.  If left blank, it is assumed that the default
	 * configuration file is to be used.  If the specified configuration exists, Imp loads it silently. If not,
	 * Imp launches the text-based configuration interface to let the user customize this configuration.
	 * <p>
	 * Optionally, adding the word "edit" to the end of the command line opens the editor with the loaded
	 * configuration file as well.
	 * 
	 * @param args The configuration file to use.
	 * 
	 * @since 1.0
	 */
	public static void main(String[] args) {
		
		// Get an Imp!
		Imp bot = BotFactory.getBot();
		
		// Initialize the configuration
		boolean configExists = true;
		try {
			if (args.length == 0)
				configExists = BotFactory.initConfigurationManager(null);
			else {
				if (args[0].trim().equalsIgnoreCase("edit")) {
					args[0] = "default";
					args[1] = "edit";
				}
				configExists = BotFactory.initConfigurationManager(args[0]);
			}
		}
		catch (IOException e) {
			System.out.println(e.getMessage() + "\nShutting down.");
			System.exit(1);
		}
		bot.configurationManager = BotFactory.getConfigurationManager();
		if (!configExists)
			bot.configInterface.start(bot.configurationManager); // Open up the configuration editor
		else if (args.length >= 2 && args[1].trim().equalsIgnoreCase("edit"))
			bot.configInterface.start(bot.configurationManager);
		
		// Initialize the ModuleManager object
		BotFactory.initModuleManager();
		bot.moduleManager = BotFactory.getModuleManager();
		bot.modules = bot.moduleManager.getModules();
		
		// Populate the bot with configuration data
        bot.setVerbose(Boolean.valueOf(bot.configurationManager.getValue(CONF_DISPLAYVERBOSE)));
        bot.setAutoNickChange(Boolean.valueOf(bot.configurationManager.getValue(CONF_AUTONICKCHANGE)));
        bot.setName(bot.configurationManager.getValue(CONF_BOTNICK));
        bot.setLogin(bot.configurationManager.getValue(CONF_BOTLOGIN));
        bot.setFinger(bot.configurationManager.getValue(CONF_FINGERREPLY));
        bot.setVersion(bot.configurationManager.getValue(CONF_VERSIONREPLY));
        bot.setMessageDelay(Integer.parseInt(bot.configurationManager.getValue(CONF_MESSAGEDELAY)));
        
        // Connect & Join channel
        bot.connectAndJoin();
	}
	
	/**
	 * Connects to the server/port specified in the configuration, and joins the bot's channel.
	 *
	 */
	public void connectAndJoin() {
		String serverPass = configurationManager.getValue(CONF_SERVERPASS).trim();
        String serverAddress = configurationManager.getValue(CONF_SERVERADDRESS);
        int serverPort = Integer.parseInt(configurationManager.getValue(CONF_SERVERPORT));
       	try {
       		if (serverPass.equals(""))
       			connect(serverAddress, serverPort);
       		else
       			connect(serverAddress, serverPort, serverPass);
       		
       		// Join channel
           	joinChannel();
        }
       	catch (IrcException e) {
       		System.err.println("Well, this sucks: " + e.getMessage());
       	}
       	catch (IOException e) {
       		System.err.println("Well, this sucks: " + e.getMessage());
       	}
	}
	
	/**
	 * Joins the bot's channel.
	 *
	 */
	public void joinChannel() {
		String channel = configurationManager.getValue(CONF_CHANNEL);
       	String channelPass = configurationManager.getValue(CONF_CHANNELPASS).trim();
       	if (!channelPass.equals(""))
       		joinChannel(channel, channelPass);
       	else
       		joinChannel(channel);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onConnect() {
		for (int i = 0; i < modules.length; i++)
			modules[i].onConnect();
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate message-listening modules.
	 */
	public void onAction(String sender, String login, String hostname, String target, String action) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onAction(sender, login, hostname, target, action);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onBanList(String channel, String[] banlist) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onBanList(channel, banlist);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onDeop(channel, sourceNick, sourceLogin, sourceHostname, recipient);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onDehop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onDehop(channel, sourceNick, sourceLogin, sourceHostname, recipient);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onDevoice(channel, sourceNick, sourceLogin, sourceHostname, recipient);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onDisconnect() {
		for (int i = 0; i < modules.length; i++)
			modules[i].onDisconnect();
		
		// Automatically reconnect if specified
		String auto = configurationManager.getValue(CONF_AUTORECONNECT);
		if (auto.toLowerCase().equals("true"))
			connectAndJoin();
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onFinger(sourceNick, sourceLogin, sourceHostname, target);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onInvite(targetNick, sourceNick, sourceLogin, sourceHostname, channel);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onJoin(String channel, String sender, String login, String hostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onJoin(channel, sender, login, hostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
		
		// Automatically rejoin if specified
		if (recipientNick.equals(this.getNick()) && configurationManager.getValue(CONF_AUTOREJOIN).toLowerCase().equals("true"))
			joinChannel();
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the ModuleManager object through
	 * the {@link ModuleManager#messageHook(String, String, String, String, String, boolean)} method.
	 */
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		moduleManager.messageHook(channel, sender, login, hostname, message, false);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onMode(channel, sourceNick, sourceLogin, sourceHostname, mode);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onNickChange(String oldNick, String login, String hostname, String newNick) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onNickChange(oldNick, login, hostname, newNick);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onNotice(sourceNick, sourceLogin, sourceHostname, target, notice);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onOp(channel, sourceNick, sourceLogin, sourceHostname, recipient);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onHop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onHop(channel, sourceNick, sourceLogin, sourceHostname, recipient);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onPart(String channel, String sender, String login, String hostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onPart(channel, sender, login, hostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onPing(sourceNick, sourceLogin, sourceHostname, target, pingValue);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the ModuleManager object through
	 * the {@link ModuleManager#messageHook(String, String, String, String, String, boolean)} method.
	 */
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		moduleManager.messageHook(sender, sender, login, hostname, message, true);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onQuit(sourceNick, sourceLogin, sourceHostname, reason);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onRemoveChannelBan(channel, sourceNick, sourceLogin, sourceHostname, hostmask);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onRemoveChannelKey(channel, sourceNick, sourceLogin, sourceHostname, key);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onRemoveChannelLimit(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onRemoveInviteOnly(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onRemoveModerated(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onRemoveNoExternalMessages(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onRemovePrivate(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onRemoveSecret(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onRemoveTopicProtection(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onSetChannelBan(channel, sourceNick, sourceLogin, sourceHostname, hostmask);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onSetChannelKey(channel, sourceNick, sourceLogin, sourceHostname, key);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onSetChannelLimit(channel, sourceNick, sourceLogin, sourceHostname, limit);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onSetInviteOnly(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onSetModerated(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onSetNoExternalMessages(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onSetPrivate(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onSetSecret(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onSetTopicProtection(channel, sourceNick, sourceLogin, sourceHostname);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onTopic(channel, topic, setBy, date, changed);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onUserList(String channel, User[] users) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onUserList(channel, users);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onUserMode(targetNick, sourceNick, sourceLogin, sourceHostname, mode);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		sendNotice(sourceNick, " VERSION " + getVersion());
		for (int i = 0; i < modules.length; i++)
			modules[i].onVersion(sourceNick, sourceLogin, sourceHostname, target);
	}
	
	/**
	 * Called by the PircBot interface.  Notifies the appropriate event-listening modules.
	 */
	public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		for (int i = 0; i < modules.length; i++)
			modules[i].onVoice(channel, sourceNick, sourceLogin, sourceHostname, recipient);
	}
	
	/**
	 * Sends the supplied message to the appropriate target (channel or user).  If the message
	 * is longer than the maximum legal length of IRC messages, the extra is word-wrapped to
	 * another message.  Message breaks will also appear at line breaks ('\n') in the message
	 * String.
	 * 
	 * @param target The target, channel or user, to send the message to.
	 * @param message The message to send to the target.
	 */
	public void sendMessage(String target, String message) {
		while (message.startsWith("\n"))
			message = message.substring(1);
		while (message.endsWith("\n"))
			message = message.substring(0, message.length() - 1);
		while (message.length() > getMaxLineLength() || message.indexOf('\n') > -1) {
			int cut = theLesserPositive(message.indexOf('\n'), 
					message.lastIndexOf(' ', getMaxLineLength()));
			super.sendMessage(target, message.substring(0, cut));
			message = message.substring(cut + 1);
		}
		super.sendMessage(target, message);
	}
	
	/**
	 * Sends the supplied action to the appropriate target (channel or user).  If the message
	 * is longer than the maximum legal length of IRC messages, the extra is word-wrapped to
	 * another message.  Message breaks will also appear at line breaks ('\n') in the message
	 * String.
	 * 
	 * @param target The target, channel or user, to send the action message to.
	 * @param message The action message to send to the target.
	 */
	public void sendAction(String target, String message) {
		while (message.startsWith("\n"))
			message = message.substring(1);
		while (message.endsWith("\n"))
			message = message.substring(0, message.length() - 1);
		while (message.length() > getMaxLineLength() || message.indexOf('\n') > -1) {
			int cut = theLesserPositive(message.indexOf('\n'), 
					message.lastIndexOf(' ', getMaxLineLength()));
			super.sendAction(target, message.substring(0, cut));
			message = message.substring(cut + 1);
		}
		super.sendAction(target, message);
	}
	
	private int theLesserPositive(int a, int b) {
		int min = Math.min(a, b);
		int max = Math.max(a, b);
		if (min < 0) {
			if (max >= 0)
				return max;
			return -1;
		}
		return min;
	}
}

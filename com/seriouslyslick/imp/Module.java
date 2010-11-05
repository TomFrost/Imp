/**
 * Module.java
 * Tom Frost
 * August 11, 2006
 * 
 * The abstract for creating a module.  Please see the documentation for
 * ModuleData for instructions on how to properly construct the data unit,
 * or your module will not work!
 */
package com.seriouslyslick.imp;

import org.jibble.pircbot.User;


/**
 * The abstract Module class is the basic building block for IRC modules.  The only required method in
 * a Module is the {@link #getModuleData()} method, which returns a {@link ModuleData} object that defines the
 * entire module and organizes its associated information in a way that can be read and shared by the
 * {@link ModuleManager} class.  Any other of the methods here can be overridden, allowing the module to react
 * to that specific event.
 * 
 * @author Tom Frost
 * @since 1.0
 * @version 1.0
 */
public abstract class Module implements ConfigurationConstants {
	
	// Some friendly shortcuts to make our Modules cleaner
	protected final Imp BOT = BotFactory.getBot();
	protected final ConfigurationManager CONFIG = BotFactory.getConfigurationManager();
	protected final String PREFIX = CONFIG.getValue(CONF_COMMANDPREFIX);
	
	/**
	 * Gets a {@link ModuleData} object that defines this Module.
	 * 
	 * @return the associated {@link ModuleData} object.
	 * @see ModuleData
	 */
	public abstract ModuleData getModuleData();
	
	/**
	 * Called before the Module receives any server data or commands.  Override this method to
	 * execute any necessary initialization code that would otherwise be done in a constructor.
	 * Using a constructor in a module will almost always result in error.
	 */
	public void initialize() {}
	
	/**
	 * Gets an array of configuration items that this module is requesting to add to the main bot
	 * configuration.  Identification names cannot be duplicated.  The current value of any
	 * configuration item submitted with this method can be retrieved by using the usual
	 * CONFIG.getValue(String) method.
	 * 
	 * @return an array of ConfigurationItem objects to add to the bot's main configuration.
	 */
	public ConfigurationItem[] getConfigurationItems() { return null; }
	
	public void onAction(String sender, String login, String hostname, String target, String action) {}
	public void onAdmin(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}
	public void onConnect() {}
	public void onCommand(String channel, String sender, String login, String hostname, String command, String params, boolean sentPrivate) {}
	public void onBanList(String channel, String[] banlist) {}
	public void onDeadmin(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}
	public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}
	public void onDehop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}
	public void onDeowner(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}
	public void onDevoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}
	public void onDisconnect() {}
	public void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {}
	public void onHop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}
	public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {}
	public void onJoin(String channel, String sender, String login, String hostname) {}
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {}
	public void onMessage(String channel, String sender, String login, String hostname, String message) {}
	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {}
	public void onNickChange(String oldNick, String login, String hostname, String newNick) {}
	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {}
	public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}
	public void onOwner(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}
	public void onPart(String channel, String sender, String login, String hostname) {}
	public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {}
	public void onPrivateMessage(String sender, String login, String hostname, String message) {}
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {}
	public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {}
	public void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {}
	public void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {}
	public void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {}
	public void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {}
	public void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {}
	public void onUserList(String channel, User[] users) {}
	public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {}
	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {}
	public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}
	
	/**
	 * Sends the supplied message to the appropriate target (channel or user).  If the message
	 * is longer than the maximum legal length of IRC messages, the extra is word-wrapped to
	 * another message.  Message breaks will also appear at line breaks ('\n') in the message
	 * String.
	 * 
	 * @param target The target, channel or user, to send the message to.
	 * @param message The message to send to the target.
	 */
	public final void sendMessage(String target, String message) {
		BotFactory.getBot().sendMessage(target, message);
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
	public final void sendAction(String target, String message) {
		BotFactory.getBot().sendAction(target, message);
	}
}

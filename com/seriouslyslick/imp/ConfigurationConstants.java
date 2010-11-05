/**
 * ConfigurationConstants.java
 * Tom Frost
 * Sep 21, 2006
 *
 * 
 */
package com.seriouslyslick.imp;


/**
 * This is an interface to hold the constants that make communication with {@link ConfigurationManager} a
 * whole lot easier.
 * 
 * @author Tom Frost
 * @since 1.0
 * @version 1.0
 */
public interface ConfigurationConstants {
	public final String CONF_BOTNICK = "Bot Nickname";
	public final String CONF_AUTONICKCHANGE = "Auto nick change";
	public final String CONF_BOTLOGIN = "Bot Login";
	public final String CONF_FINGERREPLY = "FINGER reply";
	public final String CONF_VERSIONREPLY = "VERSION reply";
	public final String CONF_SERVERADDRESS = "Server address";
	public final String CONF_SERVERPORT = "Server port";
	public final String CONF_SERVERPASS = "Server password";
	public final String CONF_CHANNEL = "Channel";
	public final String CONF_CHANNELPASS = "Channel password";
	public final String CONF_COMMANDPREFIX = "Command prefix";
	public final String CONF_DISPLAYVERBOSE = "Display verbose";
	public final String CONF_MESSAGEDELAY = "Message delay";
	public final String CONF_MASTERNICK = "Master nickname";
	public final String CONF_AUTORECONNECT = "Auto-reconnect";
	public final String CONF_AUTOREJOIN = "Auto-rejoin";
	public final String CONF_MASTERBYHOST = "Master by host";
	public final String CONF_MASTERPASS = "Master password";
	public final String CONF_ALLOWADMIN = "Allow admin commands";
	public final String CONF_ALLOWOWNER = "Allow owner commands";
}

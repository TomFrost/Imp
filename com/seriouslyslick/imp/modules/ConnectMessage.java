/**
 * ConnectMessage.java
 * Tom Frost
 * April 4, 2007
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.Command;
import com.seriouslyslick.imp.ConfigurationItem;
import com.seriouslyslick.imp.Module;
import com.seriouslyslick.imp.ModuleData;

/**
 * Lets the bot give an arrival announcement when he joins his channel for the first time!
 * 
 * @author Tom Frost
 */
public class ConnectMessage extends Module {
	
	// Configuration items
	private final String CONF_LOGINACTION = "Bot login action";
	private final String CONF_LOGINMSG = "Bot login message";
	
	// Basic variables
	private boolean loggingIn = true;

	public ConfigurationItem[] getConfigurationItems() {
		return new ConfigurationItem[] {
				new ConfigurationItem(CONF_LOGINACTION, "The emote to execute when the bot logs into the server and joins the channel.  Leave blank for no action.", "has entered the building.") {
					public boolean isLegal(String val) {
						return true;
					}
				},
				new ConfigurationItem(CONF_LOGINMSG, "The message to speak when the bot logs into the server and joins the channel.  Leave blank for no message.  This will be sent after the action, if an action is specified.", "") {
					public boolean isLegal(String val) {
						return true;
					}
				}
		};
	}
	public ModuleData getModuleData() {
		return new ModuleData("Connect Message", (float)1.0, "Tom Frost",
				"Lets the bot give an arrival announcement when he joins his channel for the first time!", 
				new String[0], new float[0], new Command[] {});
	}
	
	public void onConnect() {
		loggingIn = true;
	}
	
	public void onJoin(String channel, String sender, String login, String hostname) {
		if (loggingIn && sender.equals(BOT.getNick())) {
			loggingIn = false;
			if (!CONFIG.getValue(CONF_LOGINACTION).trim().equals(""))
				sendAction(channel, CONFIG.getValue(CONF_LOGINACTION));
			if (!CONFIG.getValue(CONF_LOGINMSG).trim().equals(""))
				sendMessage(channel, CONFIG.getValue(CONF_LOGINMSG));
		}
	}
}

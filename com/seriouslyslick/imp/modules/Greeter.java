/**
 * Greeter.java
 * Tom Frost
 * June 11, 2007
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.Command;
import com.seriouslyslick.imp.ConfigurationItem;
import com.seriouslyslick.imp.Module;
import com.seriouslyslick.imp.ModuleData;

/**
 * @author Tom Frost
 * 
 * Ok, I'll admit it.  I'm writing this entirely for personal reasons.  My IRC channel changed servers, and I'm
 * writing this specifically for informing people of the change when they join the old server.  I intend to make
 * this more featureful in the future, but for now, I'm only putting in the bare minimum.  It's 2:15am and I
 * want this done tonight.
 *
 */
public class Greeter extends Module {
	
	// Configuration items
	private final String CONF_GREETMSG = "Greeting message";
	private final String CONF_GREETPRIVMSG = "Send private greet";
	
	public ConfigurationItem[] getConfigurationItems() {
		return new ConfigurationItem[] {
				new ConfigurationItem(CONF_GREETMSG, "The message to greet new channel joiners with.  Use NICKNAME in place of the user's nickname.  Leave blank for no greeting.", "") {
					public boolean isLegal(String val) {
						return true;
					}
				},
				new ConfigurationItem(CONF_GREETPRIVMSG, "If true, the greeting message will be private /msg'd to the recipient.  If false, the bot will say the greeting in the channel.", "false") {
					public boolean isLegal(String val) {
						return val.matches("(?i)^(true|false)$");
					}
				}
		};
	}

	public ModuleData getModuleData() {
		return new ModuleData("Greeter", (float)0.1, "Tom Frost", "Greets users as they arrive.", 
				new String[0], new float[0], new Command[0]);
	}
	
	public void onJoin(String channel, String sender, String login, String hostname) {
		if (!sender.equals(BOT.getNick())) {
			String greeting = CONFIG.getValue(CONF_GREETMSG).replaceAll("NICKNAME", sender);
			if (!greeting.trim().equals("")) {
				if (CONFIG.getValue(CONF_GREETPRIVMSG).equalsIgnoreCase("true"))
					sendMessage(sender, greeting);
				else
					sendMessage(channel, greeting);
			}
		}
	}
}

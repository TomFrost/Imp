/**
 * NickServ.java
 * Tom Frost
 * April 3, 2007
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.Command;
import com.seriouslyslick.imp.ConfigurationItem;
import com.seriouslyslick.imp.Module;
import com.seriouslyslick.imp.ModuleData;

/**
 * The NickServ module takes care of various tasks on NickServ-enabled IRC servers, such as
 * identifying the Bot's nickname upon login and automatically ghosting users upon login when the
 * Bot's nickname isn't available.
 * 
 * @author Tom Frost
 */
public class NickServ extends Module {
	
	private final String CONF_NICKSERVPASS = "NickServ password";
	private final String CONF_NSAUTOGHOST = "NickServ auto-ghost";
	
	private boolean startingUp = true;
	private boolean takeName = false;
	
	public ConfigurationItem[] getConfigurationItems() {
		return new ConfigurationItem[] {
			new ConfigurationItem(CONF_NICKSERVPASS, "The password to use when sending the IDENTIFY command to the NickServ bot.  Leave blank to not identify.", "") {
				public boolean isLegal(String val) {
					return true;
				}
			},
			new ConfigurationItem(CONF_NSAUTOGHOST, "When on, the bot will automatically disconnect users that take the bot's registered nickname.", "on") {
				public boolean isLegal(String val) {
					return val.matches("(?i)(on|off)");
				}
			}
		};
	}

	public ModuleData getModuleData() {
		return new ModuleData("NickServ", (float)0.5, "Tom Frost",
				"The NickServ module takes care of various tasks on NickServ-enabled IRC servers, such as " +
				"identifying the Bot's nickname upon login and automatically ghosting users upon login when " +
				"the Bot's nickname isn't available.",
				new String[0], new float[0], new Command[0]);
	}
	
	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		/*if (sourceNick.equalsIgnoreCase("nickserv") && !CONFIG.getValue(CONF_NICKSERVPASS).equals("")
				&& BOT.getNick().equals(CONFIG.getValue(CONF_BOTNICK)) && notice.indexOf("IDENTIFY password") >= 0)
			sendMessage(sourceNick, "IDENTIFY " + CONFIG.getValue(CONF_NICKSERVPASS));*/
		if (sourceNick.equalsIgnoreCase("nickserv")) {
			if (!CONFIG.getValue(CONF_NICKSERVPASS).equals("")) {
				if (BOT.getNick().equals(CONFIG.getValue(CONF_BOTNICK))) {
					if (notice.matches("(?i).*msg.*NickServ.*IDENTIFY.*password.*")) {
						sendMessage(sourceNick, "IDENTIFY " + CONFIG.getValue(CONF_NICKSERVPASS));
					}
				}
			}
		}
	}
	
	public void onNickChange(String oldNick, String login, String hostname, String newNick) {
		// If someone is switching their name to our name... Who cares if we're not using it?
		// ROAST 'IM.
		if (CONFIG.getValue(CONF_NSAUTOGHOST).equalsIgnoreCase("on")
				&& !BOT.getNick().equals(CONFIG.getValue(CONF_BOTNICK))
				&& newNick.equalsIgnoreCase(CONFIG.getValue(CONF_BOTNICK))
				&& !CONFIG.getValue(CONF_NICKSERVPASS).equals(""))
			ghostNick(CONFIG.getValue(CONF_BOTNICK), CONFIG.getValue(CONF_NICKSERVPASS));
		
		// Or hey, maybe someone is changing their name AWAY from ours.  And maybe we want it!
		if (oldNick.equalsIgnoreCase(CONFIG.getValue(CONF_BOTNICK)) && takeName) {
			BOT.changeNick(CONFIG.getValue(CONF_BOTNICK));
			takeName = false;
		}
	}
	
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		// Is someone quitting who has our name?  And do we wants it!??!  My preciousssssss...
		if (sourceNick.equalsIgnoreCase(CONFIG.getValue(CONF_BOTNICK))) {
			if (sourceNick.equalsIgnoreCase(BOT.getNick())) {
				// False alarm, it was us.  But let's make a note that we want our name when we come back.
				startingUp = true;
			}
			else if (takeName) {
				// We wants it!!
				BOT.changeNick(CONFIG.getValue(CONF_BOTNICK));
				takeName = false;
			}
		}
	}
	
	public void onDisconnect() {
		// Well.. that sucked.  Oh well.
		startingUp = true;
	}
	
	public void onJoin(String channel, String sender, String login, String hostname) {
		// If someone else is joining the room with our name, well that's just not cool.
		// We'll check to see if the configuration wants us to ghost them first, though.
		if (CONFIG.getValue(CONF_NSAUTOGHOST).equalsIgnoreCase("on")
				&& !BOT.getNick().equals(CONFIG.getValue(CONF_BOTNICK))
				&& sender.equalsIgnoreCase(CONFIG.getValue(CONF_BOTNICK))
				&& !CONFIG.getValue(CONF_NICKSERVPASS).equals(""))
			ghostNick(CONFIG.getValue(CONF_BOTNICK), CONFIG.getValue(CONF_NICKSERVPASS));
		
		// If we're the ones joining, let's just make sure we have our own name.  If not,
		// this guy is OVER.  Assuming we're allowed to ghost, of course.
		if (startingUp && sender.equals(BOT.getNick())) {
			startingUp = false;
			if (!BOT.getNick().equals(CONFIG.getValue(CONF_BOTNICK)))
				takeName = true;
			if (CONFIG.getValue(CONF_NSAUTOGHOST).equalsIgnoreCase("on")
					&& !CONFIG.getValue(CONF_NICKSERVPASS).equals(""))
				ghostNick(CONFIG.getValue(CONF_BOTNICK), CONFIG.getValue(CONF_NICKSERVPASS));
		}
	}
	
	/**
	 * Ghosts the given user with the given password.
	 * 
	 * @param nick The nickname to ghost.
	 * @param pass The password that the given nickname is registered with.
	 */
	public void ghostNick(String nick, String pass) {
		sendMessage("nickserv", "GHOST " + nick + " " + pass);
	}
}

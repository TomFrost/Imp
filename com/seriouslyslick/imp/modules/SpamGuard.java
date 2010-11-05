/**
 * SpamGuard.java
 * Tom Frost
 * March 23, 2007
 * 
 * Offers various types of spam protection.
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.BotFactory;
import com.seriouslyslick.imp.Command;
import com.seriouslyslick.imp.Module;
import com.seriouslyslick.imp.ModuleData;
import com.seriouslyslick.imp.modules.BanManager;
import com.seriouslyslick.imp.modules.UserManager;
import com.seriouslyslick.imp.util.TimeQueue;

import java.util.Hashtable;

/**
 * @author Tom Frost
 *
 */
public class SpamGuard extends Module {
	
	// Repeat tracker class
	private class RepeatTracker {
		private TimeQueue msgQueue = new TimeQueue(4, 120);
		private String lastMsg = "";
		private int repeatCount = 0;
		
		public boolean lastSaid(String msg) {
			if (msg.equals(lastMsg)) {
				repeatCount++;
				if (msgQueue.stamp() && repeatCount >= 3)
					return true;
			}
			else {
				lastMsg = msg;
				repeatCount = 0;
			}
			return false;
		}
	}
	
	// Module statuses
	boolean commandBlock = true;
	boolean generalBlock = true;
	boolean repeatBlock = true;
	
	// Required modules
	UserManager userManager;
	BanManager banManager;
	
	// Data trackers
	Hashtable<String, TimeQueue> commandTracker = new Hashtable<String, TimeQueue>();
	Hashtable<String, TimeQueue> generalTracker = new Hashtable<String, TimeQueue>();
	Hashtable<String, RepeatTracker> repeatTracker = new Hashtable<String, RepeatTracker>();
	Hashtable<String, Integer> offenses = new Hashtable<String, Integer>();
	
	private String spamsetHelp = "Usage: " + PREFIX + "spamset (command|general|repeat) (on|off)\n" + 
		"Turns one of the three types of spam blocking on or off.  Type 'command' blacklists, temp-bans, and kicks " +
		"any user who tries to command-flood the bot.  The 'general' type temp-bans any user that is flooding the " +
		"channel, regardless of their messages.  Finally, the 'repeat' type temp=bans anyone who spams the same " +
		"message repeatedly in the channel.  This is generally more fast-acting than the 'general' type, but both " +
		"can work side-by-side.";
	private String spamstatusHelp = "Usage: " + PREFIX + "spamstatus\nShows the running status of the various spam " +
		"blockers.";
	
	private Command[] commands = new Command[] {
			new Command("spamset", 'a', 'b', spamsetHelp) {
				public void run(String channel, String sender, String login, String hostname, String request) {
					String[] args = request.trim().split(" ");
					if (args.length == 2) {
						boolean set = false;
						boolean legal = false;
						if (args[1].toLowerCase().equals("block")) {
							set = true;
							legal = true;
						}
						else if (args[1].toLowerCase().equals("allow")) {
							set = false;
							legal = true;
						}
						if (legal) {
							String onOff = "off";
							if (set)
								onOff = "on";
							if (args[0].toLowerCase().equals("command")) {
								commandBlock = set;
								sendMessage(channel, "Command-spam blocking is now " + onOff + ".");
								if (!set)
									commandTracker = new Hashtable<String, TimeQueue>();
							}
							else if (args[0].toLowerCase().equals("general")) {
								generalBlock = set;
								sendMessage(channel, "General-spam blocking is now " + onOff + ".");
								if (!set)
									generalTracker = new Hashtable<String, TimeQueue>();
							}
							else if (args[0].toLowerCase().equals("repeat")) {
								repeatBlock = set;
								sendMessage(channel, "Repeat-spam blocking is now " + onOff + ".");
								if (!set)
									repeatTracker = new Hashtable<String, RepeatTracker>();
							}
							else
								sendMessage(channel, "Sorry, I don't know that spam channel.  Please use either " +
										"'command', 'general', or 'repeat'.");
						}
						else
							sendMessage(channel, "Spam channels can only be set to 'block' or 'allow'.");
					}
					else
						sendMessage(channel, "Syntax error!  Type '!help spamblock' for assistance.");
				}
			},
			new Command("spamstatus", 'h', 'b', spamstatusHelp) {
				public void run(String channel, String sender, String login, String hostname, String request) {
					String[] types = new String[3];
					if (commandBlock)
						types[0] = "blocked";
					else
						types[0] = "allowed";
					if (generalBlock)
						types[1] = "blocked";
					else
						types[1] = "allowed";
					if (repeatBlock)
						types[2] = "blocked";
					else
						types[2] = "allowed";
					sendMessage(channel, "[Command spam: " + types[0] + "]  [General spam: " + types[1] + "]  " +
							"[Repeat spam: " + types[2] + "]");
				}
			}
	};

	public ModuleData getModuleData() {
		return new ModuleData("Spam Guard", (float)0.1, "Tom Frost",
				"Spam Guard offers various types of spam protection.  Specifically, it can protect against command spam, " +
				"general channel spam, and repetitive message spam.",
				new String[] {"User Manager", "Ban Manager"}, new float[] {(float)2.2, (float)1.2}, commands);
	}
	public void initialize() {
		userManager = (UserManager)BotFactory.getModuleManager().getModule("User Manager");
		banManager = (BanManager)BotFactory.getModuleManager().getModule("Ban Manager");
	}
	public void onCommand(String channel, String sender, String login, String hostname, String command, String params, boolean sentPrivate) {
		if (commandBlock) {
			if (!commandTracker.containsKey(hostname))
				commandTracker.put(hostname, new TimeQueue(3, 5));
			if (commandTracker.get(hostname).stamp()) {
				if (!userManager.isMaster(sender)) {
					int level;
					if (userManager.getUserLevel(sender) < 2)
						level = 2;
					else
						level = userManager.getUserLevel(sender) + 1;
					userManager.addToBlacklist(sender, level);
					punish(sender, hostname, "You tried to spam-flood me and are no longer allowed to use my commands.");
				}
			}
		}
	}
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (generalBlock) {
			if (!generalTracker.containsKey(hostname))
				generalTracker.put(hostname, new TimeQueue(8, 13));
			if (generalTracker.get(hostname).stamp() && !userManager.isMaster(sender))
				punish(sender, hostname, "You're spamming.");
		}
		if (repeatBlock) {
			if (!repeatTracker.containsKey(hostname))
				repeatTracker.put(hostname, new RepeatTracker());
			if (repeatTracker.get(hostname).lastSaid(message) && !userManager.isMaster(sender))
				punish(sender, hostname, "You're repeat-message spamming.");
		}
	}
	public void onAction(String sender, String login, String hostname, String target, String action) {
		onMessage(target, sender, login, hostname, action);
	}
	
	private void punish(String sender, String hostname, String reason) {
		if (!offenses.containsKey(hostname)) {
			offenses.put(hostname, new Integer(1));
			banManager.ban(sender, 5);
			banManager.kick(sender, reason + "  This is your first spam offense.  You are banned " +
					"for 5 minutes.");
		}
		else if (offenses.get(hostname).intValue() == 2) {
			offenses.put(hostname, new Integer(offenses.get(hostname).intValue() + 1));
			banManager.ban(sender, 15);
			banManager.kick(sender, reason + "  This is your second spam offense.  You are banned " +
					"for 15 minutes.");
		}
		else if (offenses.get(hostname).intValue() == 3) {
			offenses.remove(hostname);
			banManager.ban(sender);
			banManager.kick(sender, reason + "  This is your third spam offense, and you are now permabanned.  Goodbye.");
		}
	}
}

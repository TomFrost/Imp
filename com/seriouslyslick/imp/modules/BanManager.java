/**
 * BanManager.java
 * Tom Frost
 * Aug 29, 2006
 *
 * Simple module giving ops access to more fun features :)
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.BotFactory;
import com.seriouslyslick.imp.Command;
import com.seriouslyslick.imp.Module;
import com.seriouslyslick.imp.ModuleData;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Tom Frost
 *
 */
public class BanManager extends Module {
	
	private Hashtable<String, String> bannedUsers = new Hashtable<String, String>();
	private Hashtable<String, Timer> banTimers = new Hashtable<String, Timer>();
	
	private UserManager userManager;
	
	private String banHelp = "Usage: " + PREFIX + "ban [nickname]\nBans any given nickname in the main channel.";
	private String kickHelp = "Usage: " + PREFIX + "kick [nickname] <reason>\nKicks any given nickname in the main channel. Reason optional.";
	private String kickbanHelp = "Usage: " + PREFIX + "kickban [nickname] <reason>\nSimultaneously kicks and bans any given nickname in the main channel.  Reason optional.";
	private String unbanHelp = "Usage: " + PREFIX + "unban [nickname]\nUnbans the given nickname in the main channel.";
	private String banlistHelp = "Usage: " + PREFIX + "banlist\nPrints the list of members banned through the bot.";
	private String unbanallHelp = "Usage: " + PREFIX + "unbanall\nUnbans every person in the ban list.  Use with care.";

	Command[] commands = new Command[] {
		new Command("ban", 'h', 'b', banHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				int index = request.trim().indexOf(' ');
				if (index != -1)
					request = request.trim().substring(0, index);
				if (request != null && !request.equals("") && !illegalAction(sender, request))
					ban(request);
			}
		},
		new Command("unban", 'h', 'b', unbanHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				int index = request.trim().indexOf(' ');
				if (index != -1)
					request = request.trim().substring(0, index);
				if (request != null && !request.equals(""))
					unban(request);
			}
		},
		new Command("unbanall", 'q', 'b', unbanallHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				unbanAll();
				sendMessage(channel, "Mass-unban complete.");
			}
		},
		new Command("kick", 'h', 'b', kickHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String[] args = request.split(" ", 2);
				if (args.length == 0 || args[0] == null || args[0].equals(""))
					return;
				String reason = null;
				if (args.length > 1 && args[1] != null && !args[1].equals(""))
					reason = args[1];
				if (!illegalAction(sender, args[0]))
					kick(args[0], reason);
			}
		},
		new Command("kickban", 'h', 'b', kickbanHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String[] args = request.split(" ", 2);
				if (args.length == 0 || args[0] == null || args[0].equals(""))
					return;
				String reason = null;
				if (args.length > 1 && args[1] != null && !args[1].equals(""))
					reason = args[1];
				if (!illegalAction(sender, args[0])) {
					ban(args[0]);
					kick(args[0], reason);
				}
			}
		},
		new Command("banlist", 'v', 'b', banlistHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				sendMessage(channel, "Banned users: " + getBanlist());
			}
		}
	};
	
	public ModuleData getModuleData() {
		return new ModuleData("Ban Manager", (float)1.2, "Tom Frost", 
				"Implements a system of efficient banning/unbanning/kicking users.",
				new String[] {"User Manager"}, new float[] {(float)2.0}, commands);
	}
	
	public void initialize() {
		userManager = (UserManager)BotFactory.getModuleManager().getModule("User Manager");
	}
	public void onBanList(String channel, String[] banlist) {
		for (int i = 0; i < banlist.length; i++)
			addHostmask(banlist[i]);
	}
	public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		if (!sourceNick.equals(BOT.getNick()))
			addHostmask(hostmask);
	}
	public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		bannedUsers.values().remove(hostmask);
	}
	private void addHostmask(String hostmask) {
		String[] breakdown = hostmask.split("(\\!|\\@)");
		if (breakdown.length == 3) {
			if (!breakdown[0].equals("*"))
				bannedUsers.put(breakdown[0].toLowerCase(), hostmask);
			else if (!breakdown[2].equals("*")) {
				String nick;
				int index = hostmask.indexOf('@');
				if (index != -1 && (nick = userManager.lookUp(hostmask.substring(index + 1))) != null && !nick.equals(""))
					bannedUsers.put(nick.toLowerCase(), hostmask);
				else
					bannedUsers.put(hostmask, hostmask);
			}
			else if (!breakdown[1].equals("*"))
				bannedUsers.put(breakdown[1].toLowerCase(), hostmask);
			else
				bannedUsers.put(hostmask, hostmask);
		}
	}
	
	
	public void ban(String nick) {
		String host, hostmask;
		if ((host = userManager.getHost(nick)) != null)
			hostmask = "*!*@" + host;
		else
			hostmask = nick + "!*@*";
		bannedUsers.put(nick.toLowerCase(), hostmask);
		BOT.setMode(CONFIG.getValue(CONF_CHANNEL), "-e " + nick);
		BOT.ban(CONFIG.getValue(CONF_CHANNEL), hostmask);
	}
	public void ban(String nick, int minutes) {
		final String banNick = nick.trim().toLowerCase();
		ban(banNick);
		Timer unbanTimer = new Timer();
		unbanTimer.schedule(new TimerTask() {
			public void run() {
				if (bannedUsers.containsKey(banNick))
					unban(banNick);
				banTimers.remove(banNick);
			}
				}, minutes * 1000 * 60);
		banTimers.put(banNick, unbanTimer);
	}
	public void unban(String nick) {
		String hostmask;
		if ((hostmask = bannedUsers.get(nick.toLowerCase())) == null)
			BOT.unBan(CONFIG.getValue(CONF_CHANNEL), nick + "!*@*");
		else {
			BOT.unBan(CONFIG.getValue(CONF_CHANNEL), hostmask);
			bannedUsers.remove(nick.toLowerCase());
		}
	}
	@SuppressWarnings(value={"unchecked"})
	public void unbanAll() {
		Iterator<String> unbanList = ((Hashtable<String, String>)bannedUsers.clone()).values().iterator();
		String unbanNext;
		while (unbanList.hasNext()) {
			unbanNext = unbanList.next();
			BOT.unBan(CONFIG.getValue(CONF_CHANNEL), unbanNext);
			bannedUsers.values().remove(unbanNext);
		}
	}
	public void kick(String nick) {
		kick(nick, null);
	}
	public void kick(String nick, String reason) {
		if (reason == null || reason.trim().equals(""))
			BOT.kick(CONFIG.getValue(CONF_CHANNEL), nick);
		else
			BOT.kick(CONFIG.getValue(CONF_CHANNEL), nick, reason);
	}
	
	private String getBanlist() {
		Enumeration<String> banned = bannedUsers.keys();
		String ret = "";
		if (banned.hasMoreElements())
			ret = banned.nextElement();
		while (banned.hasMoreElements())
			ret += ", " + banned.nextElement();
		return ret;
	}
	private boolean illegalAction(String sender, String target) {
		if (target.equalsIgnoreCase(BOT.getNick())) {
			kick(sender, "You're a moron.");
			return true;
		}
		else if (target.equalsIgnoreCase(CONFIG.getValue(CONF_MASTERNICK)) || userManager.isMaster(target)) {
			kick(sender, "I'm not attacking my master.  Don't be a moron.");
			return true;
		}
		return false;
	}
}

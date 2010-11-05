/**
 * UserManager.java
 * Tom Frost
 * August 11, 2006
 * 
 * Manages users in Imp's current channel.
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.*;

import org.jibble.pircbot.User;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * @author Tom Frost
 *
 */
public class UserManager extends Module {

	private Hashtable<String, Integer> commandBlacklist = new Hashtable<String, Integer>();
	private Hashtable<String, User> users = new Hashtable<String, User>();
	private Hashtable<String, User> allUsers = new Hashtable<String, User>();
	private Hashtable<String, String> knownHosts = new Hashtable<String, String>();
	private String masterNick = "";
	private String masterUser = "";
	private String masterHost = "";
	
	private String viewuserHelp = "Usage: " + PREFIX + "viewuser [user]\nGives all the data we have on a certain user.";
	private String findhostHelp = "Usage: " + PREFIX + "findhost [host]\nAttempts to find a nickname that matches the supplied hostname.";
	private String setpassHelp = "Usage: " + PREFIX + "setpass [password]\nSets the password that the owner uses to identify with the bot when not logged on from the bot's hostname.";
	private String blacklistHelp = "Usage: " + PREFIX + "blacklist [user]\nAdds the specified user to the commands blacklist.  Users on the commands blacklist are unable to invoke commands on the bot.";
	private String deblacklistHelp = "Usage: " + PREFIX + "deblacklist [user]\nRemoves the specified user from the commands blacklist.";
	private String viewBlacklistHelp = "Usage: " + PREFIX + "viewblacklist\nDisplays the current command blacklist in heirarchy format.";
	private String masterHelp = "Usage: " + PREFIX + "master <password>\nPassword is optional if bot is configured to match owners based on hostname.  Otherwise, the correct password must be supplied to give the user Master status of this bot!";
	private String blacklistClearHelp = "Usage: " + PREFIX + "blacklist_clear\nClears the blacklist completely.";
	
	private Command[] commands = new Command[] {
		new Command("usertable", 'm', 'b', "Displays the user table.") {
			public void run(String channel, String sender, String login, String hostname, String request) {
				Enumeration<String> enumer = users.keys();
				User temp;
				while (enumer.hasMoreElements()) {
					temp = users.get(enumer.nextElement());
					sendMessage(channel, temp.getPrefix() + temp.getNick() + "!" + 
							temp.getLogin() + "@" + temp.getHostname());
				}
				sendMessage(channel, "End of user table");
			}
		},
		new Command("master", 'p', 'b', masterHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				boolean successful = false;
				if (CONFIG.getValue(CONF_MASTERBYHOST).equalsIgnoreCase("true") 
						&& hostname.equals(getHost(BotFactory.getBot().getNick())))
					successful = true;
				else if (CONFIG.getValue(CONF_MASTERPASS).equals(request))
					successful = true;
				
				if (successful) {
					if (!masterHost.equals(""))
						sendMessage(lookUp(masterHost), "Master status was just usurped by " + lookUp(hostname) + "!");
					masterNick = sender;
					masterUser = login;
					masterHost = hostname;
					sendMessage(channel, "Hi, " + CONFIG.getValue(CONF_MASTERNICK) + "! :D");
				}
				else
					sendMessage(channel, "You're not my master.  Quit playing with this command.");
			}
		},
		new Command("viewuser", 'h', 'b', viewuserHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				if (request != null && !request.equals("")) {
					if (allUsers.containsKey(request.trim().toLowerCase())) {
						User temp = getUser(request.trim().toLowerCase());
						sendMessage(channel, temp.getNick() + " is " + 
								temp.getNick() + "!" + temp.getLogin() + "@" + temp.getHostname());
						sendMessage(channel, "-- Access level: " + getUserLevel(temp.getNick()));
						sendMessage(channel, "-- Prefix: " + temp.getPrefix());
						sendMessage(channel, "-- [In Channel: " + isInChannel(temp.getNick()) + "] [Voice: " + temp.hasVoice() + "] [Half-op: " + temp.isHop() + 
								"] [Op: " + temp.isOp() + "] [Admin: " + isAdmin(temp.getNick()) + "] [Owner: " + isOwner(temp.getNick()) + "] [Master: " + isMaster(temp.getNick()) + "]");
					}
					else
						sendMessage(channel, "Sorry " + sender + ", I've never seen that person!");
				}
				else
					sendMessage(channel, "You forgot to tell me who you want info about!");
			}
		},
		new Command("findhost", 'h', 'b', findhostHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				if (request != null && !request.equals("")) {
					String nick = lookUp(request.trim());
					if (nick != null)
						sendMessage(channel, "I saw " + nick + " on that host!");
					else
						sendMessage(channel, "Sorry, couldn't find anyone with that host.");
				}
				else
					sendMessage(channel, "You forgot to tell me what host you're looking up!");
			}
		},
		new Command("setpass", 'm', 'p', setpassHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				if (request != null && !request.equals("")) {
					ConfigurationItem passItem = CONFIG.getConfigurationItem(CONF_MASTERPASS);
					if (passItem.setValue(request)) {
						CONFIG.saveConfig();
						sendMessage(sender, "Master password set!");
					}
					else
						sendMessage(sender, "Illegal password.  Need some help?  The master password description is: " + 
								passItem.getDesc());
				}
				else
					sendMessage(channel, "You forgot to tell me what password to set!");
			}
		},
		new Command("blacklist", 'h', 'b', blacklistHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				if (request != null && !request.equals("")) {
					request = request.trim();
					if (addToBlacklist(request, getUserLevel(sender)))
						sendMessage(channel, "User '" + request + "' has been added to the command blacklist.");
					else {
						if (getUserLevel(request) >= getUserLevel(sender))
							sendMessage(channel, "You do not have high enough authority to blacklist this user.");
						else
							sendMessage(channel, "I've never seen that user.  I can't add that person to the blacklist " +
									"without having seen them first.");
					}
				}
				else
					sendMessage(channel, "You forgot to tell me who to blacklist!");
			}
		},
		new Command("deblacklist", 'h', 'b', deblacklistHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				if (request != null && !request.equals("")) {
					request = request.trim();
					if (commandBlacklist.containsKey(getHost(request))) {
						if (removeFromBlacklist(request, getUserLevel(sender)))
							sendMessage(channel, "User '" + request + "' has been removed from the command blacklist.");
						else
							sendMessage(channel, "You do not have high enough authority to remove '" + request +
									"' from the blacklist.");
					}
					else
						sendMessage(channel, "User " + request + " isn't in the blacklist.");
				}
				else
					sendMessage(channel, "You forgot to tell me who to remove from the blacklist!");
			}
		},
		new Command("viewblacklist", 'h', 'b', viewBlacklistHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String[] list = new String[5];
				for (int i = 0; i < 5; i++)
					list[i] = "";
				Enumeration<String> blUsers = commandBlacklist.keys();
				String curName;
				while (blUsers.hasMoreElements()) {
					curName = blUsers.nextElement();
					list[commandBlacklist.get(curName).intValue() - 2] += ", " + lookUp(curName);
				}
				if (!list[4].equals(""))
					sendMessage(channel, "Blacklisted by master: " + list[4].substring(2));
				if (!list[3].equals(""))
					sendMessage(channel, "Blacklisted by owner: " + list[3].substring(2));
				if (!list[2].equals(""))
					sendMessage(channel, "Blacklisted by admin: " + list[2].substring(2));
				if (!list[1].equals(""))
					sendMessage(channel, "Blacklisted by op: " + list[1].substring(2));
				if (!list[0].equals(""))
					sendMessage(channel, "Blacklisted by hop: " + list[0].substring(2));
				if (list[0].equals("") && list[1].equals("") && list[2].equals("") 
						&& list[3].equals("") && list[4].equals(""))
					sendMessage(channel, "The blacklist is empty.");
			}
		},
		new Command("blacklist_clear", 'm', 'b', blacklistClearHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				clearBlacklist();
				sendMessage(channel, "Blacklist cleared.");
			}
		}
	};
	
	public ModuleData getModuleData() {
		return new ModuleData("User Manager", (float)2.3, "Tom Frost",
				"User Manager is responsible for keeping track of a channel's members and their statuses.",
				new String[0], new float[0], commands);
	}
	
	public void initialize() {
		loadData();
	}
	public void onUserList(String channel, User[] users) {
		this.users = BotFactory.getBot().getUserTable(channel);
		User theClone;
		for (int i = 0; i < users.length; i++) {
			theClone = users[i].clone();
			allUsers.put(theClone.getNick().toLowerCase(), theClone);
			if (!theClone.getNick().equals(BotFactory.getBot().getNick()))
				knownHosts.put(theClone.getHostname(), theClone.getNick());
		}
		saveData();
	}
	public void onJoin(String channel, String sender, String login, String hostname) {
		if (sender.equals(masterNick) && (!lookUp(hostname).equals(masterNick) || !login.equals(masterUser))) {
			masterNick = "";
			masterHost = "";
			masterUser = "";
		}
		allUsers.put(sender.toLowerCase(), new User("", new String(sender), new String(login), new String(hostname)));
		if (!sender.equals(BotFactory.getBot().getNick()));
			knownHosts.put(new String(hostname), new String(sender));
		saveData();
	}
	public void onNickChange(String oldNick, String login, String hostname, String newNick) {
		allUsers.put(newNick.toLowerCase(), new User("", new String(newNick), new String(login), new String(hostname)));
		if (!newNick.equals(BotFactory.getBot().getNick()));
			knownHosts.put(hostname, newNick);
		if (oldNick.equals(masterNick) && login.equals(masterUser) && hostname.equals(masterHost))
			masterNick = newNick;
		else if (newNick.equals(masterNick)) {
			masterNick = "";
			masterHost = "";
			masterUser = "";
		}
		saveData();
	}
	
	
	public String lookUp(String hostname) {
		return knownHosts.get(hostname);
	}
	public boolean hasVoice(String nick) {
		if (users.containsKey(nick.toLowerCase()))
			return users.get(nick.toLowerCase()).hasVoice();
		return false;
	}
	public boolean isOwner(String nick) {
		if (users.containsKey(nick.toLowerCase()))
			return users.get(nick.toLowerCase()).isOwner();
		return false;
	}
	public boolean isAdmin(String nick) {
		if (users.containsKey(nick.toLowerCase()))
			return users.get(nick.toLowerCase()).isAdmin();
		return false;
	}
	public boolean isOp(String nick) {
		if (users.containsKey(nick.toLowerCase()))
			return users.get(nick.toLowerCase()).isOp();
		return false;
	}
	public boolean isHop(String nick) {
		if (users.containsKey(nick.toLowerCase()))
			return users.get(nick.toLowerCase()).isHop();
		return false;
	}
	public boolean isMaster(String nick) {
		if (lookUp(masterHost) != null)
			return nick.toLowerCase().equals(lookUp(masterHost).toLowerCase());
		return false;
	}
	public boolean isMaster(String nick, String login) {
		if (isMaster(nick) && login.equals(masterUser))
			return true;
		return false;
	}
	public boolean isMaster(String nick, String login, String hostname) {
		if (isMaster(nick, login) && hostname.equals(masterHost))
			return true;
		return false;
	}
	public boolean isInChannel(String nick) {
		return users.containsKey(nick.toLowerCase());
	}
	public boolean isKnown(String nick) {
		return allUsers.containsKey(nick.toLowerCase());
	}
	public String getLogin(String nick) {
		if (users.containsKey(nick.toLowerCase()))
			return users.get(nick.toLowerCase()).getLogin();
		else if (allUsers.containsKey(nick.toLowerCase()))
			return allUsers.get(nick.toLowerCase()).getLogin();
		return null;
	}
	public String getHost(String nick) {
		if (users.containsKey(nick.toLowerCase()))
			return users.get(nick.toLowerCase()).getHostname();
		else if (allUsers.containsKey(nick.toLowerCase()))
			return allUsers.get(nick.toLowerCase()).getHostname();
		return null;
	}
	public User getUser(String nick) {
		if (users.containsKey(nick.toLowerCase()))
			return users.get(nick.toLowerCase());
		else if (allUsers.containsKey(nick.toLowerCase()))
			return allUsers.get(nick.toLowerCase());
		return null;
	}
	public int getUserLevel(String nick) {
		if (isMaster(nick)) return 6;
		if (isOwner(nick)) return 5;
		if (isAdmin(nick)) return 4;
		if (isOp(nick)) return 3;
		if (isHop(nick)) return 2;
		if (hasVoice(nick)) return 1;
		if (isInChannel(nick)) return 0;
		return -1;
	}
	
	public boolean addToBlacklist(String nick, int authLevel) {
		String host = getHost(nick);
		if (host == null || getUserLevel(nick) >= authLevel || nick.equals(BOT.getNick()))
			return false;
		commandBlacklist.put(host, new Integer(authLevel));
		return true;
	}
	public boolean removeFromBlacklist(String nick, int authLevel) {
		String host = getHost(nick);
		if (host == null || !commandBlacklist.containsKey(host)
				|| commandBlacklist.get(host).intValue() > authLevel)
			return false;
		commandBlacklist.remove(host);
		return true;
	}
	public boolean isBlacklisted(String nick) {
		String host = getHost(nick);
		if (host == null)
			return false;
		return commandBlacklist.containsKey(host);
	}
	public void clearBlacklist() {
		commandBlacklist = new Hashtable<String, Integer>();
	}
	
	private void saveData() {
		CONFIG.saveObject("UserManager::allUsers", allUsers);
		CONFIG.saveObject("UserManager::knownHosts", knownHosts);
		CONFIG.saveObject("UserManager::commandBlacklist", commandBlacklist);
	}
	
	@SuppressWarnings(value={"unchecked"})
	private void loadData() {
		allUsers = (Hashtable<String, User>)CONFIG.loadObject("UserManager::allUsers");
		knownHosts = (Hashtable<String, String>)CONFIG.loadObject("UserManager::knownHosts");
		commandBlacklist = (Hashtable<String, Integer>)CONFIG.loadObject("UserManager::commandBlacklist");
		
		if (allUsers == null) allUsers = new Hashtable<String, User>();
		if (knownHosts == null) knownHosts = new Hashtable<String, String>();
		if (commandBlacklist == null) commandBlacklist = new Hashtable<String, Integer>();
	}
}
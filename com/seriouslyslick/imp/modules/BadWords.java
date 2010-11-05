/**
 * BadWords.java
 * Tom Frost
 * Aug 30, 2006
 *
 * Kicks/Bans users based on use of bad words.
 */
package com.seriouslyslick.imp.modules;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;

import com.seriouslyslick.imp.BotFactory;
import com.seriouslyslick.imp.Command;
import com.seriouslyslick.imp.Module;
import com.seriouslyslick.imp.ModuleData;
import com.seriouslyslick.imp.util.TimeQueue;

import org.jibble.pircbot.Colors;

/**
 * @author Tom Frost
 *
 */
public class BadWords extends Module {
	
	// Configuration.  I should probably move these into the configuration items, but.. nah.
	private final String DEFAULT_KICK_MESSAGE = "You said a bad word!";
	private final int BADWORDS_LIMIT = 3;
	private final int MINUTE_INTERVAL = 3;
	private final int MINUTES_TO_BAN = 5;
	
	// Status constants
	private final int ACTIVE = 0;
	private final int STATUSLOCK = 1;
	private final int CHANGELOCK = 2;
	private final int BANFORUSING = 3;
	
	// Links to other modules
	private BanManager banManager;
	private UserManager userManager;

	private ArrayList<String> activeLists = new ArrayList<String>();
	private ArrayList<String> activeRegex = new ArrayList<String>();
	private Hashtable<String, ArrayList<String>> wordLists = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, String> kickMsgs = new Hashtable<String, String>();
	private Hashtable<String, Boolean[]> listStatus = new Hashtable<String, Boolean[]>();
	private Hashtable<String, ArrayList<String>> savedLists = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, String> savedKickMsgs = new Hashtable<String, String>();
	private Hashtable<String, Boolean[]> savedStatus = new Hashtable<String, Boolean[]>();
	
	private Hashtable<String, TimeQueue> banList = new Hashtable<String, TimeQueue>();
	private Hashtable<String, Integer> offenses = new Hashtable<String, Integer>();
	
	private String badwordsHelp = "Usage: " + PREFIX + "badwords [wordlist] <(+/-)word> <(+/-)word2> <...> <clear>\nUsed without any options, prints a list of the current 'bad words'.\nWhen followed with +word or -word, the command will add or remove 'word' from the list, respectively.\nFollowed by 'clear', the list will be reset.\nFor advanced users, banned words can also be regular expressions.  For example, typing:\n" + PREFIX + "badwords +.*\\s.*\nwill ban any whitespace characters.\nIf a certain list is change-locked, only my Master can change it.";
	private String savebadwordlistHelp = "Usage: " + PREFIX + "savebadwordlist [listname]\nSaves the current set of bad words (and kick message) under a certain title, so it can be recalled later.";
	private String reloadbadwordlistHelp = "Usage: " + PREFIX + "reloadbadwordlist [listname]\nClears the current set of bad words, and applies a saved set.";
	private String viewbadwordlistsHelp = "Usage: " + PREFIX + "viewbadwordlists\nDisplays the names of all the saved word lists.";
	private String removebadwordlistHelp = "Usage: " + PREFIX + "removebadwordlist [listname]\nRemoves the specified list from storage.";
	private String setkickmsgHelp = "Usage: " + PREFIX + "setkickmsg <message>\nSets the kick reason to be used when kicking someone for saying a bad word.\nLeave the message blank to use the default message.";
	private String banbadwordsHelp = "Usage: " + PREFIX + "banbadwords [wordlist] [on/off]\nBans people for excessive use of bad words.  Off by default.  If the status-lock is on for a certain list, only my Master can change this.";
	private String wordlistHelp = "Usage: " + PREFIX + "wordlist [listname] [on/off]\nActivates or deactivates a certain bad word list.  If the list is status-locked, only my Master can change it.";
	private String statuslockHelp = "Usage: " + PREFIX + "statuslock [listname] [on/off]\nSets or unsets the status lock on a given list.  When a list is status-locked, only the Master can activate or deactivate it.";
	private String changelockHelp = "Usage: " + PREFIX + "changelock [listname] [on/off]\nSets or unsets the change lock on a given list.  When a list is change-locked, only the Master can alter the contents of the list.";
	private String createlistHelp = "Usage: " + PREFIX + "createlist [listname]\nCreates the specified word list.  The list name must not currently exist, and must be only one word.";
	
	private Command[] commands = new Command[] {
		new Command("badwords", 'o', 'b', badwordsHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String[] args = request.split(" ");
				if (args.length == 0 || args[0] == null || args[0].trim().equalsIgnoreCase("")) {
					// No list name was given!  Whoops.
					sendMessage(channel, "You must specify a word list.  Type " + PREFIX + "viewbadwordlists to see a listing, or " +
							"supply any unused name to start a new list.");
				}
				else {
					// There's a list name.  Grab it!
					String listName = args[0].trim().toLowerCase();
					
					// If the list name starts with +, -, or "clear", then chances are there's been an error.
					if (listName.startsWith("+") || listName.startsWith("-") || listName.equals("clear"))
						sendMessage(channel, "You must specify a word list.  Type " + PREFIX + "viewbadwordlists to see a listing.");
					else {
						// The word list is good.  Does it exist, and are we allowed to change it?
						boolean exists = wordLists.containsKey(listName);
						boolean changelock = true;
						if (exists && (!listStatus.get(listName)[CHANGELOCK] || userManager.isMaster(sender)))
							changelock = false;
						
						if (exists) {
							// Clear command
							if (args.length >= 2 && args[1].equalsIgnoreCase("clear")) {
								if (!changelock) {
									wordLists.get(listName).clear();
									regenRegex();
									sendMessage(channel, "Bad Words list '" + listName + "' cleared.");
								}
								else
									sendMessage(channel, "Sorry, list '" + listName + "' is locked and cannot be changed.");
							}
							// Listum the wordsum.
							else if (args.length == 1 || (args.length == 2 && args[1].trim().equals(""))) {
								sendMessage(channel, "Bad words [" + listName + "]: " + makeString(getWordList(listName), ", "));
								sendMessage(channel, "Kick message [" + listName + "]: " + kickMsgs.get(listName));
							}
							// Edit a list's words.
							else if (args.length >= 2) {
								String addWords = "", removeWords = "";
								for (int i = 1; i < args.length; i++) {
									if (args[i].charAt(0) == '+' && args[i].length() > 1)
										if (addWord(listName, args[i].substring(1)))
											addWords += args[i].trim().substring(1) + " ";
									if (args[i].charAt(0) == '-' && args[i].length() > 1)
										if (removeWord(listName, args[i].substring(1)))
											removeWords += args[i].substring(1) + " ";
								}
								addWords = addWords.trim();
								removeWords = removeWords.trim();
								boolean post = false;
								if (!addWords.equals("")) {
									addWords = "The following words have been banned in the '" + listName + "' list: [" + addWords + "].";
									post = true;
								}
								if (!removeWords.equals("")) {
									removeWords = "The following words have been unbanned in the '" + listName + "' list: [" + removeWords + "].";
									post = true;
								}
								if (!addWords.equals("") && !removeWords.equals(""))
									removeWords = " " + removeWords;
								if (post)
									sendMessage(channel, addWords + removeWords);
							}
							// WTF.
							else
								sendMessage(channel, "I didn't understand that.  Try saying !help badwords");
						}
						else
							sendMessage(channel, "Sorry, I don't have a list called '" + listName + "'!");
					}
				}
			}
		},
		new Command("savebadwordlist", 'm', 'b', savebadwordlistHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String name = request.trim().toLowerCase();
				if (name.equalsIgnoreCase(""))
					sendMessage(channel, "You must supply a name for the list.");
				else {
					savedLists.put(name, makeClone(wordLists.get(name)));
					savedKickMsgs.put(name, kickMsgs.get(name));
					savedStatus.put(name, makeClone(listStatus.get(name)));
					saveLists();
					sendMessage(channel, "List '" + name + "' saved.");
				}
			}
		},
		new Command("reloadbadwordlist", 'o', 'b', reloadbadwordlistHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String name = request.trim().toLowerCase();
				if (!savedLists.containsKey(name))
					sendMessage(channel, "Sorry, I don't have a list by that name.");
				else {
					wordLists.put(name, makeClone(savedLists.get(name)));
					kickMsgs.put(name, savedKickMsgs.get(name));
					listStatus.put(name, makeClone(savedStatus.get(name)));
					regenRegex();
					sendMessage(channel, "Word list '" + name + "' has been reloaded!");
				}
			}
		},
		new Command("viewbadwordlists", 'o', 'b', viewbadwordlistsHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				Enumeration<String> allKeys = wordLists.keys();
				String curName;
				String output;
				sendMessage(channel, "Bad word lists:");
				while (allKeys.hasMoreElements()) {
					curName = allKeys.nextElement();
					output = Colors.BLUE + curName + Colors.NORMAL + " [Status: ";
					if (listStatus.get(curName)[ACTIVE])
						output += Colors.GREEN + "ON" + Colors.NORMAL;
					else
						output += Colors.RED + "OFF" + Colors.NORMAL;
					output += "] [Locks: ";
					if (listStatus.get(curName)[STATUSLOCK] && !listStatus.get(curName)[CHANGELOCK])
						output += Colors.RED + "status" + Colors.NORMAL;
					else if (!listStatus.get(curName)[STATUSLOCK] && listStatus.get(curName)[CHANGELOCK])
						output += Colors.RED + "change" + Colors.NORMAL;
					else if (listStatus.get(curName)[STATUSLOCK] && listStatus.get(curName)[CHANGELOCK])
						output += Colors.RED + "status" + Colors.NORMAL + ", " + Colors.RED + "change" + Colors.NORMAL;
					else
						output += "None";
					output += "] [Banning: ";
					if (listStatus.get(curName)[BANFORUSING])
						output += Colors.GREEN + "ON" + Colors.NORMAL;
					else
						output += Colors.RED + "OFF" + Colors.NORMAL;
					output += "]";
					sendMessage(channel, output);
				}
			}
		},
		new Command("removebadwordlist", 'm', 'b', removebadwordlistHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				if (request.equals(""))
					sendMessage(channel, "You must specify a list name.");
				else {
					request = request.trim().toLowerCase();
					if (wordLists.containsKey(request)) {
						wordLists.remove(request);
						kickMsgs.remove(request);
						listStatus.remove(request);
						if (savedLists.containsKey(request)) {
							savedLists.remove(request);
							savedKickMsgs.remove(request);
							savedStatus.remove(request);
							saveLists();
						}
						regenRegex();
						sendMessage(channel, "List '" + request + "' removed.");
					}
					else
						sendMessage(channel, "Sorry, list '" + request + "' doesn't exist!");
				}
			}
		},
		new Command("setkickmsg", 'o', 'b', setkickmsgHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String[] args = request.split(" ", 2);
				args[0] = args[0].trim().toLowerCase();
				if (wordLists.containsKey(args[0])) {
					if (args.length > 1 && !args[1].trim().equals("")) {
						kickMsgs.put(args[0], args[1]);
						sendMessage(channel, "Kick message for list '" + args[0] + "' changed to: " + args[1]);
					}
					else
						sendMessage(channel, "You must specify a kick message.");
				}
				else {
					if (args[0].trim().equals(""))
						sendMessage(channel, "You must specify a list name.");
					else
						sendMessage(channel, "Sorry, list '" + args[0] + "' doesn't exist!");
				}
			}
		},
		new Command("banbadwords", 'o', 'b', banbadwordsHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String[] args = request.split(" ");
				args[0] = args[0].trim().toLowerCase();
				if (wordLists.containsKey(args[0])) {
					boolean statuslock = true;
					if (!listStatus.get(args[0])[STATUSLOCK] || userManager.isMaster(sender))
						statuslock = false;
					if (!statuslock) {
						if (args.length > 1 && args[1].trim().matches("(?i)(on|off)")) {
							if (args[1].trim().equalsIgnoreCase("on")) {
								listStatus.get(args[0])[BANFORUSING] = true;
								sendMessage(channel, "Now banning people when they use " + BADWORDS_LIMIT +
										" bad words within " + MINUTE_INTERVAL + " minutes for list '" + args[0] + "'.");
							}
							else {
								listStatus.get(args[0])[BANFORUSING] = false;
								sendMessage(channel, "No longer banning on list '" + args[0] + "'.");
							}
						}
						else
							sendMessage(channel, "You must specify either 'on' or 'off'.");
					}
					else
						sendMessage(channel, "Sorry, this list is status-locked, and you don't have the permissions " +
							"to change it.");
				}
				else {
					if (args[0].trim().equals(""))
						sendMessage(channel, "You must specify a list name.");
					else
						sendMessage(channel, "Sorry, list '" + args[0] + "' doesn't exist!");
				}
			}
		},
		new Command("wordlist", 'o', 'b', wordlistHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String[] args = request.split(" ");
				args[0] = args[0].trim().toLowerCase();
				if (wordLists.containsKey(args[0])) {
					boolean statuslock = true;
					if (!listStatus.get(args[0])[STATUSLOCK] || userManager.isMaster(sender))
						statuslock = false;
					if (!statuslock) {
						if (args.length > 1 && args[1].trim().matches("(?i)(on|off)")) {
							if (args[1].trim().equalsIgnoreCase("on")) {
								listStatus.get(args[0])[ACTIVE] = true;
								sendMessage(channel, "List '" + args[0] + "' has been activated.");
							}
							else {
								listStatus.get(args[0])[ACTIVE] = false;
								sendMessage(channel, "List '" + args[0] + "' has been deactivated.");
							}
							regenRegex();
						}
						else
							sendMessage(channel, "You must specify either 'on' or 'off'.");
					}
					else
						sendMessage(channel, "Sorry, this list is status-locked, and you don't have the permissions " +
								"to change it.");
				}
				else {
					if (args[0].trim().equals(""))
						sendMessage(channel, "You must specify a list name.");
					else
						sendMessage(channel, "Sorry, list '" + args[0] + "' doesn't exist!");
				}
			}
		},
		new Command("statuslock", 'm', 'b', statuslockHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String[] args = request.split(" ");
				args[0] = args[0].trim().toLowerCase();
				if (wordLists.containsKey(args[0])) {
					if (args.length > 1 && args[1].trim().matches("(?i)(on|off)")) {
						if (args[1].trim().equalsIgnoreCase("on")) {
							listStatus.get(args[0])[STATUSLOCK] = true;
							sendMessage(channel, "Status lock activated for list '" + args[0] + "'.");
						}
						else {
							listStatus.get(args[0])[STATUSLOCK] = false;
							sendMessage(channel, "Status lock deactivated for list '" + args[0] + "'.");
						}
					}
					else
						sendMessage(channel, "You must specify either 'on' or 'off'.");
				}
				else {
					if (args[0].trim().equals(""))
						sendMessage(channel, "You must specify a list name.");
					else
						sendMessage(channel, "Sorry, list '" + args[0] + "' doesn't exist!");
				}
			}
		},
		new Command("changelock", 'm', 'b', changelockHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String[] args = request.split(" ");
				args[0] = args[0].trim().toLowerCase();
				if (wordLists.containsKey(args[0])) {
					if (args.length > 1 && args[1].trim().matches("(?i)(on|off)")) {
						if (args[1].trim().equalsIgnoreCase("on")) {
							listStatus.get(args[0])[CHANGELOCK] = true;
							sendMessage(channel, "Change lock activated for list '" + args[0] + "'.");
						}
						else {
							listStatus.get(args[0])[CHANGELOCK] = false;
							sendMessage(channel, "Change lock deactivated for list '" + args[0] + "'.");
						}
					}
					else
						sendMessage(channel, "You must specify either 'on' or 'off'.");
				}
				else {
					if (args[0].trim().equals(""))
						sendMessage(channel, "You must specify a list name.");
					else
						sendMessage(channel, "Sorry, list '" + args[0] + "' doesn't exist!");
				}
			}
		},
		new Command("createlist", 'o', 'b', createlistHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				request = request.trim().toLowerCase();
				if (!request.equals("")) {
					if (createList(request))
						sendMessage(channel, "List '" + request + "' created.");
					else
						sendMessage(channel, "List '" + request + "' already exists!  Try a different name.");
				}
				else
					sendMessage(channel, "You didn't tell me what list to make!");
			}
		}
	};
	
	public ModuleData getModuleData() {
		return new ModuleData("Bad Words", (float)2.0, "Tom Frost", "Kicks people for saying bad words, or bans them for using them repeatedly. Handles multiple sets of bad words, each with their own unique kick message.",
				new String[] {"User Manager", "Ban Manager"}, new float[] {(float)1.1, (float)1.0}, commands);
	}
	
	public void initialize() {
		banManager = (BanManager)BotFactory.getModuleManager().getModule("Ban Manager");
		userManager = (UserManager)BotFactory.getModuleManager().getModule("User Manager");
		loadSavedLists();
	}
	
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (activeLists.size() > 0) {
			for (int i = 0; i < activeLists.size(); i++) {
				if (activeRegex.get(i) != null && message.matches(activeRegex.get(i))) {
					if (banList.containsKey(hostname)) {
						if (banList.get(hostname).stamp() && listStatus.get(activeLists.get(i))[BANFORUSING]) {
							int banMinutes = MINUTES_TO_BAN * (int)Math.pow((double)2, offenses.get(hostname).doubleValue());
							offenses.put(hostname, new Integer(offenses.get(hostname).intValue() + 1));
							banManager.ban(sender, banMinutes);
							banManager.kick(sender, "You've used " + BADWORDS_LIMIT + " forbidden words within " + 
									MINUTE_INTERVAL + " minutes.  You are banned for " + banMinutes +
									" minutes.  This is offense #" + offenses.get(hostname) + " for you.");
							banList.get(hostname).reset();
							return;
						}
					}
					else {
						banList.put(hostname, new TimeQueue(BADWORDS_LIMIT, MINUTE_INTERVAL * 60));
						banList.get(hostname).stamp();
						offenses.put(hostname, new Integer(0));
					}
					banManager.kick(sender, kickMsgs.get(activeLists.get(i)));
				}
			}
		}
	}
	
	public void onAction(String sender, String login, String hostname, String target, String action) {
		onMessage(target, sender, login, hostname, action);
	}
	
	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		onMessage(target, sourceNick, sourceLogin, sourceHostname, notice);
	}
	
	
	/**
	 * Gets the specified word list in array form.
	 * 
	 * @param list The name of the list to retrieve.
	 * @return An array of the words in the given list.
	 */
	public String[] getWordList(String list) {
		list = list.trim().toLowerCase();
		return wordLists.get(list).toArray(new String[] {});
	}
	
	/**
	 * Creates the specified list.  The new list will be blank, with all status items turned off, and with
	 * a default kick message.
	 * 
	 * @param list The name of the list to be created.
	 * @return <code>true</code> if the list did not exist and was created; <code>false</code> otherwise.
	 */
	public boolean createList(String list) {
		list = list.trim().toLowerCase();
		if (!wordLists.containsKey(list)) {
			wordLists.put(list, new ArrayList<String>());
			kickMsgs.put(list, DEFAULT_KICK_MESSAGE);
			listStatus.put(list, newStatusSet());
			return true;
		}
		return false;
	}
	
	/**
	 * Adds a word to the specified list.
	 * 
	 * @param list The word list to add the word to.
	 * @param word The word to be added.
	 * @return <code>true</code> if the word did not exist and was added; <code>false</code> otherwise, or if the list
	 * 			itself does not exist.
	 */
	public boolean addWord(String list, String word) {
		word = word.trim();
		list = list.trim().toLowerCase();
		if (wordLists.containsKey(list) && !wordLists.get(list).contains(word)) {
			wordLists.get(list).add(word);
			regenRegex();
			return true;
		}
		return false;
	}
	
	/**
	 * Removes a word from the specified list.
	 * 
	 * @param list The word list to remove the word from.
	 * @param word The word to be removed.
	 * @return <code>true</code> if the word dexisted and was removed; <code>false</code> otherwise, or if the list
	 * 			itself does not exist.
	 */
	public boolean removeWord(String list, String word) {
		word = word.trim();
		list = list.trim().toLowerCase();
		if (wordLists.containsKey(list) && wordLists.get(list).contains(word)) {
			wordLists.get(list).remove(word);
			regenRegex();
			return true;
		}
		return false;
	}
	
	/**
	 * This method digs into the status objects for each list to see if it's marked as activated.
	 * If so, it adds the list to the ArrayList of activated lists, and generates a regex pattern
	 * for that list and adds it to the activeRegex ArrayList.
	 *
	 */
	private void regenRegex() {
		Enumeration<String> keys = wordLists.keys();
		activeLists.clear();
		activeRegex.clear();
		String list;
		while (keys.hasMoreElements()) {
			list = keys.nextElement();
			if (listStatus.get(list)[ACTIVE]) {
				activeLists.add(list);
				String words = makeString(getWordList(list), "|");
				String regex;
				if (words.equals(""))
					regex = null;
				else
					regex = "(?i).*(\\W|^)(" + words + ")(\\W|$).*";
				activeRegex.add(regex);
			}
		}
	}
	
	/**
	 * Turns a String array into a flat String, using a specified delimiter to separate each value.
	 * 
	 * @param array The array to be made into a string.
	 * @param delim The delimiter to separate each string value.
	 * @return
	 */
	private String makeString(String[] array, String delim) {
		String ret = "";
		if (array.length > 0)
			ret = array[0];
		for (int i = 1; i < array.length; i++)
			ret += delim + array[i];
		return ret;
	}
	
	@SuppressWarnings(value={"unchecked"})
	private ArrayList<String> makeClone(ArrayList<String> orig) {
		return (ArrayList<String>)orig.clone();
	}
	
	private Hashtable<String, Boolean[]> makeClone(Hashtable<String, Boolean[]> orig) {
		Enumeration<String> keys = orig.keys();
		Hashtable<String, Boolean[]> ret = new Hashtable<String, Boolean[]>();
		String ele;
		while (keys.hasMoreElements()) {
			ele = keys.nextElement();
			ret.put(ele, makeClone(orig.get(ele)));
		}
		return ret;
	}
	
	private Boolean[] makeClone(Boolean[] orig) {
		Boolean[] ret = new Boolean[orig.length];
		for (int i = 0; i < orig.length; i++)
			ret[i] = orig[i];
		return ret;
	}
	
	private void saveLists() {
		CONFIG.saveObject("BadWords::savedKickMsgs", savedKickMsgs);
		CONFIG.saveObject("BadWords::savedLists", savedLists);
		CONFIG.saveObject("BadWords::savedStatus", savedStatus);
	}
	
	private Boolean[] newStatusSet() {
		return new Boolean[] {false, false, false, false};
	}
	
	/**
	 * This method not only restores the saved objects, but it also clears the kickMsgs, wordLists, and
	 * listStatus objects and populates them with the saved data.  As such, this should be called only
	 * once at the initialization of the module, and never again unless the module should be reverted back
	 * to its original state.
	 *
	 */
	@SuppressWarnings(value={"unchecked"})
	private void loadSavedLists() {
		savedKickMsgs = (Hashtable<String, String>)CONFIG.loadObject("BadWords::savedKickMsgs");
		savedLists = (Hashtable<String, ArrayList<String>>)CONFIG.loadObject("BadWords::savedLists");
		savedStatus = (Hashtable<String, Boolean[]>)CONFIG.loadObject("BadWords::savedStatus");
		// For backward compatibility...
		if (savedKickMsgs == null)
			savedKickMsgs = new Hashtable<String, String>();
		if (savedLists == null)
			savedLists = new Hashtable<String, ArrayList<String>>();
		if (savedStatus == null)
			savedStatus = new Hashtable<String, Boolean[]>();
		
		kickMsgs = (Hashtable<String, String>)savedKickMsgs.clone();
		wordLists = (Hashtable<String, ArrayList<String>>)savedLists.clone();
		listStatus = makeClone(savedStatus);
		
		// For backward compatibility, if we can't find a status object for a given list,
		// we're going to make one.  If a list HAS one, we're going to see if it is active
		// or not.  If so, we're going to make it active in our current runtime as well.
		activeLists = new ArrayList<String>();
		Enumeration<String> listKeys = wordLists.keys();
		String listName;
		boolean changed = false;
		while (listKeys.hasMoreElements()) {
			listName = listKeys.nextElement();
			if (listStatus.get(listName) == null) {
				// This item has no status object associated with it.  Let's give it one.
				listStatus.put(listName, newStatusSet());
				savedStatus.put(listName, newStatusSet());
				changed = true;
			}
		}
		// Excellent.  If we've had to make any ListStatus objects, let's save our changes.
		if (changed)
			CONFIG.saveObject("BadWords::savedStatus", savedStatus);
		
		// Some of our lists may be turned on by default.  Let's activate them, if so.
		regenRegex();
	}
}

/**
 * Harassment.java
 * Tom Frost
 * August 12, 2006
 * 
 * A fun little module to harass other users.
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.*;

/**
 * @author Tom Frost
 *
 */
public class Harassment extends Module {
	
	private String harass = "@", harassMsg = "";
	private boolean harassMsgOn = false;
	private boolean harassing = true;
	
	private String harassHelp = "Usage: " + PREFIX + "harass [name]. <And tell [him/her] that [message].>\n" +
		"Exmaple: " + PREFIX + "harass John. And tell him that he likes asparagus.\n" +
		"Results in the bot saying 'Shut up, John.  You like asparagus.' when John speaks.";
	private String harassResetHelp = "Usage: " + PREFIX + "harass_reset\nStops the bot from harassing his target.";
	private String harassmentHelp = "Usage: " + PREFIX + "harassment [on/off]\n" +
		"Turns the harassment module on or off.  When off, no one can use the harass command.";
	
	private Command[] commands = new Command[] {
		new Command("harass", 'p', 'b', harassHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				if (!harassing)
					sendMessage(channel, "Sorry " + sender + ", the harassment module is deactivated.");
				else {
					int possibleEnd = -1;
					int nameEnd = request.indexOf(' ');
					if (nameEnd == -1)
						harass = request;
					else {
						if ((possibleEnd = request.indexOf('.')) != -1 && possibleEnd < nameEnd)
							nameEnd = possibleEnd;
						if ((possibleEnd = request.indexOf(',')) != -1 && possibleEnd < nameEnd)
							nameEnd = possibleEnd;
						harass = request.substring(0, nameEnd);
					}
					if (harass.equalsIgnoreCase(BotFactory.getBot().getNick()) || 
							harass.equalsIgnoreCase(BotFactory.getBot().getLogin()) ||
							harass.equalsIgnoreCase(BotFactory.getBot().getLogin().substring(1))) {
						sendMessage(channel, "You're a moron, " + sender + ". I'm not harassing myself.");
						harass = "@";
						harassMsgOn = false;
						return;
					}
					if (request.matches("(?i).*and\\stell\\s(him|her)\\sthat\\s.*")) {
						request = request.substring(request.indexOf("that") + 5);
						request = toSecondPerson(request);
						harassMsg = request.substring(0, 1).toUpperCase() + request.substring(1);
						harassMsgOn = true;
					}
					else
						harassMsgOn = false;
					if (harass.trim().equals("")) {
						sendMessage(channel, "You didn't tell me who to harass!");
						harass = "@";
						harassMsgOn = false;
						return;
					}
					sendMessage(channel, "I'll be waiting for " + harass + " >:D");
				}
			}
		},
		new Command("harass_reset", 'p', 'b', harassResetHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				if (!harass.equals("@")) {
					harassMsgOn = false;
					sendMessage(channel, "Aw, ok, I'll stop.  Sorry " + harass + ".");
					harass = "@";
				}
				else
					sendMessage(channel, "Hey, I'm not harassing anyone!");
			}
		},
		new Command("harassment", 'h', 'b', harassmentHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				if (request.equalsIgnoreCase("off")) {
					harassing = false;
					harass = "@";
					sendMessage(channel, "Harassment module has been turned off.");
				}
				else if (request.equalsIgnoreCase("on")) {
					harassing = true;
					sendMessage(channel, "Harassment module activated!");
				}
				else
					sendMessage(channel, "Sorry, I only understand 'on' and 'off' with that command.");
			}
		}
	};
	
	public ModuleData getModuleData() {
		return new ModuleData("Harassment", (float)1.1, "Tom Frost",
				"Harassment is a fun little module to harass other users.  Anyone can control " +
				"it, but it's activated/deactivated by half-ops and ops.", new String[0], new float[0], commands);
	}
	
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (harassing && (sender.equalsIgnoreCase(harass) || login.equalsIgnoreCase(harass) ||
				login.substring(1).equalsIgnoreCase(harass))) {
			if (harassMsgOn)
				sendMessage(channel, "Shut up, " + sender + ". " + harassMsg);
			else
				sendMessage(channel, "Shut up, " + sender + ".");
		}
	}
	
	private String toSecondPerson(String str) {
		str = " " + str + " ";
		
		str = str.replaceAll(" you ", " I ");
		str = str.replaceAll(" she is", " you are");
		str = str.replaceAll(" she has", " you have");
		str = str.replaceAll(" he is", " you are");
		str = str.replaceAll(" he has", " you have");
		str = str.replaceAll(" she's", " you're");
		str = str.replaceAll(" he's", " you're");
		str = str.replaceAll(" his", " your");
		str = str.replaceAll(" her", " your");
		str = str.replaceAll(" himself", " yourself");
		str = str.replaceAll(" herself", " yourself");
		str = str.replaceAll(" him", " you");

		int cur = 0, prev = 0, posA, posB;
		String match;
		while (str.matches(".*\\s(he|she)\\s\\w*s.*")) {
			prev = cur;
			if ((cur = str.indexOf(" he", cur)) == -1)
				cur = str.indexOf(" she", prev);
			if ((prev = str.indexOf(" she", prev)) < cur && prev != -1)
				cur = prev;
			if (cur == -1)
				break;
			match = str.substring((posA = str.indexOf(' ', cur + 1) + 1), (posB = closestIndexOf(new char[] {' ', '.', ',', '-', '!', ';', ':', '?'}, str, str.indexOf(' ', cur + 1) + 1)));
			if (match.endsWith("ies"))
				match = match.substring(0, match.length() - 3) + "y";
			else if (match.endsWith("s"))
				match = match.substring(0, match.length() - 1);
			str = str.substring(0, posA) + match + str.substring(posB);
			cur = posB;
		}
		
		str = str.replaceAll(" he ", " you ");
		str = str.replaceAll(" she ", " you ");
		str = str.trim();
		
		return str;
	}
	
	private int closestIndexOf(char[] chars, String findIn, int startAt) {
		int index = findIn.indexOf(chars[0], startAt);
		int check;
		for (int i = 1; i < chars.length; i++) {
			if ((check = findIn.indexOf(chars[i], startAt)) < index && check != -1)
				index = check;
		}
		return index;
	}
}

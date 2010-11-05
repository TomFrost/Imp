/**
 * RemoteControl.java
 * Tom Frost
 * August 12, 2006
 * 
 * Provides a way to make the IRC bot do.. whatever.
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.*;

/**
 * @author Tom Frost
 *
 */
public class RemoteControl extends Module {
	
	private String sayHelp = "Usage: " + PREFIX + "say [text]\nMakes the bot say any text in its main channel.";
	private String meHelp = "Usage: " + PREFIX + "me [text]\nMakes the bot perform an action in its main channel.";
	private String msgHelp = "Usage: " + PREFIX + "msg [person/channel] [text]\nMakes the bot send a message to the specified person or channel.";
	private String modeHelp = "Usage: " + PREFIX + "mode [mode]\nSets the specified mode. NOTE: Channel not required in this command, as with normal IRC clients.  The mode is simply set on the bot's operating channel.";
	private String rejoinHelp = "Usage: " + PREFIX + "rejoin\nMakes the bot rejoin its main channel.";
	private String nickHelp = "Usage: " + PREFIX + "nick [new nickname]\nChanges the bot's nickname.";
	private String dieHelp = "Usage: " + PREFIX + "die <reason>\nKills the bot, with optional reason.";
	private String opHelp = "Usage: " + PREFIX + "op [nickname]\nOps the specified user.";
	private String deopHelp = "Usage: " + PREFIX + "deop [nickname]\nDe-Ops the specified user.";
	private String hopHelp = "Usage: " + PREFIX + "hop [nickname]\nHalf-Ops the specified user.";
	private String dehopHelp = "Usage: " + PREFIX + "dehop [nickname]\nDe-Half-Ops the specified user.";
	private String voiceHelp = "Usage: " + PREFIX + "voice [nickname]\nGives a voice to the specified user.";
	private String devoiceHelp = "Usage: " + PREFIX + "devoice [nickname]\nRemoves voice from the specified user.";
	
	private Command[] commands = new Command[] {
		new Command("say", 'h', 'p', sayHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				sendMessage(CONFIG.getValue(CONF_CHANNEL), request);
			}
		},
		new Command("me", 'h', 'p', meHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				sendAction(CONFIG.getValue(CONF_CHANNEL), request);
			}
		},
		new Command("msg", 'o', 'b', msgHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				String[] data = request.split(" ", 2);
				if (data.length > 1 && !data[0].trim().equals(""))
					sendMessage(data[0], data[1]);
				else
					sendMessage(channel, "When sending someone a private message.. you need both the someone, " +
							"and the private message.");
			}
		},
		new Command("mode", 'o', 'b', modeHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				BOT.setMode(CONFIG.getValue(CONF_CHANNEL), request);
			}
		},
		new Command("rejoin", 'o', 'b', rejoinHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				BOT.partChannel(CONFIG.getValue(CONF_CHANNEL));
				BOT.joinChannel();
			}
		},
		new Command("nick", 'm', 'b', nickHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				BOT.changeNick(request);
			}
		},
		new Command("die", 'm', 'b', dieHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				BOT.quitServer(request);
				System.exit(0);
			}
		},
		new Command("op", 'o', 'b', opHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				BOT.op(CONFIG.getValue(CONF_CHANNEL), request);
			}
		},
		new Command("deop", 'o', 'b', deopHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				BOT.deOp(CONFIG.getValue(CONF_CHANNEL), request);
			}
		},
		new Command("hop", 'o', 'b', hopHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				BOT.hop(CONFIG.getValue(CONF_CHANNEL), request);
			}
		},
		new Command("dehop", 'o', 'b', dehopHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				BOT.deHop(CONFIG.getValue(CONF_CHANNEL), request);
			}
		},
		new Command("voice", 'h', 'b', voiceHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				BOT.voice(CONFIG.getValue(CONF_CHANNEL), request);
			}
		},
		new Command("devoice", 'h', 'b', devoiceHelp) {
			public void run(String channel, String sender, String login, String hostname, String request) {
				BOT.deVoice(CONFIG.getValue(CONF_CHANNEL), request);
			}
		}
	};
	
	public ModuleData getModuleData() {
		return new ModuleData("Remote Control", (float)1.1, "Tom Frost", "Provides a way to make the bot say and do almost anything!",
				new String[0], new float[0], commands);
	}
}

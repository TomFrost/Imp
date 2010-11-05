/**
 * Acrophobia.java
 * Tom Frost
 * March 15, 2007
 * 
 * Plays Acrophobia!  See "instructions" command for details on gameplay.
 * 
 * This module was thrown together quickly because I was really in the mood to play this--
 * admittedly, it was planned poorly and executed even worse.  This code is quite messy and
 * redundant, so please accept my apologies.  It should be understandable at the least, though.
 * Just know that the entire faceoff round is handled separately from the functions that control
 * the rest of the game, and exists in the contained "FaceOff" class.
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.Command;
import com.seriouslyslick.imp.Module;
import com.seriouslyslick.imp.ModuleData;

import java.util.Hashtable;
import java.util.ArrayList;
import org.jibble.pircbot.Colors;
import java.util.Timer;
import java.util.TimerTask;

public class Acrophobia extends Module {
	
	private class PlayerData {
		public String name = "";
		public int index = -1;
		public int points = 0;
		public String acro = "";
		public int vote = -1;
		public int submitOrder = -1;
		public int lastAcroRound = -1;
		public int lastVoteRound = -1;
	}
	private class VoteObject {
		public String acro = "";
		public int playerIndex = -1;
		public int votes = 0;
		public int submitOrder = 0;
		ArrayList<Integer> voters = new ArrayList<Integer>();
	}
	private class FaceOff {
		boolean voteOn = false;
		boolean acroOn = false;
		int voteRound = 0;
		int acroRound = 0;
		int gameRound;
		int numVoters;
		
		private int[] players = new int[2];
		String[] matchAcros = new String[3];
		int[] fastest = new int[3];
		boolean[] swapped = new boolean[3];
		String[][] acros = new String[3][2];
		int[][] scores = new int[3][2];
		int[] scoreTotal = new int[2];
		Timer faceOffTimer = new Timer();
		
		@SuppressWarnings(value={"unchecked"})
		ArrayList<Integer>[][] voters = new ArrayList[3][2];
		
		FaceOff(int player1, int player2) {
			players[0] = player1;
			players[1] = player2;
			
			scoreTotal[0] = 0;
			scoreTotal[1] = 0;
			
			numVoters = playersInScoreboard() - 2;
			gameRound = curRound + 1;
		
			for (int i = 0; i < 3; i++) {
				matchAcros[i] = getAcro(i + 3);
				fastest[i] = -1;
				
				if ((int)(Math.random() * 2) == 1)
					swapped[i] = true;
				else
					swapped[i] = false;
			}
			
			for (int i = 0; i < 3; i++) {
				for (int q = 0; q < 2; q++) {
					acros[i][q] = "";
					scores[i][q] = 0;
					voters[i][q] = new ArrayList<Integer>();
				}
			}
		}
		
		public void submit(String sender, String message) {
			PlayerData player = getPlayerData(sender);
			
			// If it's one of the two in the faceoff, he's gotta be submitting an acro.
			// If not, he's gotta be voting.
			if (player.index == players[0] || player.index == players[1])
				acroSubmit(player.index, message);
			else
				voteSubmit(player, message);
		}
		
		private void acroSubmit(int index, String message) {
			if (acroOn) {
				if (matches(message, matchAcros[acroRound])) {
					acros[acroRound][getIndex(index)] = message;
					sendMessage(getName(index), "Acro submitted!");
					
					if (fastest[acroRound] == -1)
						fastest[acroRound] = getIndex(index);
					else if (fastest[acroRound] == getIndex(index) && !acros[acroRound][swap(getIndex(index))].equals(""))
						fastest[acroRound] = swap(getIndex(index));
				}
				else
					sendMessage(getName(index), "That doesn't match the acro!  The acro is: [ " + matchAcros[acroRound] + " ].");
			}
		}
		
		private void voteSubmit(PlayerData player, String message) {
			if (voteOn) {
				message = message.trim();
				if (message.matches("[12]")) {
					int vote = Integer.parseInt(message) - 1;
					if (swapped[voteRound])
						vote = swap(vote);
					boolean changedVote = false;
					boolean doubleVote = false;
					for (int i = 0; i < 2; i++) {
						if (voters[voteRound][i].contains(new Integer(player.index))) {
							if (vote == i)
								doubleVote = true;
							else {
								changedVote = true;
								voters[voteRound][swap(i)].remove(new Integer(player.index));
								scores[voteRound][swap(i)]--;
							}
						}
					}
					
					if (doubleVote)
						sendMessage(player.name, "Sorry, you can't vote for a player more than once.");
					else {
						voters[voteRound][vote].add(new Integer(player.index));
						scores[voteRound][vote]++;
						int realVote = vote;
						if (swapped[voteRound])
							realVote = swap(realVote);
						realVote++;
						if (changedVote)
							sendMessage(player.name, "Vote changed to #" + realVote + ": " + acros[voteRound][vote]);
						else
							sendMessage(player.name, "You voted for #" + realVote + ": " + acros[voteRound][vote]);
					}
				}
				else
					sendMessage(player.name, "That is not a valid vote.  Please vote for either 1 or 2 by typing " +
							"/msg " + BOT.getNick() + " 1  or  /msg " + BOT.getNick() + " 2 .");
			}
		}
		
		private void enterVoting(int round) {
			voteRound = round;
			int blank = -1;
			
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "The acro from round " + (round+1) + " was: [ " + matchAcros[round] + " ].");
			
			for (int i = 0; i < 2; i++)
				if (acros[round][i].equals(""))
					blank = i;
			if (blank != -1) {
				scores[voteRound][swap(blank)] += numVoters;
				scoreTotal[swap(blank)] += numVoters;
				sleep(5);
				sendMessage(CONFIG.getValue(CONF_CHANNEL), getName(players[swap(blank)]) + " was the only player to answer, and will receive " +
				"one vote for every player in the scoreboard.");
				sleep(5);
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "[" + getName(players[swap(blank)]) + " | " + numVoters + " votes]  " +
						acros[voteRound][swap(blank)]);
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "[" + getName(players[blank]) + " | 0 votes]  **No Answer**");
				if (voteRound == 2)
					showResults();
				else {
					sleep(5);
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "That's a big setback for " + getName(players[blank]) + "!");
				}
			}
			else {
				for (int i = 0; i < 2; i++) {
					if (swapped[round])
						sendMessage(CONFIG.getValue(CONF_CHANNEL), (i+1) + " |  " + acros[round][swap(i)]);
					else
						sendMessage(CONFIG.getValue(CONF_CHANNEL), (i+1) + " |  " + acros[round][i]);
				}
				voteOn = true;
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "Voting is open!  You have 20 seconds to pick your favorite.");
				sleep(15);
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "5 seconds!");
				sleep(5);
				voteOn = false;
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "Time's up!  Here's this results:");
				for (int i = 0; i < 2; i++) {
					if (swapped[round])
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "[" + getName(players[swap(i)]) + " | " + scores[round][swap(i)] +
								" votes]  " + acros[round][swap(i)]);
					else
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "[" + getName(players[i]) + " | " + scores[round][i] +
								" votes]  " + acros[round][i]);
					scoreTotal[i] += scores[round][i];
				}
				if (voteRound == 2)
					showResults();
			}
		}
		
		private void showResults() {
			sleep(2);
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "Total scores:  [" + getName(players[0]) + " " + scoreTotal[0] + "]  [" +
					getName(players[1]) + " " + scoreTotal[1] + "]");
			sleep(2);
			String winner = "";
			if (scoreTotal[0] == scoreTotal[1]) {
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "We have a tie!  The winner will be decided by which of the two submitted their answers " +
						"the fastest.  Let's take a look at who answered the fastest in each round:");
				sleep(5);
				if (fastest[0] == -1)
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "No one answered round one.");
				else
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "Round one, fastest answer: " + getName(players[fastest[0]]));
				sleep(3);
				if (fastest[1] == -1)
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "No one answered round two.");
				else
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "Round two, fastest answer: " + getName(players[fastest[1]]));
				sleep(3);
				if (fastest[2] == -1)
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "No one answered round three.");
				else
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "Round one, fastest answer: " + getName(players[fastest[2]]));
				sleep(2);
				
				if (fastest[0] != -1 && fastest[1] != -1 && fastest[2] != -1) {
					int[] tiebreaker = new int[2];
					tiebreaker[0] = 0;
					tiebreaker[1] = 0;
					for (int i = 0; i < 3; i++)
						tiebreaker[fastest[i]]++;
					if (tiebreaker[0] > tiebreaker[1])
						winner = getName(players[0]);
					else if (tiebreaker[1] > tiebreaker[0])
						winner = getName(players[1]);
					else {
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "Well, that doesn't help much!  Let's call it a tie!");
						winner = getName(players[0]) + " and " + getName(players[1]);
					}
				}
				else {
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "Looks like the game is dying out.  I'm going to stop now.");
					gameStop("");
					return;
				}
			}
			else {
				if (scoreTotal[0] > scoreTotal[1])
					winner = getName(players[0]);
				else
					winner = getName(players[1]);
			}
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "Congratulations, " + winner + "!  You've won the game!");
			sleep(10);
			if (gameMode != ACRO_OFF) {
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "The next game of Acrophobia will begin in 30 seconds.");
				sleep(30);
			}
			if (gameMode != ACRO_OFF) {
				beginNewGame();
			}
		}
		
		private int swap(int num) {
			if (num == 1) return 0;
			if (num == 0) return 1;
			return -1;
		}
		
		private int getIndex(int playerIndex) {
			if (players[0] == playerIndex) return 0;
			if (players[1] == playerIndex) return 1;
			return -1;
		}
		
		public void die() {
			voteOn = false;
			acroOn = false;
			faceOffTimer.cancel();
		}
		
		public void begin() {
			gameMode = ACRO_FACEOFF;
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "Welcome to the face off, " + getName(players[i]) + "!  You have 30 " +
								"seconds to answer each acronym.  You can change your answer as often as you want before time " +
								"is up.  There will be 3 rounds.");
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "These two players are about to enter a 3-round battle.  Whoever totals the most " +
							"votes in these three rounds wins the game!");
				}
			}, (long)(0));
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					acroRound = 0;
					acroOn = true;
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "Your first acro is: [ " + matchAcros[0] + " ].  30 seconds.");
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "In each round, they'll be given an acronym and 30 seconds to answer it.");
				}
			}, (long)(5 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() {
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "They'll start with 3 letters, and work their way to 5 in the final round.");
				}
			}, (long)(11 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() {
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "After each round, I'll tell you what the acronym was and open up voting.  " +
							"Votes are the ONLY points that these two players get, so don't forget to vote!");
				}
			}, (long)(17 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() {
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "You'll have 20 seconds to pick your favorite answer.  Since our two players " +
							"will be frantically trying to come up with phrases for the acronyms, they probably won't be " +
							"paying attention to your comments.");
				}
			}, (long)(23 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "10 seconds.");
				}
			}, (long)(25 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "5 seconds.");
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "Votes are submitted like usual: '/msg " + BOT.getNick() + " 1' to vote for the first, or " +
							"'/msg " + BOT.getNick() + " 2' to vote for the second.  Anyone but the faceoff players can vote!");
				}
			}, (long)(30 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					acroRound = 1;
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "Time's up!  Here's your next acro: [ " + matchAcros[1] + " ].  30 seconds.");
					if (acros[0][0].equals("") && acros[0][1].equals("")) {
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "The first acro was [ " + matchAcros[0] + " ].  Neither player was able to answer " +
								"it, so there will be no points this round.");
						faceOffTimer.schedule(new TimerTask() {
							public void run() { 
								sendMessage(CONFIG.getValue(CONF_CHANNEL), "There won't be a do-over for this round-- that means our players only " +
										"get two more rounds to rack up the points.");
							}
						}, (long)(8 * 1000));
						faceOffTimer.schedule(new TimerTask() {
							public void run() { 
								sendMessage(CONFIG.getValue(CONF_CHANNEL), "When only one player can't answer, the player that submitted the acro " +
										"receives one point for every other player on the scoreboard automatically.");
							}
						}, (long)(16 * 1000));
						faceOffTimer.schedule(new TimerTask() {
							public void run() { 
								sendMessage(CONFIG.getValue(CONF_CHANNEL), "Missing just one acro could cost one of these players the game!");
							}
						}, (long)(24 * 1000));
					}
					else
						enterVoting(0);
				}
			}, (long)(35 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "10 seconds.");
				}
			}, (long)(55 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "5 seconds.");
				}
			}, (long)(60 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					acroRound = 2;
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "Time's up!  Here's your last acro: [ " + matchAcros[2] + " ].  30 seconds.");
					if (acros[1][0].equals("") && acros[1][1].equals("")) {
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "The second acro was [ " + matchAcros[1] + " ].  Neither player was able to answer " +
								"it, so there will be no points this round.");
						faceOffTimer.schedule(new TimerTask() {
							public void run() { 
								sendMessage(CONFIG.getValue(CONF_CHANNEL), "There won't be a do-over for this round-- that means our players only " +
										"get two more rounds to rack up the points.");
							}
						}, (long)(8 * 1000));
						faceOffTimer.schedule(new TimerTask() {
							public void run() { 
								sendMessage(CONFIG.getValue(CONF_CHANNEL), "When only one player can't answer, the player that submitted the acro " +
										"receives one point for every other player on the scoreboard automatically.");
							}
						}, (long)(16 * 1000));
						faceOffTimer.schedule(new TimerTask() {
							public void run() { 
								sendMessage(CONFIG.getValue(CONF_CHANNEL), "Missing just one acro could cost one of these players the game!");
							}
						}, (long)(24 * 1000));
					}
					else
						enterVoting(1);
				}
			}, (long)(65 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "10 seconds.");
				}
			}, (long)(85 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "5 seconds.");
				}
			}, (long)(90 * 1000));
			faceOffTimer.schedule(new TimerTask() {
				public void run() { 
					acroOn = false;
					for(int i = 0; i < 2; i++)
						sendMessage(getName(players[i]), "Time's up!  That's the end of the faceoff.  The other players have been " +
								"voting on your answers.  I'm giving them the answers from the last round now, then we'll find " +
								"out who wins!");
					if (acros[2][0].equals("") && acros[2][1].equals("")) {
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "The last acro was [ " + matchAcros[2] + " ]. Neither player was able to answer " +
								"it, so there will be no points this round.");
						sleep(4);
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "That ends the faceoff!");
						showResults();
					}
					else {
						enterVoting(2);
					}
				}
			}, (long)(95 * 1000));
		}
	}
	
	// Game constants
	private final int FACEOFF_AT = 30;
	
	// Game modes
	private final int ACRO_OFF = 0;
	private final int ACRO_PAUSED = 1;
	private final int ACRO_GETACROS = 2;
	private final int ACRO_GETVOTES = 3;
	private final int ACRO_FACEOFF = 4;
	private int gameMode = ACRO_OFF;
	
	// Game data
	private Hashtable<String, Integer> playerIndex = new Hashtable<String, Integer>();
	private Hashtable<Integer, String> playerName = new Hashtable<Integer, String>();
	private ArrayList<PlayerData> playerData = new ArrayList<PlayerData>();
	private VoteObject[] voteObjects;
	private int numLetters, curRound, submitOrder, noAnswerCount;
	private final String alpha = "AAAABBBBCCCCDDDDEEEEEFFFFGGGGHHHHIIIIJJJJKKLLLLMMMMNNNNOOOPPPPQQRRRSSSSTTTTUVWWXYZ";
	private String matchAcro;
	private Timer acroTimer, voteTimer;
	private FaceOff faceOff;
	
	private String startHelp = "Usage: " + PREFIX + "acrostart\nStarts the Acrophobia game from a clean slate.";
	private String stopHelp = "Usage: " + PREFIX + "acrostop\nStops the Acrophobia game.";
	private String instructionsHelp = "Usage: " + PREFIX + "instructions\nExplains how to play Acrophobia.";
	
	private Command[] commands = new Command[] {
			new Command("acrostart", 'h', 'b', startHelp) {
				public void run(String channel, String sender, String login, String hostname, String request) {
					gameStart(sender);
				}
			},
			new Command("acrostop", 'h', 'b', stopHelp) {
				public void run(String channel, String sender, String login, String hostname, String request) {
					gameStop(sender);
				}
			},
			new Command("instructions", 'p', 'b', instructionsHelp) {
				public void run(String channel, String sender, String login, String hostname, String request) {
					sendMessage(sender, Colors.BOLD + "Acrophobia Instructions:");
					sendMessage(sender, "In Acrophobia, I will give the channel an acronym, such as EMC.  Each player " +
							"then gets 60 seconds in which to whisper me a phrase that fits the acronym.  For example:\n" +
							"/msg " + BOT.getNick() + " Eat more chicken!\n" +
							"After 60 seconds, I'll post a list of the submissions and each one will have a number.  You vote " +
							"for your favorite by /msg'ing me the number of the one you want to vote for -- it can't be your own!\n" +
							"You get 1 point for being the fastest answer to get a vote, 1 point for voting for the winning answer, " +
							"and one point for each vote your answer receives.\n" +
							"When the first person hits " + FACEOFF_AT + ", the face-off round starts to determine the winner!");
				}
			}
	};

	public ModuleData getModuleData() {
		return new ModuleData("Acrophobia", (float)0.2, "Tom Frost", "A clone of the classic 1995 Acrophobia game.",
				new String[] {}, new float[] {}, commands);
	}

	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		if (gameMode != ACRO_OFF) {
			switch (gameMode) {
			case ACRO_GETACROS: acroSubmit(sender, message); break;
			case ACRO_GETVOTES: voteSubmit(sender, message); break;
			case ACRO_FACEOFF: faceoffSubmit(sender, message); break;
			}
		}
	}
	public void onNickChange(String oldNick, String login, String hostname, String newNick) {
		if (gameMode != ACRO_OFF) {
			if (playerIndex.containsKey(oldNick)) {
				Integer temp = playerIndex.get(oldNick);
				playerIndex.remove(oldNick);
				playerIndex.put(newNick, temp);
				playerData.get(temp.intValue()).name = newNick;
				playerName.put(temp, newNick);
			}
		}
	}
	
	
	/* ****************
	 * GAME CONTROLLERS
	 * ****************
	 */
	
	public synchronized void gameStart(String sender) {
		if (gameMode == ACRO_OFF) {
			gameMode = ACRO_PAUSED;
			
			// Initialize timers...
			acroTimer = new Timer();
			voteTimer = new Timer();
			
			playerIndex = new Hashtable<String, Integer>();
			playerData = new ArrayList<PlayerData>();
			numLetters = 7;
			curRound = 0;
			noAnswerCount = 0;
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "Welcome to Acrophobia!  The game is about to begin.  Anyone can jump in at any time.");
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "If you don't know how to play, type: /msg " + BOT.getNick() + " " + PREFIX + "instructions");
			sleep(10);
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "Ok, let's get started!");
			sleep(4);
			enterRound();
		}
		else
			sendMessage(sender, "Acrophobia is currectly running.");
	}
	
	public void gameStop(String sender) {
		if (gameMode != ACRO_OFF) {
			if (gameMode == ACRO_FACEOFF)
				faceOff.die();
			acroTimer.cancel();
			voteTimer.cancel();
			gameMode = ACRO_OFF;
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "Acrophobia has been stopped.  See you next time!");
		}
		else
			sendMessage(sender, "Acrophobia is not currently running.");
	}
	
	private synchronized void beginNewGame() {
		if (gameMode != ACRO_OFF) {
			gameMode = ACRO_PAUSED;
			
			// Initialize timers...
			acroTimer = new Timer();
			voteTimer = new Timer();
			
			playerIndex = new Hashtable<String, Integer>();
			playerData = new ArrayList<PlayerData>();
			numLetters = 7;
			curRound = 0;
			noAnswerCount = 0;
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "The next Acrophobia game is about to begin!  Anyone can jump in at any time.");
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "If you don't know how to play, type: /msg " + BOT.getNick() + " " + PREFIX + "instructions");
			sleep(10);
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "Ok, let's get started!");
			sleep(4);
			enterRound();
		}
	}
	
	private synchronized void enterRound() {
		if (gameMode != ACRO_OFF) {
			curRound++;
			submitOrder = 0;
			numLetters = rotate(3, 7, numLetters);
			matchAcro = getAcro(numLetters);
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "This round's Acro is: [ " + matchAcro + " ]. You have 60 seconds!");
			gameMode = ACRO_GETACROS;
			acroTimer = new Timer();
			acroTimer.schedule(new TimerTask() {
					public void run() { announceTime(30); }
				}, 30 * 1000);
			acroTimer.schedule(new TimerTask() {
				public void run() { announceTime(20); }
			}, 40 * 1000);
			acroTimer.schedule(new TimerTask() {
				public void run() { announceTime(10); }
			}, 50 * 1000);
			acroTimer.schedule(new TimerTask() {
				public void run() { announceTime(5); }
			}, 55 * 1000);
			acroTimer.schedule(new TimerTask() {
				public void run() { announceTime(3); }
			}, 57 * 1000);
			acroTimer.schedule(new TimerTask() {
				public void run() { announceTime(1); }
			}, 59 * 1000);
			acroTimer.schedule(new TimerTask() {
				public void run() {
					if (gameMode != ACRO_OFF) {
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "Time's up!");
						gameMode = ACRO_PAUSED;
						startVoting();
					}
				}
			}, 60 * 1000);
		}
	}
	
	private synchronized void startVoting() {
		ArrayList<String> acros = new ArrayList<String>();
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		ArrayList<Integer> order = new ArrayList<Integer>();
		PlayerData player;
		for (int i = 0; i < playerData.size(); i++) {
			player = playerData.get(i);
			if (player.lastAcroRound == curRound) {
				acros.add(player.acro);
				indicies.add(new Integer(i));
				order.add(new Integer(player.submitOrder));
			}
		}
		
		voteObjects = new VoteObject[order.size()];
		for (int i = 0; i < voteObjects.length; i++) {
			voteObjects[i] = new VoteObject();
			voteObjects[i].voters = new ArrayList<Integer>();
			voteObjects[i].acro = acros.get(i);
			voteObjects[i].playerIndex = indicies.get(i).intValue();
			voteObjects[i].submitOrder = order.get(i).intValue();
			voteObjects[i].votes = 0;
		}
		randomizeVoteObjects();
		
		sendMessage(CONFIG.getValue(CONF_CHANNEL), "Here's this round's submissions:");
		for (int i = 0; i < order.size(); i++)
			sendMessage(CONFIG.getValue(CONF_CHANNEL), (i+1) + " |  " + voteObjects[i].acro);
		
		if (order.size() > 1) {
			noAnswerCount = 0;
			sleep(2);
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "It's voting time! /msg " + BOT.getNick() + " the number of your vote.  You can't vote for yourself!");
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "Polls are open for 45 seconds.");
			gameMode = ACRO_GETVOTES;
			voteTimer = new Timer();
			voteTimer.schedule(new TimerTask() {
				public void run() { announceTime(10); }
			}, 35 * 1000);
			voteTimer.schedule(new TimerTask() {
				public void run() { 
					if (gameMode != ACRO_OFF) {
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "The voting booths are closed!");
						gameMode = ACRO_PAUSED;
						processResults();
					}
				}
			}, 45 * 1000);
		}
		else if (order.size() == 1) {
			noAnswerCount = 0;
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "We only have one submission, so let's move straight to the results!");
			gameMode = ACRO_PAUSED;
			sleep(1);
			processResults();
		}
		else {
			noAnswerCount++;
			if (noAnswerCount > 1) {
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "No one seems to be answering!  I'll turn the game off now.");
				gameMode = ACRO_OFF;
			}
			else {
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "No answers?  I'll move on to the next round.");
				sleep(4);
				enterRound();
			}
		}
	}
	
	private synchronized void processResults() {
		if (gameMode != ACRO_OFF) {
			boolean toFaceoff = false;
			
			sleep(4);
			if (voteObjects.length == 1) {
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "The answer was submitted by " + getName(voteObjects[0].playerIndex) + 
						".  Five points are awarded for being the only answer, plus the first place bonus of a point per letter!");
				playerData.get(voteObjects[0].playerIndex).points += 5 + numLetters;
			}
			else {
				int topScore = 0;
				ArrayList<Integer> topPlayers = new ArrayList<Integer>(); 
				int fastestOrder = 1000;  // Some arbitary number that's far greater than the max number of players.
				int fastestPlayer = -1;
				ArrayList<Integer> votedTop = new ArrayList<Integer>();
				
				for (int i = 0; i < voteObjects.length; i++) {
					if (voteObjects[i].votes > 0) {
						if (voteObjects[i].votes > topScore) {
							topScore = voteObjects[i].votes;
							topPlayers.clear();
							votedTop.clear();
							topPlayers.add(new Integer(voteObjects[i].playerIndex));
							for (int q = 0; q < voteObjects[i].voters.size(); q++)
								votedTop.add(voteObjects[i].voters.get(q));
						}
						else if (voteObjects[i].votes == topScore) {
							topPlayers.add(new Integer(voteObjects[i].playerIndex));
							for (int q = 0; q < voteObjects[i].voters.size(); q++)
								votedTop.add(voteObjects[i].voters.get(q));
						}
						
						if (voteObjects[i].submitOrder < fastestOrder) {
							fastestOrder = voteObjects[i].submitOrder;
							fastestPlayer = voteObjects[i].playerIndex;
						}
					}
				}
				
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "Here's who submitted the answers, and how many votes they got!");
				sleep(3);
				printAcros();
				sleep(7);
				
				ArrayList<Integer> noVoters = new ArrayList<Integer>();
				for (int i = 0; i < voteObjects.length; i++) {
					if (voteObjects[i].votes > 0) {
						if (topPlayers.contains(new Integer(voteObjects[i].playerIndex)))
							voteObjects[i].votes += numLetters;
						if (voteObjects[i].playerIndex == fastestPlayer)
							voteObjects[i].votes += 2;
						if (!awardPoints(voteObjects[i].playerIndex, voteObjects[i].votes))
							noVoters.add(new Integer(voteObjects[i].playerIndex));
					}
				}
				for (int i = 0; i < votedTop.size(); i++)
					playerData.get(votedTop.get(i).intValue()).points++;
				
				String plural = "";
				String plural2 = "";
				String plural3 = "";
				String plural4 = "s";
				String verb = "is";
				if (topScore > 0) {
					if (topPlayers.size() > 1) {
						plural = "s";
						verb = "are";
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "There was a tie for first!");
					}
					String winners = "";
					String topVoters = "";
					for (int i = 0; i < topPlayers.size(); i++)
						winners += ", " + getName(topPlayers.get(i).intValue());
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "The winner" + plural + " " + verb + ": " + winners.substring(2) + "!");
					sleep(4);
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "The winner" + plural + " will receive " + numLetters + " bonus points this round.");
					sleep(6);
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "The fastest acro submitted that received a vote was " + getName(fastestPlayer) + "'s.  " +
							"2 bonus points will be awarded to this player.");
					for (int i = 0; i < votedTop.size(); i++)
						topVoters += ", " + getName(votedTop.get(i).intValue());
					if (votedTop.size() > 1)
						plural2 = "s";
					sleep(8);
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "The following player" + plural2 + " voted for the winning answer" + plural + 
							" and will be awarded one bonus point: " + topVoters.substring(2));
					sleep(8);
					if (noVoters.size() > 0) {
						if (noVoters.size() > 1) {
							plural3 = "s";
							plural4 = "";
						}
						String baddies = "";
						for (int i = 0; i < noVoters.size(); i++)
							baddies += ", " + getName(noVoters.get(i).intValue());
						sendMessage(CONFIG.getValue(CONF_CHANNEL), "The following player" + plural3 + " did not vote, and forfeit" + plural4 + " their points for " +
								"this round: " + baddies.substring(2) + ".");
						sleep(8);
					}
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "Let's check out the scoreboard!");
					sleep(3);
					toFaceoff = printScores();
					sleep(9);
				}
				else
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "No one voted!  No points are awarded this round.");
			}
			if (toFaceoff && gameMode != ACRO_OFF) {
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "Our top player has crossed the " + FACEOFF_AT + "-point mark.  It's time for the " +
						"Faceoff Round!");
				sleep(4);
				int[] topTwo = topTwoPlayers();
				sendMessage(CONFIG.getValue(CONF_CHANNEL), getName(topTwo[0]) + " and " + getName(topTwo[1]) + ", get ready.  I'll be giving " +
						"you the acronyms through private messages -- not in this channel.");
				sleep(8);
				faceOff = new FaceOff(topTwo[0], topTwo[1]);
				faceOff.begin();
			}
			else if (gameMode != ACRO_OFF) {
				sendMessage(CONFIG.getValue(CONF_CHANNEL), "Get ready for the next round!");
				sleep(8);
				enterRound();
			}
		}
	}
	
	/* *******************
	 * SUBMISSION HANDLERS
	 * *******************
	 */
	
	private void faceoffSubmit(String sender, String inStr) {
		if (gameMode == ACRO_FACEOFF)
			faceOff.submit(sender, inStr);
	}
	
	private void voteSubmit(String sender, String voteStr) {
		if (gameMode == ACRO_GETVOTES) {
			voteStr = voteStr.trim();
			if (voteStr.matches("\\d+")) {
				int vote = Integer.parseInt(voteStr) - 1;
				if (vote >= 0 && vote < voteObjects.length) {
					if (!playerIndex.containsKey(sender) || playerIndex.get(sender).intValue() != voteObjects[vote].playerIndex) {
						PlayerData player = getPlayerData(sender);
						
						boolean changedVote = false;
						if (player.lastVoteRound == curRound) {
							voteObjects[player.vote].votes--;
							voteObjects[player.vote].voters.remove(new Integer(player.index));
							changedVote = true;
						}
						
						player.lastVoteRound = curRound;
						player.vote = vote;
						
						voteObjects[vote].votes++;
						voteObjects[vote].voters.add(new Integer(player.index));
						if (changedVote)
							sendMessage(sender, "Changed vote to #" + (vote+1) + ": " + voteObjects[vote].acro);
						else
							sendMessage(sender, "Voted for #" + (vote+1) + ": " + voteObjects[vote].acro);
					}
					else
						sendMessage(sender, "You can't vote for yourself!");
				}
				else
					sendMessage(sender, "I don't have an acro by that number!");
			}
			else
				sendMessage(sender, "Votes are submitted by sending me a message with JUST the number you're " +
						"voting for!  Your last vote wasn't counted.");
		}
	}
	
	private void acroSubmit(String sender, String acro) {
		if (gameMode == ACRO_GETACROS) {
			if (matches(acro, matchAcro)) {
				PlayerData player = getPlayerData(sender);
				
				player.acro = acro;
				player.lastAcroRound = curRound;
				player.submitOrder = submitOrder;
				submitOrder++;
				sendMessage(sender, "Acro submitted!");
			}
			else {
				sendMessage(sender, "Your message does not fit the acro!  Submit one matching [" + matchAcro + "], or " +
						"type  /msg " + BOT.getNick() + " " + PREFIX + "instructions  to learn how to play.");
			}
		}
	}
	
	
	
	/* *********************
	 * RUDIMENTARY FUNCTIONS
	 * *********************
	 */
	
	/**
	 * Gets the PlayerData object associated with the specified player name.  If the
	 * player doesn't exist, a PlayerData object is created.
	 * 
	 *  @param playerNick The nickname of the player to retreive the data for
	 *  @return a PlayerData object associated with the player name.
	 */
	private PlayerData getPlayerData(String playerNick) {
		int index;
		boolean createNew = false;
		if (playerIndex.containsKey(playerNick))
			index = playerIndex.get(playerNick).intValue();
		else {
			index = playerData.size();
			playerIndex.put(playerNick, new Integer(index));
			playerName.put(new Integer(index), playerNick);
			playerData.add(new PlayerData());
			createNew = true;
		}
		
		PlayerData player = playerData.get(index);
		if (createNew) {
			player.name = playerNick;
			player.index = index;
			player.points = 0;
		}
		
		return player;
	}
	
	/**
	 * Gets the indicies of the two highest-scored players in the game so far.
	 * 
	 * @return an array of size 2, with the first person being the top player, and
	 * 			the second being the second-place player.  -1 in a field means that
	 * 			there is no player with a score above 0 for that position.
	 */
	private int[] topTwoPlayers() {
		ArrayList<PlayerData> scoreboard = new ArrayList<PlayerData>();
		int topScore = 0;
		int secondTop = 0;
		
		// Put all the players with 1 or more points into "scoreboard"
		for (int i = 0; i < playerData.size(); i++) {
			if (playerData.get(i).points > 0)
				scoreboard.add(playerData.get(i));
			if (playerData.get(i).points > topScore)
				topScore = playerData.get(i).points;
		}
		
		if (scoreboard.size() == 0) return new int[] {-1, -1};
		if (scoreboard.size() == 1) return new int[] {scoreboard.get(0).index, -1};
		
		// Put all the players from scoreboard into "sorted" from highest score, down.
		int index = 0;
		PlayerData[] sorted = new PlayerData[scoreboard.size()];
		while (scoreboard.size() != 0) {
			for (int i = scoreboard.size() - 1; i >= 0; i--) {
				if (scoreboard.get(i).points == topScore) {
					sorted[index] = scoreboard.remove(i);
					index++;
				}
				else if (scoreboard.get(i).points > secondTop && scoreboard.get(i).points < topScore)
					secondTop = scoreboard.get(i).points;
			}
			topScore = secondTop;
			secondTop = 0;
		}
		
		return new int[] {sorted[0].index, sorted[1].index};
	}
	
	/**
	 * Counts the number of players in the "scoreboard", which consists of all players and
	 * voters that have a score greater than zero.
	 * 
	 * @return the number of players in the scoreboard.
	 */
	private int playersInScoreboard() {
		int count = 0;
		
		for (int i = 0; i < playerData.size(); i++) {
			if (playerData.get(i).points > 0)
				count++;
		}
		return count;
	}
	
	/**
	 * Prints the player names with scores greater than zero in descending order.
	 * 
	 * @return <code>true</code> if the top score is 30 or greater; <code>false</code> otherwise.
	 */
	private boolean printScores() {
		int topScore = 0;
		int secondTop = 0;
		ArrayList<PlayerData> scoreboard = new ArrayList<PlayerData>();
		PlayerData[] sorted;
		boolean faceoff = false;
		
		// Put all the players with 1 or more points into "scoreboard"
		for (int i = 0; i < playerData.size(); i++) {
			if (playerData.get(i).points > 0)
				scoreboard.add(playerData.get(i));
			if (playerData.get(i).points > topScore)
				topScore = playerData.get(i).points;
		}
		
		// Are there even any scores to print?
		if (scoreboard.size() == 0) {
			sendMessage(CONFIG.getValue(CONF_CHANNEL), "There are no scores over 0 so far!");
			return false;
		}
		
		// Are we going to the faceoff with these scores?
		if (topScore >= FACEOFF_AT)
			faceoff = true;
		
		// Put all the players from scoreboard into "sorted" from highest score, down.
		int index = 0;
		sorted = new PlayerData[scoreboard.size()];
		while (scoreboard.size() != 0) {
			for (int i = scoreboard.size() - 1; i >= 0; i--) {
				if (scoreboard.get(i).points == topScore) {
					sorted[index] = scoreboard.remove(i);
					index++;
				}
				else if (scoreboard.get(i).points > secondTop && scoreboard.get(i).points < topScore)
					secondTop = scoreboard.get(i).points;
			}
			topScore = secondTop;
			secondTop = 0;
		}
		
		// Print the sorted array
		String print = "";
		for (int i = 0; i < sorted.length; i++)
			print += "  [" + sorted[i].name + " " + sorted[i].points + "]";
		sendMessage(CONFIG.getValue(CONF_CHANNEL), print.substring(2));
		
		return faceoff;
	}
	
	/**
	 * Prints each acro in the voteObjects array in the order of how many votes they received.
	 * Each line also includes the acro's author, and the number of votes it got.
	 */
	private void printAcros() {
		int topScore = 0;
		for (int i = 0; i < voteObjects.length; i++) {
			if (voteObjects[i].votes > topScore)
				topScore = voteObjects[i].votes;
		}
		String plural;
		for (int i = topScore; i >= 0; i--) {
			if (i == 1)
				plural = "";
			else
				plural = "s";
			for (int q = 0; q < voteObjects.length; q++) {
				if (voteObjects[q].votes == i)
					sendMessage(CONFIG.getValue(CONF_CHANNEL), "[" + getName(voteObjects[q].playerIndex) + " | " + i + " vote" + plural + "] " +
							voteObjects[q].acro);
			}
		}
	}
	
	
	/**
	 * Awards a number of points to a certain player, if and only if that
	 * player has voted in the current round.
	 * 
	 * @param player The index of the player to add points to
	 * @param points The number of points to add
	 * @return <code>true</code> is the points were added; <code>false</code> if
	 * the player did not vote and cannot receive points.
	 */
	private boolean awardPoints(int player, int points) {
		PlayerData temp = playerData.get(new Integer(player));
		if (temp.lastVoteRound == curRound) {
			temp.points += points;
			return true;
		}
		return false;
	}
	
	
	/**
	 * Gives a player's name, given their index.
	 * 
	 * @param num The player index
	 * @return The player's nickname
	 */
	private String getName(int num) {
		return playerName.get(new Integer(num));
	}
	
	
	/**
	 * Puts the voteObjects array elements in a pseudorandom order.
	 *
	 */
	private void randomizeVoteObjects() {
		ArrayList<VoteObject> temp = new ArrayList<VoteObject>();
		for (int i = 0; i < voteObjects.length; i++)
			temp.add(voteObjects[i]);
		
		int rand;
		for (int i = 0; i < voteObjects.length; i++) {
			rand = (int)(Math.random() * temp.size());
			voteObjects[i] = temp.remove(rand);
		}
	}
	
	
	/**
	 * Increments an integer between a min and max value.  If the max has been reached,
	 * the value will start over at the min.
	 * 
	 * @param min The minimum value to cycle through
	 * @param max The maximum value to cycle through
	 * @param cur The current value
	 * @return The incremented value
	 */
	private int rotate(int min, int max, int cur) {
		if (cur < max)
			return cur + 1;
		return min;
	}
	
	
	/**
	 * Tests to see if a string matches the provided acronym.
	 * 
	 * @param phrase The phrase to match to the acronym.
	 * @param acro The acronym that the phrase must match.
	 * @return <code>true</code> if the phrase matches; <code>false</code> otherwise.
	 */
	private boolean matches(String phrase, String acro) {
		phrase = phrase.trim().toLowerCase();
		char[] str = phrase.toCharArray();
		String clean = "";
		for (int i = 0; i < str.length; i++) {
			if (str[i] >= 'a' && str[i] <= 'z')
				clean += str[i];
			if (str[i] == ' ' && clean.charAt(clean.length() - 1) != ' ')
				clean += str[i];
		}
		String[] split = clean.trim().split(" ");
		if (split.length != acro.length())
			return false;
		for (int i = 0; i < acro.length(); i++) {
			if (split[i].charAt(0) != acro.toLowerCase().charAt(i))
				return false;
		}
		return true;
	}
	
	
	/**
	 * Announces a number of seconds remaining, taking stylistic traits into
	 * consideration.
	 * 
	 * @param seconds Number of seconds remaining.
	 */
	private void announceTime(int seconds) {
		if (gameMode != ACRO_OFF) {
			String plural = "";
			String ending = "";
			if (seconds > 1)
				plural = "s";
			if (seconds > 5)
				ending = " left";
			sendMessage(CONFIG.getValue(CONF_CHANNEL), seconds + " second" + plural + ending + "!");
		}
	}
	
	
	/**
	 * Generates an acronym from a weighted alphabet string with the specified
	 * number of letters.
	 * 
	 * @param num Number of letters in the acronym.
	 * @return An acronym
	 */
	private String getAcro(int num) {
		String acro = "";
		for (int i = 0; i < num; i++)
			acro += alpha.charAt((int)(Math.random() * alpha.length()));
		return acro;
	}
	
	
	/**
	 * Shortcut to letting the bot pause.
	 * 
	 * @param seconds Number of seconds to wait before continuing.
	 */
	private void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		}
		catch (InterruptedException e) {}
	}
}

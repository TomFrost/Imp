/**
 * BasicTextUI.java
 * Tom Frost
 * April 4, 2007
 */
package com.seriouslyslick.imp;

import java.util.ArrayList;

/**
 * Provides an extremely basic user interface for altering the bot's configuration.
 * Basic as this interface is, it is guaranteed to work on all platforms, as well as
 * over Telnet and SSH.
 * 
 * @author Tom Frost
 */
public class BasicTextUI implements ConfigurationUI {

	public void start(ConfigurationManager configurationManager) {
		ArrayList<ConfigurationItem> items = configurationManager.getItemList();
		String sel;
		System.out.println("\nHi there, and welcome to Imp.\n\nThis is the Imp configuration editor.  On the " +
				"next screen, you'll see the different configuration values for Imp.  You can set or get help " +
				"on any one of the values, just by typing the appropriate commands.  When you're done, the " +
				"configuration file will be saved, and Imp will load with the specified configuration.\n\n " +
				"Press the enter key to begin.\n\n\n");
		Keyboard.readLine();
		do {
			for (int i = 0; i < items.size(); i++)
				System.out.println(makeLength(i + "", 4) + "| " + makeLength(items.get(i).getName(), 25) + 
						"| " + items.get(i).getValue());
			System.out.println("\nType 'exit' to quit, 'save' to save and continue, 'help #' for help on a number, or 'set # [value]' to change a number's value.");
			do {
				System.out.print("Your selection: ");
				sel = Keyboard.readLine();
				if (!sel.matches("(?i)((help \\d+)|exit|save|(set \\d+( .*)?))"))
					System.out.println("I didn't understand that.. Read those instructions again.");
				else {
					String[] split = sel.split(" ", 3);
					if (split[0].equalsIgnoreCase("help")) {
						int num = Integer.parseInt(split[1]);
						if (num < 0 && num >= items.size()) {
							System.out.println("There's no item number " + num + "!\n\nPress enter to continue");
							Keyboard.readLine();
						}
						else {
							System.out.println("\n" + items.get(num).getDesc() + "\n\nPress enter to continue.");
							Keyboard.readLine();
						}
					}
					else if (split[0].equalsIgnoreCase("set")) {
						int num = Integer.parseInt(split[1]);
						if (num < 0 && num >= items.size()) {
							System.out.println("There's no item number " + num + "!\n\nPress enter to continue");
							Keyboard.readLine();
						}
						else {
							String set = "";
							if (split.length == 3)
								set = split[2];
							if (items.get(num).setValue(set))
								System.out.println("\n" + items.get(num).getName() + " is now set to " + set + ".");
							else
								System.out.println("\nSorry, that was not a legal value.  May I suggest the 'help' command?");
							System.out.println("\nPress the enter key to continue.");
							Keyboard.readLine();
						}
					}
				}
			} while (!sel.matches("(?i)((help \\d+)|exit|save|(set \\d+( .*)?))"));
		} while (!sel.matches("(?i)(exit|save)"));
		if (sel.equalsIgnoreCase("exit")) {
			System.out.println("Goodbye!");
			System.exit(0);
		}
		configurationManager.saveConfig();
		System.out.println("Configuration saved.");
	}
	
	private String makeLength(String orig, int length) {
		if (orig.length() > length)
			return orig.substring(0, length);
		if (orig.length() == length)
			return orig;
		else
			return makeLength(orig + " ", length);
	}
}

/**
 * Keyboard.java
 * Added Sep 21, 2006
 *
 * Tom Frost: I picked this up at college and it's been a nice little utility to have on hand since.
 * The professor I got it from isn't even aware of the author, but I've been assured the class is
 * completely free.
 */
package com.seriouslyslick.imp;

import java.io.*;

/**
 * This is a utility class that allows easy input of integers, doubles, and
 * lines without forcing the client to deal with Streams & Readers.
 * 
 * @author Unknown
 * @since 1.0
 * @version 1.0
 */
public class Keyboard {
	private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	// -------------------------------------------------------------

	/**
	 * The next line entered at the keyboard is returned.
	 */
	public static String readLine() {
		try {
			return br.readLine();
		}
		catch (IOException e) {
			throw new InternalError("Fatal input error in Keyboard.java");
		}
	}

	// ----------------------------------------------------------------

	/**
	 * The user should enter an integer followed by the Enter-key. If a legal
	 * integer is entered, that integer is returned. Otherwise, an error message
	 * is output. When the user eventually enters a legal integer, that integer
	 * is returned.
	 */
	public static int readInt() {
		String next = "";
		while (true) {
			try {
				next = readLine().trim();
				if (!next.equals(""))
					return Integer.parseInt(next);
			}
			catch (NumberFormatException nfe) {
				System.out.println();
				System.out.println("***  IO exception : cannot convert " + next + " to integer  ***");
				System.out.println("***  " + next + " is ignored. " + "Please re-enter.  ***");
				System.out.println();
			}
		}
	}

	// ------------------------------------------------------------

	/**
	 * The user should enter a double followed by the Enter-key. If a legal
	 * double is entered, that double is returned. Otherwise, an error message
	 * is output. When the user eventually enters a legal double, that double is
	 * returned.
	 */
	public static double readDouble() {
		String next = "";
		while (true) {
			try {
				next = readLine().trim();
				if (!next.equals(""))
					return Double.parseDouble(next);
			}
			catch (NumberFormatException nfe) {
				System.out.println();
				System.out.println("***  IO exception : cannot convert " + next + " to double.  ***");
				System.out.println("***  " + next + " is ignored. " + "Please re-enter.  ***");
				System.out.println();
			}
		}
	}
}

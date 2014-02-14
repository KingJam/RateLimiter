package com.bruceslawson.ratelimiter;

/**
 *  @author Bruce Slawson &lt;bruce@bruceslawson.com&gt;
 *
 */
public class DebugPrinter {

	/**
	 * A very basic logger that prints to stdout
	 * 
	 * @param message The message to print
	 */
	protected static void print(Class<?> theClass, String message) {
		System.out.println( theClass.getName() + "> " + message);
	}
	
}

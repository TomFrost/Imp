/**
 * ConfigurationUI.java
 * Tom Frost
 * April 4, 2007
 */
package com.seriouslyslick.imp;

/**
 * Provides an incredibly simple interface with which to make user interfaces for the bot configuration.
 * 
 * @author Tom Frost
 */
public interface ConfigurationUI {
	
	/**
	 * Launches the configuration user interface.
	 *
	 */
	public void start(ConfigurationManager configurationManager);
}

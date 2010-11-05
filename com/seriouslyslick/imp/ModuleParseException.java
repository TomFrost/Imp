/**
 * ModuleParseException.java
 * Tom Frost
 * Sep 10, 2006
 *
 * 
 */
package com.seriouslyslick.imp;


/**
 * @author Tom Frost
 *
 */
public class ModuleParseException extends RuntimeException {
	static final long serialVersionUID = 1;
	
	public ModuleParseException(String reason) {
		super(reason);
	}
}

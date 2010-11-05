/**
 * ConfigurationItem.java
 * Tom Frost
 * Sep 19, 2006
 *
 * 
 */
package com.seriouslyslick.imp;


/**
 * 
 * The ConfigurationItem class holds one custom, user-defined value, as well as what the value
 * is for along with a more detailed description of how it's used and what kinds of values
 * are legal.  A default value is also included, to both guide users as well as not
 * break the system if an item is not configured.
 * <p>
 * This form is very simple, and the only abstract method is <code>boolean isLegal()</code>.
 * Often, isLegal will simply just return <code>true</code> if any type of string is accepted, or
 * return <code>val.matches("\\S*")</code> (where "\\S*" is any regex string that the imput must
 * match) to keep a more firm grip on the type of input.  If this method returns <code>false</code>,
 * the user will be asked to try again until it returns true.
 * 
 * @author Tom Frost
 * @since 1.0
 * @version 1.0 alpha
 */
public abstract class ConfigurationItem {
	private String itemName, itemDesc, value, defaultValue;
	
	/**
	 * Creates a new ConfigurationItem.
	 * 
	 * @param itemName The title of this configuration value.  Such as "Server name" or "Master password".
	 * 				Must be unique.
	 * @param itemDesc A more detailed description of this configuration item, including proper formatting
	 * 				if necessary.
	 * @param defaultValue The default value for this item.  Cannot be null.
	 */
	public ConfigurationItem(String itemName, String itemDesc, String defaultValue) {
		if (defaultValue == null)
			throw new IllegalArgumentException("Default value for configuration item '" + itemName + "' cannot be null.");
		
		this.itemName = itemName;
		this.itemDesc = itemDesc;
		this.value = defaultValue;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Tests if the supplied value is a legal value for this item.
	 * 
	 * @param val The value to test.
	 * @return <code>true</code> if the value is legal; <code>false</code> otherwise.
	 * 
	 * @since 1.0
	 */
	public abstract boolean isLegal(String val);
	
	/**
	 * Gets the item name as supplied in the constructor.
	 * 
	 * @return the item name.
	 * 
	 * @since 1.0
	 */
	public final String getName() {
		return itemName;
	}
	
	/**
	 * Gets the item description as supplied in the constructor.
	 * 
	 * @return the item description.
	 * 
	 * @since 1.0
	 */
	public final String getDesc() {
		return itemDesc;
	}
	
	/**
	 * Gets the current value associated with this configuration item.
	 * 
	 * @return the item's current value.
	 * 
	 * @since 1.0
	 */
	public final String getValue() {
		return value;
	}
	
	/**
	 * Gets the default value assigned to this item as supplied in the constructor.  This value
	 * may or may not match the current value of the item.
	 * 
	 * @return the item's default value.
	 * 
	 * @since 1.0
	 */
	public final String getDefault() {
		return defaultValue;
	}
	
	/**
	 * Sets the value of this item back to the default value as supplied in the constructor.
	 *
	 * @since 1.0
	 */
	public final void setToDefault() {
		value = defaultValue;
	}
	
	/**
	 * Sets the value of this item to the supplied value if and only if the value is legal according to
	 * the {@link isLegal(String)} method.
	 * 
	 * @param val The new value to assign to this configuration item.
	 * @return <code>true</code> if the value has been successfully set, <code>false</code> if it was not
	 * set due to invalidity.
	 * 
	 * @since 1.0
	 */
	public final boolean setValue(String val) {
		if (isLegal(val)) {
			value = val;
			return true;
		}
		return false;
	}
	
	/**
	 * Returns a String representation of this object.  In this case, the value held by the configuration
	 * item is returned.
	 * @return A String representation of this ConfigurationItem.
	 * 
	 * @since 1.0
	 */
	public final String toString() {
		return value;
	}

	/**
	 * Checks to see if the supplied object is equal to this ConfigurationItem.
	 * 
	 * @returns <code>true</code> if all the elements of this ConfigurationItem match the supplied
	 * object; <code>false</code> otherwise, or if the supplied object is not a ConfigurationItem.
	 */
	public final boolean equals(Object e) {
		if (e instanceof ConfigurationItem)
			return itemName.equals(((ConfigurationItem)e).getName()) &&
				value.equals(((ConfigurationItem)e).getValue()) &&
				itemDesc.equals(((ConfigurationItem)e).getDesc()) &&
				defaultValue.equals(((ConfigurationItem)e).getDefault());
		return false;
	}
}

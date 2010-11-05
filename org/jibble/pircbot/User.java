/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of PircBot.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: User.java,v 1.24 2004/10/14 18:10:18 pjm2 Exp $

** Updated in various ways by Tom Frost for use in the IMP IRC Bot.
** Updates are from 2006 forward.

*/

package org.jibble.pircbot;

import java.io.Serializable;

/**
 * This class is used to represent a user on an IRC server.
 * Instances of this class are returned by the getUsers method
 * in the PircBot class.
 *  <p>
 * Note that this class no longer implements the Comparable interface
 * for Java 1.1 compatibility reasons.
 *
 * @since   1.0.0
 * @author  Paul James Mutton,
 *          <a href="http://www.jibble.org/">http://www.jibble.org/</a>
 *          Extended functionality added by Tom Frost
 *          <a href="http://www.frosteddesign.com">http://www.frosteddesign.com</a>
 * @version    1.4.5 (Build time: Tue Mar 29 20:58:46 2005)
 */
public class User implements Cloneable, Serializable {
	
	static final long serialVersionUID = 1;
	
	/**
     * Constructs a User object with a known prefix and nick.
     * Login and hostname remain null.
     *
     * @param prefix The status of the user, for example, "@".
     * @param nick The nick of the user.
     */
    public User(String prefix, String nick) {
        _prefix = prefix;
        _nick = nick;
        _login = null;
        _hostname = null;
        _lowerNick = nick.toLowerCase();
    }
	
	
    /**
     * Constructs a User object with a known prefix, nick, login, and hostname.
     *
     * @param prefix The status of the user, for example, "@".
     * @param nick The nick of the user.
     * @param login The login of the user.
     * @param hostname The hostname of the user.
     */
    public User(String prefix, String nick, String login, String hostname) {
        _prefix = prefix;
        _nick = nick;
        _login = login;
        _hostname = hostname;
        _lowerNick = nick.toLowerCase();
    }
    
    
    /**
     * Returns the prefix of the user. If the User object has been obtained
     * from a list of users in a channel, then this will reflect the user's
     * status in that channel.
     *
     * @return The prefix of the user. If there is no prefix, then an empty
     *         String is returned.
     */
    public String getPrefix() {
        return _prefix;
    }
    
    
    /**
     * Sets the prefix of the user.
     *
     * @param prefix The user's new prefix.
     */
    public void setPrefix(String prefix) {
    	_prefix = prefix;
    }
    
    
    /**
     * Returns whether or not the user represented by this object is an
     * owner. If the User object has been obtained from a list of users
     * in a channel, then this will reflect the user's operator status in
     * that channel.
     * 
     * @return true if the user is an owner in the channel.
     */
    public boolean isOwner() {
        return _prefix.indexOf('~') >= 0;
    }
    
    
    /**
     * Returns whether or not the user represented by this object is an
     * administrator. If the User object has been obtained from a list of users
     * in a channel, then this will reflect the user's operator status in
     * that channel.
     * 
     * @return true if the user is an administrator in the channel.
     */
    public boolean isAdmin() {
        return _prefix.indexOf('&') >= 0;
    }
    
    
    /**
     * Returns whether or not the user represented by this object is an
     * operator. If the User object has been obtained from a list of users
     * in a channel, then this will reflect the user's operator status in
     * that channel.
     * 
     * @return true if the user is an operator in the channel.
     */
    public boolean isOp() {
        return _prefix.indexOf('@') >= 0;
    }
    
    
    /**
     * Returns whether or not the user represented by this object is a
     * half-operator. If the User object has been obtained from a list of users
     * in a channel, then this will reflect the user's operator status in
     * that channel.
     * 
     * @return true if the user is an operator in the channel.
     */
    public boolean isHop() {
        return _prefix.indexOf('%') >= 0;
    }
    
    
    /**
     * Returns whether or not the user represented by this object has
     * voice. If the User object has been obtained from a list of users
     * in a channel, then this will reflect the user's voice status in
     * that channel.
     * 
     * @return true if the user has voice in the channel.
     */
    public boolean hasVoice() {
        return _prefix.indexOf('+') >= 0;
    }        
    
    
    /**
     * Returns the nick of the user.
     * 
     * @return The user's nick.
     */
    public String getNick() {
        return _nick;
    }
    
    
    /**
     * Sets the nick of the user.
     * 
     * @param nick The user's new nick.
     */
    public void setNick(String nick) {
        _nick = nick;
        _lowerNick = nick.toLowerCase();
    }
    
    
    /**
     * Returns the login of the user.
     * 
     * @return The user's login.
     */
    public String getLogin() {
        return _login;
    }
    
    
    /**
     * Returns the hostname of the user.
     * 
     * @return The user's hostname.
     */
    public String getHostname() {
        return _hostname;
    }
    
    
    /**
     * Returns the nick of the user complete with their prefix if they
     * have one, e.g. "@Dave".
     * 
     * @return The user's prefix and nick.
     */
    public String toString() {
        return this.getPrefix() + this.getNick();
    }
    
    
    /**
     * Returns true if the nick represented by this User object is the same
     * as the argument. A case insensitive comparison is made.
     * 
     * @return true if the nicks are identical (case insensitive).
     */
    public boolean equals(String nick) {
        return nick.toLowerCase().equals(_lowerNick);
    }
    
    
    /**
     * Returns true if the Object passed to this method is both a User object, and each of its
     * fields are equal to that of this User object as determined by their respective .equals()
     * methods.
     * 
     * @return <code>true</code> if o is equal to this User object; <code>false</code> otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof User) {
            User other = (User) o;
            return other.getNick().equals(_nick) && other.getPrefix().equals(_prefix) &&
            	other.getLogin().equals(_login) && other.getHostname().equals(_hostname);
        }
        return false;
    }
    
    
    /**
     * Returns the hash code of this User object.
     * 
     * @return the hash code of the User object.
     */
    public int hashCode() {
        return (_prefix + _nick + "!" + _login + "@" + _hostname).hashCode();
    }
    
    
    /**
     * Returns the result of calling the compareTo method on lowercased
     * nicks. This is useful for sorting lists of User objects.
     * 
     * @return the result of calling compareTo on lowercased nicks.
     */
    public int compareTo(Object o) {
        if (o instanceof User) {
            User other = (User) o;
            return other._lowerNick.compareTo(_lowerNick);
        }
        return -1;
    }
    
    /**
     * Returns a clone of this User object.  Its data field remain the same, but it will be
     * allocated in an entirely different memory space.
     * 
     * @return A clone of this object.
     */
    public User clone() {
    	return new User(_prefix, _nick, _login, _hostname);
    }
    
    private String _prefix;
    private String _nick;
    private String _login;
    private String _hostname;
    private String _lowerNick;
}
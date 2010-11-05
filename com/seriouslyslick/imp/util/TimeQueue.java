/**
 * TimeQueue.java
 * Tom Frost
 * Separate class created March 23, 2007
 * Original code created August 30, 2006
 * 
 * TimeQueue is a method of tracking how many events happen within a set
 * time period.
 * 
 * For example, let's say that there's a bot in a chat channel that
 * should ban someone when they say three expletives within five minutes.
 * TimeQueue makes this incredibly easy.  Create a new TimeQueue object with
 * a limit of 3, with seconds set to 5 * 60.  Every time a particular use says an
 * expletive, call stamp() on the TimeQueue object.  If stamp() returns true,
 * the user should be kicked.  If it returns false, they've not yet reached
 * the limit.  It's as simple as that!
 */
package com.seriouslyslick.imp.util;

import java.util.Date;

/**
 * @author Tom Frost
 *
 */
public class TimeQueue {
	
	private int limit;
	private long seconds;
	private int nodes;
	private TimeQueueNode head, tail;
	
	/**
	 * Creates a new TimeQueue object with the specified limit on number of
	 * events to occur within a set time frame.
	 * 
	 * @param limit Number of events to allow in the time frame before stamp() returns true.
	 * @param minutes The number of seconds to constrain the specified number of events.
	 */
	public TimeQueue(int limit, long seconds) {
		this.limit = limit - 1;
		this.seconds = seconds;
		nodes = 0;
	}
	
	/**
	 * Resets the TimeQueue object back to zero events.
	 *
	 */
	public void reset() {
		head = null;
		tail = null;
		nodes = 0;
	}
	
	/**
	 * Stamps the TimeQueue object.  This method is to be called whenever the event to be
	 * tracked occurs.
	 * 
	 * @return <code>true</code> if this stamp exceeds the allowed number of stamps in the
	 * 			given timeframe; <code>false</code> otherwise.
	 */
	public boolean stamp() {
		if (nodes == 0) {
			head = new TimeQueueNode();
			tail = head;
			nodes++;
			return false;
		}
		else if (nodes < limit) {
			tail.next = new TimeQueueNode();
			tail = tail.next;
			nodes++;
			return false;
		}
		else {
			tail.next = new TimeQueueNode();
			tail = tail.next;
			TimeQueueNode temp = head;
			head = head.next;
			int elapsed = (int)(tail.getDate().getTime() - temp.getDate().getTime()) / 1000;
			if (elapsed <= seconds)
				return true;
			return false;
		}
	}
	
	private class TimeQueueNode {
		private Date timestamp;
		public TimeQueueNode next;
		public TimeQueueNode() {
			timestamp = new Date();
		}
		public Date getDate() {
			return timestamp;
		}
	}
}

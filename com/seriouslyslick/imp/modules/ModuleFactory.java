/**
 * ModuleFactory.java
 * Tom Frost
 * August 12, 2006
 * 
 * Provides a registration for all active modules.
 */
package com.seriouslyslick.imp.modules;

import com.seriouslyslick.imp.Module;

/**
 * @author Tom Frost
 *
 */
public class ModuleFactory {
	private static Module[] modules = null;
	
	public static Module[] getModules() {
		if (modules == null)
			reloadModules();
		return modules;
	}
	
	public static void reloadModules() {
		modules = new Module[] {
				new UserManager(),
				new Information(),
				new BanManager(),
				new SpamGuard(),
				new Harassment(),
				new RemoteControl(),
				new BadWords(),
				new NickServ(),
				new Acrophobia(),
				new ConnectMessage(),
				new Greeter()
				};
	}
}

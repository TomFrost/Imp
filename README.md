#IMP: IRC Modularized Preprocessor
####A single-channel IRC bot written in Java. Based on [PircBot](http://www.jibble.org/pircbot.php).
####Written by [Tom Frost](http://www.frosteddesign.com)

Stuff You Should Know
----------------------
I wrote Imp really hurredly back in college because I needed a way to manage an old IRC room.  While
Imp is very stable (my longest Imp instance is 1.5 years running and going strong at the time of
this writing) and still very extensible and useful, looking back at my years-old code with the
experience I have now makes me wonder what in the world I was thinking for some of this stuff.  So
if you intend to develop on Imp, you shouldn't have too hard of a time -- just accept my sincerest
apologies for the overly-nested logic and explosion of disheveled programming patterns ;-)

The only documentation for Imp is the Javadoc-style comments in the source.

Requirements
-------------
Java 5 or higher (Java 6 recommended).  You'll need the JDK-- the package with the compiler (javac) as
well as the runtime (java).

Installation
-------------
Assuming you're on a *nix-like OS and have the above requirements, you should be able to execute
the following lines to download and compile Imp.  Tweak as necessary for Windows.

    cd ~
	git clone http://github.com/TomFrost/Imp.git
    cd Imp
	javac com/seriouslyslick/imp/Imp.java

Now to execute Imp, type:

    cd ~/Imp
	java com.seriouslyslick.imp.Imp

This command will launch a text-based configuration utility that will allow you to specify the server and
channel to connect Imp to, as well as a myriad of other options.  Some options can be changed via IRC
messages while Imp is running, but not all.  This configuration will be the "default" config, and
will be automatically loaded the next time you start Imp.  To edit the default config, load Imp with
the line:

    java com.seriouslyslick.imp.Imp edit default

If you need multiple configurations, just make up a name for your new configuration and launch Imp
with that argument (in this example, the new config is called "altconfig"):

    java com.seriouslyslick.imp.Imp altconfig

Editing that in the future is the same as editing the default config:

    java com.seriouslyslick.imp.Imp edit altconfig

Support
--------
Informal support for Support for Imp is currently offered on
**[webdevRefinery.com](http://www.webdevrefinery.com)**.  Ask away in the Java forum!

Legal
------
This development release of Imp does not have a fancy official license.  In lieu of that, Imp is
free for personal, private, non-profit use.  Any usage that does not meet those three stipulations
requires my personal written permission.

Credits
--------
Imp was created 2005-2006 by [Tom Frost](http://www.frosteddesign.com).

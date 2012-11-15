/*
OOP with tradicional rythms
Aris Bezas 121115
*/
Rythm_Class {

	*initClass {
		StartUp add: {
			"========================".postln;
			"* Rythm Class ".postln;
			"========================".postln;
			this.bootTheServer;
		}
	}

	*bootTheServer	{
		if (not(Server.default.serverRunning)) { Server.default.boot };
		Server.default.doWhenBooted {
			this.loadTheBuffers;
			this.sendTheSynths;
		};
	}

	*loadTheBuffers	{

		~dum = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/bass_Drum_Single_Kick.aiff");
		~te = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/gonga_single_hit.aiff");
		~bell = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/agogo_bell.aiff");

	}
	*sendTheSynths	{
		SynthDef(\dum, { | out, freq = 440, amp = 1, bufnum = 10, pan = 0, gate = 1, roomsize = 5, revtime = 1.6, damping = 0.62, mulVerb = 1 |
			Out.ar(0,
				Pan2.ar(
					GVerb.ar(
						PlayBuf.ar(1,bufnum:bufnum, doneAction:2),
						roomsize,
						revtime,
						damping,
						mul: mulVerb
					)
					+
					PlayBuf.ar(1,bufnum:bufnum, doneAction:2),
					0, amp)
			);
		}).store;
	}
}

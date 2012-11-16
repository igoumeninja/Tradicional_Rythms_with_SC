/*
OOP with tradicional rythms
Aris Bezas 121115

Examples:
~bps = 2;
Rythms.xasapiko
~rythm.play;
Rythms.changeBPS(2)

------ MORE ---------
~instrumentPPatt.source = Pseq([\playBufGVerb], inf);
~bufnumPPatt.source = Pseq([~, ~te], inf);
~ampPPatt.source = Pseq([1, 0.5], inf);
~durPPatt.source = Pseq([1, 1]/2, inf);

*/
Rythm_Class {

	*initClass {
		StartUp add: {
			if (not(Server.default.serverRunning)) { Server.default.boot };
			Server.default.doWhenBooted {
				this.loadTheBuffers;
				this.sendTheSynths;
				this.defineProxyPattern;
				"\n|====================|".postln;
				"|Rythm Class is ready|".postln;
				"|====================|\n".postln;
			};
		}
	}

	*loadTheBuffers	{

		~dum = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/bass_Drum_Single_Kick.aiff");
		~te = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/gonga_single_hit.aiff");
		~bell = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/agogo_bell.aiff");

	}
	*sendTheSynths	{
		SynthDef(\simplePlayBuf, { | out, freq = 440, amp = 1, bufnum = 10, pan = 0, gate = 1 |
			Out.ar(0, Pan2.ar(PlayBuf.ar(1,bufnum:bufnum,doneAction:
				2), 0, amp));
		}).store;

		SynthDef(\playBufGVerb, { | out, freq = 440, amp = 1, bufnum = 10, pan = 0, gate = 1, roomsize = 5, revtime = 1.6, damping = 0.62, mulVerb = 1 |
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

	*defineProxyPattern {
		~instrumentPPatt = PatternProxy(Pseq([\simplePlayBuf], inf));
		~bufnumPPatt = PatternProxy(Pseq([~dum, ~te], inf));
		~ampPPatt = PatternProxy(Pseq([1, 1], inf));
		~durPPatt = PatternProxy(Pseq([1, 1], inf));


		~rythm = Pbind(
			\instrument,     \simplePlayBuf,
			\bufnum,         ~bufnumPPatt,
			\amp,            ~ampPPatt,
			\dur,            ~durPPatt,
		);
	}
}

//========================================================================
//========================================================================
//========================================================================

Rythms {
	*changeBPS { |bps|
		~durPPatt.source = ~durPPatt.source*bps;
	}
	*xasapiko {
		~bufnumPPatt.source = Pseq([~dum, ~te], inf);
		~ampPPatt.source = Pseq([1, 1], inf);
		~durPPatt.source = Pseq([1, 1], inf);
	}
	*xasapiko2 {
		~bufnumPPatt.source = Pseq([
			~dum,   0,
			~te,  ~te,
			~dum,   0,
			~te,  0
		], inf);
		~ampPPatt.source = Pseq([
			1,0,
			1,1,
			1,0,
			1,1
		], inf);
		~durPPatt.source = Pseq([
			1,1,
			1,1,
			1,1,
			1,1
		]/~bps, inf);
	}
}
/*
OOP with tradicional rythms
Aris Bezas 121115

Examples:
~bps = 2;
Rythms.karsilamas
Rythms.xasapiko
~rythm.play;
~rythm2.play;
Rythm_Class.changeBPS(0.8)

------ MORE ---------
~instrumentPPatt.source = Pseq([\playBufGVerb], inf);
~bufnumPPatt.source = Pseq([~, ~te], inf);
~ampPPatt.source = Pseq([1, 0.5], inf);
~durPPatt.source = Pseq([1, 1]/2, inf);
~bufnumPPatt.source = Pseq([~dum, ~te,~dum, ~te,~dum, ~te,~dum, ~te,~te], inf);
~bufnumPPatt.source = Pseq([~tambourine, ~tambourine,~tambourine, ~te,~dum, ~te,~dum, ~te,~te], inf);
~bufnumPPatt.source = Pseq([~dum, ~te,~te, ~te,~dum, ~te,~dum, ~te,~te], inf);
~bufnumPPatt.source = Pseq([~dum, ~te,~dum, ~te,~dum, ~te,~dum, ~te,~bell], inf);
~bufnumPPatt.source = Pseq([~dum, ~bell,~dum, ~bell,~dum, ~bell,~dum, ~bell,~bell], inf);

~bufnumPPatt.source = Pseq([~motoLoop, ~te,~motoLoop, ~te,~motoLoop, ~te,~motoLoop, ~motoLoop,~motoLoop], inf);

//Looper
x = Synth(\simplePlayBuf,[\bufnum, ~motoLoop, \loop, 1]);

//GVerb
//living room
a = Synth(\gverb_mic, [\roomsize, 16, \revtime, 1.24, \damping, 0.10, \inputbw, 0.95, \drylevel -3, \earlylevel, -15, \taillevel, -17]);
a.free;

//church
a = Synth(\gverb_mic, [\roomsize, 80, \revtime, 4.85, \damping, 0.41, \inputbw, 0.19, \drylevel -3, \earlylevel, -9, \taillevel, -11]);
a.free;

// cathedral
a = Synth(\gverb_mic, [\roomsize, 243, \revtime, 1, \damping, 0.1, \inputbw, 0.34, \drylevel -3, \earlylevel, -11, \taillevel, -9]);
4a.free

// canyon
a = Synth(\gverb_mic, [\roomsize, 300, \revtime, 103, \damping, 0.43, \inputbw, 0.51, \drylevel -5, \earlylevel, -26, \taillevel, -20]);

x.set()
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
		~tambourine = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/tambourine.aiff");
		~motoLoop = Buffer.read(Server.default, "/Users/ari/Media/sounds/loops/MotownDrummer08.aif");
	}
	*sendTheSynths	{
		SynthDef(\simplePlayBuf, { | out, freq = 440, amp = 1, bufnum = 10, pan = 0, gate = 1, loop = 0 |
			Out.ar(0, Pan2.ar(PlayBuf.ar(1,bufnum:bufnum,doneAction:
				2, loop: loop), 0, amp));
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
		}).send(Server.default);

		SynthDef(\gverb_mic, {
			|roomsize, revtime, damping, inputbw, spread = 15, drylevel, earlylevel, pos = 0, amp = 1,
			taillevel|
			Out.ar(0, Pan2.ar(GVerb.ar(
				SoundIn.ar(0),
                roomsize,
                revtime,
                damping,
                inputbw,
                spread,
                drylevel.dbamp,
                earlylevel.dbamp,
                taillevel.dbamp,
				roomsize, 0.3) + SoundIn.ar(0)),
				pos,
				amp)

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
	*changeBPS { |bps|
		~durPPatt.source = ~durPPatt.source*bps;
	}
}

//========================================================================
//========================================================================
//========================================================================

Rythms {
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
	*karsilamas {
		~bufnumPPatt.source = Pseq([~dum, ~te,~dum, ~te,~dum, ~te,~dum, ~te,~te], inf);
		~ampPPatt.source = Pseq([1, 1,1, 1,1, 1,1, 1, 1], inf);
		~durPPatt.source = Pseq([1, 1,1, 1,1, 1,1, 1, 1], inf);
	}

}
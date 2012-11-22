/*
Rythm
OOP with tradicional rythms
Aris Bezas 121115

//====================
Rythms
Rythm.play
Rythm.trionXronon(35) //Dontiapikna
Rythm.xasapiko(100) //Katerina, Toumpourlika
Rythm.mantilatos(3,100)
Rythm.tsifteteli(1,90)
Rythm.tsifteteli(5,90)
Rythm.sousta(1,90)

SCDoc

=================
Looper
x = Synth(\simplePlayBuf,[\bufnum, ~motoLoop, \loop, 1]);

==================
MicIn with GVerb

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

=====================
Pitch change

Rythm.play
Rythm.trionXronon(18) //Dontiapikna
~ratePPatt.source = Pseq([8], inf);
~instrumentPPatt.source = Pseq([\phasorPlayBuf], inf);
~instrumentPPatt.source = Pseq([\playBufGVerb], inf);
~instrumentPPatt.source = Pseq([\simplePlayBuf], inf);

//=========
Ratios
1: 4/4
2: 3/4
3: 7/8
4: 9/8

//==================
Change buffer
~dum = ~bell

Rythm.stop

============
SCDoc

Rythm
*/


Rythm {
	*initClass {
		StartUp add: {
			if (not(Server.default.serverRunning)) { Server.default.boot };
			Server.default.doWhenBooted {
				//ratio= [   0,    1,    2,     3,     4,       5,    6,     7]
				//ratio= [ 2/4,  4/4,  8/8, 16/16,  33/32,    3/4,  7/8,   9/8]
				~ratio = [1/60, 1/60, 1/30,  1/15,   2/15, 0.4/18, 1/15, 0.1/3];
				~bpm = 60;
				this.loadTheBuffers;
				this.sendTheSynths;
				this.defineProxyPattern;
				"\n|====================|".postln;
				"|Rythm Class is ready|".postln;
				"|====================|\n".postln;
			};

		}
	}

	*play {~rythm.play;}
	*stop {~rythm.stop;}//Doesn't work

	*loadTheBuffers	{

		~bass_drum = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/bass_Drum_Single_Kick.aiff");
		~gonga = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/gonga_single_hit.aiff");
		~bell = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/agogo_bell.aiff");
		~tambourine = Buffer.read(Server.default, "/Users/ari/Media/sounds/percusion/tambourine.aiff");
		~motoLoop = Buffer.read(Server.default, "/Users/ari/Media/sounds/loops/MotownDrummer08.aif");

		//default
		~dum = ~bass_drum;
		~te  = ~gonga;
	}
	*sendTheSynths	{
		//Simple
		SynthDef(\simplePlayBuf, { | out, freq = 440, amp = 1, bufnum = 10, pan = 0, gate = 1, loop = 0 |
			Out.ar(0, Pan2.ar(PlayBuf.ar(1,bufnum:bufnum,doneAction:
				2, loop: loop), 0, amp));
		}).store;

		//\phasorPlayBuf
		SynthDef(\phasorPlayBuf, { | out, freq = 440, amp = 2, bufnum = 10, pan = 0, gate = 1, loop = 0, rate = 1|
			var playBuf, phasor;
			//phasor = Phasor.ar(0, BufRateScale.kr(bufnum)*rate, 0, BufFrames.kr(bufnum));
			//playBuf = BufRd.ar(1, bufnum, phase:phasor,loop:0,interpolation:1);
			playBuf = PlayBuf.ar(1,bufnum,BufRateScale.kr(bufnum)*rate,loop,doneAction:2);
			Out.ar(0, Pan2.ar(playBuf, 0, amp));
		}).store;

		//doesn't work
		SynthDef(\playBufGVerb, { | out, freq = 440, amp = 1, bufnum = 10, pan = 0, gate = 1, roomsize = 5, revtime = 1.6, damping = 0.62, mulVerb = 1 |
			var playBuffer = PlayBuf.ar(1,bufnum:bufnum, doneAction:2);
			Out.ar(0,
				Pan2.ar(
					GVerb.ar(
						playBuffer,
						roomsize,
						revtime,
						damping,
						mul: mulVerb
					)
					+
					playBuffer,
					0, amp)
			);
		}).store;

		//doesn't work
		SynthDef(\playBufGVerbPhasor, { | out, freq = 440, amp = 0.1, bufnum = 10, pan = 0, gate = 1, roomsize = 5, revtime = 1.6, damping = 0.62, mulVerb = 1 |
			var playBuffer;
			playBuffer = PlayBuf.ar(1,bufnum:bufnum, doneAction:2);
			Out.ar(0,
				Pan2.ar(
					GVerb.ar(
						playBuffer,
						roomsize,
						revtime,
						damping,
						mul: mulVerb
					)
					+
					playBuffer,
					0, amp)
			);
		}).store;


		//mic input
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
		~ratePPatt = PatternProxy(Pseq([1], inf));
		~bufnumPPatt = PatternProxy(Pseq([~dum, ~te], inf));
		~ampPPatt = PatternProxy(Pseq([0, 0], inf));
		~durPPatt = PatternProxy(Pseq([1, 1], inf));

		~rythm = Pbind(
			\instrument,     ~instrumentPPatt,
			\rate,           ~ratePPatt,
			\bufnum,         ~bufnumPPatt,
			\amp,            ~ampPPatt,
			\dur,            ~durPPatt,
		);
	}

	//Rythms
	*xasapiko { |bpm=100|
		~bpm = ~ratio[0]*bpm;
		~durPPatt.source = Pseq([1, 1], inf)/~bpm;
		~bufnumPPatt.source = Pseq([~dum, ~te], inf);
		~ampPPatt.source = Pseq([1, 1], inf);
	}
	*trionXronon { |bpm = 100|
		~bpm = ~ratio[3]*bpm;
		~durPPatt.source = Pseq([1, 1, 1], inf)/~bpm;
		~bufnumPPatt.source = Pseq([~dum, ~te, ~te], inf);
		~ampPPatt.source = Pseq([1, 1, 1], inf);
	}
	*karsilamas {|version,bpm=100|
		~bufnumPPatt.source = Pseq([~dum, ~te,~dum, ~te,~dum, ~te,~dum, ~te,~te], inf);
		~ampPPatt.source = Pseq([1, 1,1, 1,1, 1,1, 1, 1], inf);
		~durPPatt.source = Pseq([1, 1,1, 1,1, 1,1, 1, 1], inf);
	}
	*mantilatos { |version, bpm=100|
		~bpm = ~ratio[6]*bpm;
		~durPPatt.source = Pseq([1, 1, 1, 1, 1, 1, 1], inf)/~bpm;
		case
		{version == 1} {
			~bufnumPPatt.source = Pseq([~dum, 0,~te, ~te, ~te, ~te, ~te], inf);
			~ampPPatt.source = Pseq([1, 0, 1, 1, 1, 1, 1], inf);
		}
		{version == 2} {
			~bufnumPPatt.source = Pseq([~dum, 0,~te, 0, ~dum, 0, ~te], inf);
			~ampPPatt.source = Pseq([1, 0, 1, 0, 1, 0, 1], inf);
		}
		{version == 3} {
			~bufnumPPatt.source = Pseq([~dum, 0,~te, 0, ~te, 0, 0], inf);
			~ampPPatt.source =    Pseq([   1, 0,  1, 0,   1, 0, 0], inf);
		};
	}
	*tsifteteli { |version, bpm=100|
		~bpm = ~ratio[3]*bpm;
		~durPPatt.source = Pseq([1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1], inf)/~bpm;
		case
		{version == 1} {
			~bpm = ~ratio[2]*bpm;
			~durPPatt.source = Pseq([1, 1, 1, 1, 1, 1, 1, 1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, ~te, 0, ~te, ~dum, 0, ~te, 0], inf);
			~ampPPatt.source =    Pseq([1,    1,   0, 1,   1,    0, 1,   0], inf);
		}
		{version == 2} {
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, 0, ~te, ~te, ~te, 0, ~dum, 0, ~te, ~te, ~te, 0, ~te, ~te], inf);
			~ampPPatt.source =    Pseq([1,    0, 1,   0, 1,   1,   1,   0,    1, 0,   1,   1,   1, 0,   1,   1], inf);
		}
		{version == 3} {
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, 0, ~te, ~te, ~te, 0, ~dum, ~te, ~te, ~te, ~te, 0, ~te, ~te], inf);
			~ampPPatt.source =    Pseq([1,    0,   1, 0,   1,   1,   1, 0,    1,   1,   1,   1,   1, 0,   1,   1], inf);
		}
		{version == 4} {
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~te, ~te, ~te, ~te, 0, ~dum,   0, ~te, ~te,   ~te, 0, ~te, ~te], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,   1,   1,   1,   1, 0,    1,   0,   1,   1,     1, 0,   1,   1], inf);
		}
		{version == 5} {
			~bufnumPPatt.source = Pseq([~dum, 0, ~dum, 0, ~te, ~te, ~te, 0, ~dum,   0, ~te, ~te,   ~te, 0, ~te, ~te], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,  0,   1,   1,   1, 0,    1,   0,   1,   1,     1, 0,   1,   1], inf);
		};
	}
	*tessaronXronon { |version, bpm=100|
		~bpm = ~ratio[2]*bpm;
		~durPPatt.source = Pseq([1, 1, 1, 1, 1, 1, 1, 1], inf)/~bpm;
		case
		{version == 1} {
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~te, ~dum, 0, ~te, 0], inf);
			~ampPPatt.source =    Pseq([   1, 0,   1,   1,    1, 0,   1, 0], inf);
		}
		{version == 2} {
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~te, ~dum, ~te, ~te, 0], inf);
			~ampPPatt.source =    Pseq([   1, 0,   1,   1,    1,   1,   1, 0], inf);
		};
	}
	*sousta { |version, bpm=100|
		~bpm = ~ratio[2]*bpm;
		~durPPatt.source = Pseq([1, 1, 1, 1, 1, 1, 1, 1], inf)/~bpm;
		case
		{version == 1} {
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~te, ~dum, 0, ~te, 0], inf);
			~ampPPatt.source =    Pseq([   1, 0,   1,   1,    1, 0,   1, 0], inf);
		}
		{version == 2} {
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~te, ~dum, ~te, ~te, 0], inf);
			~ampPPatt.source =    Pseq([   1, 0,   1,   1,    1,   1,   1, 0], inf);
		};
	}
	*syrto7 { |version, bpm=100|
		~bpm = ~ratio[6]*bpm;
		~durPPatt.source = Pseq([1, 1, 1, 1, 1, 1, 1], inf)/~bpm;
		case
		{version == 1} {
			~bufnumPPatt.source = Pseq([~dum, ~te, ~te, ~dum,  0, ~te, 0], inf);
			~ampPPatt.source =    Pseq([   1,   1,   1,    1,  0,   1, 0], inf);
		}
		{version == 2} {
			~bufnumPPatt.source = Pseq([~dum,  0, ~te, ~dum,  0, ~te, 0], inf);
			~ampPPatt.source =    Pseq([   1,  0,   1,    1,  0,   1, 0], inf);
		};
	}
	*syrto4 { |version, bpm=100|
		~bpm = ~ratio[2]*bpm;
		~durPPatt.source = Pseq([1, 1, 1, 1, 1, 1, 1, 1], inf)/~bpm;
		case
		{version == 1} {
			~bufnumPPatt.source = Pseq([~dum,  0,   0, ~te,  ~dum,  0, ~te, 0], inf);
			~ampPPatt.source =    Pseq([   1,  0,   0,   1,     1,  0,  1, 0], inf);
		}
		{version == 2} {
			"not yet".postln;
		};
	}
}

/*
Rythm
OOP with tradicional rythms
Aris Bezas 121115

//====================
Rythm.play(52,110,1);  //Initialize
Rythm.play(12.1,180);  //Dontia Pykna

Rythm.play(12.3,100,1);
Rythm.play(2,100);  //just change

*/


Rythm {
	*initClass {
		StartUp add: {
			if (not(Server.default.serverRunning)) { Server.default.boot };
			Server.default.doWhenBooted {
				//ratio= [   0,    1,    2,     3,     4,       5,     6,      7,     8,     9,     10,    11 ,      12]
				//ratio= [ 2/4,  4/4,  8/8, 16/16,  33/32,    3/4,   6/8,    7/8,   9/8,   10/8   12/8,   18/8(9/4) 18/16]
				~ratio = [1/60, 1/60, 1/30,  1/15,   2/15, 0.4/18,  1/30,   1/15, 0.1/3,   1/30,   1/30, 1/60,      0.1/6];
				~bpm = 60;
				this.loadTheBuffers;
				this.sendTheSynths;
				this.defineProxyPattern;
				/*
				"\n|======================|".postln;
				"|Rythm Class is running|".postln;
				"|======================|\n".postln;
				*/
			};
		}
	}

	*play {|version=1, bpm=100, init=0|
		if (init == 0,
			{
				"Rythm is Playing, so just change the change the rythm".postln;
				this.changeRythm(version, bpm);
			},
			{
				"Rythm is not Playing, so first let's start the Pattern and after define the rythm".postln;
				this.start;
				this.changeRythm(version, bpm);
			}
		)
	}
	*start{~rythm.play;}
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

		//\phasorPlayBuf
		SynthDef(\phasorPlayBuf, { | out, freq = 440, amp = 2, bufnum = 10, pan = 0, gate = 1,
			loop = 0, rate = 1|
			var playBuf, phasor;
			//phasor = Phasor.ar(0, BufRateScale.kr(bufnum)*rate, 0, BufFrames.kr(bufnum));
			//playBuf = BufRd.ar(1, bufnum, phase:phasor,loop:0,interpolation:1);
			playBuf = PlayBuf.ar(1,bufnum,BufRateScale.kr(bufnum)*rate,loop,doneAction:2);
			Out.ar(0, Pan2.ar(playBuf, 0, amp));
		}).store;

		//doesn't work
		SynthDef(\playBufGVerb, { | out, freq = 440, amp = 1, bufnum = 10, pan = 0, gate = 1,
			roomsize = 5, revtime = 1.6, damping = 0.62, mulVerb = 1 |
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
		SynthDef(\playBufGVerbPhasor, { | out, freq = 440, amp = 0.1, bufnum = 10, pan = 0, gate = 1,
			roomsize = 5, revtime = 1.6, damping = 0.62, mulVerb = 1 |
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
		~bpm = ~ratio[7]*bpm;
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
		~bpm = ~ratio[7]*bpm;
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
	*changeRythm { |rythmNum=1, bpm=100|
		case
		{rythmNum == 1} {
			~bpm = ~ratio[0]*bpm;
			~durPPatt.source = Pseq([1, 1, 1, 1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, ~te,  ~dum, ~te], inf);
			~ampPPatt.source =    Pseq([   1,   1,     1,   1], inf);
		}
		{rythmNum == 2} {
			~bpm = ~ratio[5]*bpm;
			~durPPatt.source = Pseq([1, 1, 1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, ~te,  ~te], inf);
			~ampPPatt.source =    Pseq([   1,   1,    1], inf);
		}
		{rythmNum == 3} {
			~bpm = ~ratio[2]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te,  ~te, ~dum,  0,  ~te,  0], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,    1,    1,  0,   1,   0], inf);
		}
		{rythmNum == 4} {
			~bpm = ~ratio[2]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te,  ~te, ~dum,  ~te,  ~te,  0], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,    1,    1,    1,   1,   0], inf);
		}
		{rythmNum == 5} {
			~bpm = ~ratio[3]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te,  ~te, ~dum,   0,  ~te,  0], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,    1,    1,   0,   1,   0], inf);
		}
		{rythmNum == 6} {
			~bpm = ~ratio[3]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te,  ~te, ~dum,   0,  ~te, ~te], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,    1,    1,   0,   1,    1], inf);
		}
		{rythmNum == 7} {
			~bpm = ~ratio[0]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te,  ~te, ~te,   0,  ~te, 0], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,    1,   1,   0,   1,  0], inf);
		}
		{rythmNum == 8.1} {
			~bpm = ~ratio[2]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, 0,  ~te, ~te,   0,  ~te, 0], inf);
			~ampPPatt.source =    Pseq([1,    0, 0,    1,   1,   0,   1,  0], inf);
		}
		{rythmNum == 8.2} {
			~bpm = ~ratio[2]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, 0,  ~te, ~te,  ~te,  ~te, ~te], inf);
			~ampPPatt.source =    Pseq([1,    0, 0,    1,   1,    1,   1,    1], inf);
		}
		{rythmNum == 9.1} {
			~bpm = ~ratio[5]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, 0,  ~te, ~te,   0,  ~te, 0,~te, 0,~te, 0], inf);
			~ampPPatt.source =    Pseq([1,    0, 0,    1,   1,   0,   1,  0,  1, 0,  1, 0], inf);
		}
		{rythmNum == 9.2} {
			~bpm = ~ratio[5]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, 0,~te, ~te,   0,  ~te, 0,~te, ~te,~te, ~te], inf);
			~ampPPatt.source =    Pseq([1,    0, 0,  1,   1,   0,   1,  0,  1,   1,  1,   1], inf);
		}
		{rythmNum == 10.1} {
			~bpm = ~ratio[6]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~dum, ~te,   0], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,   1,   1,    0], inf);
		}
		{rythmNum == 10.2} {
			~bpm = ~ratio[6]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~dum, ~te, ~te], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,   1,   1,    1], inf);
		}
		{rythmNum == 11.1} {
			~bpm = ~ratio[9]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~dum, ~te, 0, ~dum, ~dum, ~te, 0, ~te, ~te], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,   1,   1,  0,    1,    1,   1,  0,  1,   1], inf);
		}
		{rythmNum == 11.2} {
			~bpm = ~ratio[9]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~dum, ~te, 0, ~dum, ~dum, ~te, 0, 0,0], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,   1,   1,  0,    1,    1,   1, 0, 0,0], inf);
		}
		{rythmNum == 12.1} {
			~bpm = ~ratio[9]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~te, ~dum,  0, ~te,  0, 0,0], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,   1,    1,  0,   1,  0, 0,0], inf);
		}
		{rythmNum == 12.2} {
			~bpm = ~ratio[9]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~te, ~dum,  ~dum, ~te,  0, ~te,  ~te], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,   1,    1,     1,   1,  0,   1,    1], inf);
		}
		{rythmNum == 12.3} {
			~bpm = ~ratio[9]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~te, ~dum,  0, ~te,  0, ~te,  ~te], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,   1,    1,  0,   1,  0,   1,    1], inf);
		}
		{rythmNum == 13} {
			~bpm = ~ratio[9]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~te, ~dum,  0, ~te,  0, 0, 0,~dum, 0, ~te, ~dum, ~te, 0], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,   1,    1,  0,   1,  0, 0, 0,   1, 0,  1,    1,   1, 0], inf);
		}

		{rythmNum == 47} {
			~bpm = ~ratio[11]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, 0, ~te, 0,~dum,  0, ~te,  ~te, ~dum, 0, ~te, 0, ~dum, 0, ~te, ~te], inf);
			~ampPPatt.source =    Pseq([1,    0,   1, 0,   1, 0,   1,  0,   1,    1,    1, 0,   1, 0,    1, 0,   1,   1], inf);
		}

		{rythmNum == 52} {
			~bpm = ~ratio[12]*bpm;
			~durPPatt.source = Pseq([1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1], inf)/~bpm;
			~bufnumPPatt.source = Pseq([~dum, 0, ~te, ~te, ~te, 0,~te,  0, ~dum,  0, ~te, ~te, ~te, 0, ~te, 0, ~te, 0], inf);
			~ampPPatt.source =    Pseq([1,    0,   1,   1,   1, 0,  1,  0,   1,   0,   1,   1,   1, 0,   1, 0,   1, 0], inf);
		}

	}
}

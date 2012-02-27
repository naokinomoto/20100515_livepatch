/************************************************************************************
 ************************************************************************************
 **
 ** SynthDef
 **
 ************************************************************************************
 ************************************************************************************/
(
(
SynthDef("Chaotic01", {
	|outbus = 0, freq = 50, 
	lfo1 = 10.0, lfo2 = 10.0, lfo3 = 10.0, lfo4 = 10.0,
	mul1 = 1.0, mul2 = 1.0, mul3 = 0.5, mul4 = 0.5|
	var out;
	out = LatoocarfianN.ar(
			(SampleRate.ir / 4) * freq,
			LFNoise2.kr(lfo1, mul1, 1.5),
			LFNoise2.kr(lfo2, mul2, 1.5),
			LFNoise2.kr(lfo3, mul3, 1.5),
			LFNoise2.kr(lfo4, mul4, 1.5)
		);
	out = LeakDC.ar(out);
	Out.ar(outbus, out.dup());
}).load(s);
);

(
SynthDef("ClipNoise", {|outbus = 0, freq = 5, min = 20, max = 5000, rq = 0.5|
	var out;
	out = LFClipNoise.ar(TExpRand.kr(min, min + max, Dust.kr(freq)));

	//out = ClipNoise.ar(1);
	//out = RLPF.ar(out, TExpRand.kr(min, min + max, Dust.kr(freq)), rq);
	Out.ar(outbus, out.dup());
}).load(s);
);

(
SynthDef("Crackle01", {|outbus = 0, freq = 10, min = 0.01, max = 0.9|
	var out;
	out = Crackle.ar(TExpRand.kr(min, min + max, Dust.kr(freq))) * 2;
	Out.ar(outbus, out.dup());
}).load(s);
);

(
SynthDef("Drone01", {
	|outbus = 0, amp = 1,
	amp1 = 1.0, amp2 = 1.0, amp3 = 1.0, amp4 = 1.0, amp5 = 1.0, amp6 = 1.0,
	modfreq1 = 1.0, modfreq2 = 1.0, modfreq3 = 1.0, modfreq4 = 1.0, modfreq5 = 1.0, modfreq6 = 1.0, 
	modamp1 = 0, modamp2 = 0, modamp3 = 0, modamp4 = 0, modamp5 = 0, modamp6 = 0|
	var freqlist = [
		[5.0, 500.0], 
		[500.0, 1000.0], 
		[1000.0, 3000.0], 
		[3000.0, 6000.0], 
		[6000.0, 10000.0], 
		[10000.0, 20000.0]];
	var amplist = [amp1, amp2, amp3, amp4, amp5, amp6];
	var modfreqlist = [modfreq1, modfreq2, modfreq3, modfreq4, modfreq5, modfreq6];
	var modamplist = [modamp1, modamp2, modamp3, modamp4, modamp5, modamp6];
	var out;
	
	out = Mix.ar(Array.fill(6, {|i|
		var lo = freqlist[i][0];
		var hi = freqlist[i][1];
		var amp = amplist[i];
		var modfreq = modfreqlist[i];
		var modamp = modamplist[i];
		var mod = SinOsc.kr(modfreq, 0, modamp, 1);
		Klang.ar(`[Array.rand(12, lo, hi), nil, nil]) * amp * 0.1 * mod;
	})) * amp;
	
	Out.ar(outbus, (out * 0.2).dup());
}).load(s);
);

(
SynthDef("FmFFT", {|outbus=0, trig_freq=10, sus=10, freq1_1=100, freq1_2=100, freq1_3=20, freq2_1=500, freq2_2=500, freq2_3=20, amp = 1|
	var freq=440, sustain=0.5, gate=1,
	trig, osc1, osc2, 
	freqenv1, ratioenv1, indexenv1, 
	freqenv1att, ratioenv1att, indexenv1att,
	freqenv1rel, ratioenv1rel, indexenv1rel,
	freqenv2, ratioenv2, indexenv2, 
	freqenv2att, ratioenv2att, indexenv2att, 
	freqenv2rel, ratioenv2rel, indexenv2rel, 
	chain1, chain2, chain, out;
	trig = Dust.kr(trig_freq);
	sustain = TRand.kr(0.1, sus, trig);
	
	freqenv1att = TRand.kr(0.001, sustain, trig);
	ratioenv1att = TRand.kr(0.001, sustain, trig);
	indexenv1att = TRand.kr(0.001, sustain, trig);
	freqenv1rel = sustain - freqenv1att;
	ratioenv1rel = sustain - ratioenv1att;
	indexenv1rel = sustain - indexenv1att;

	freqenv2att = TRand.kr(0.001, sustain, trig);
	ratioenv2att = TRand.kr(0.001, sustain, trig);
	indexenv2att = TRand.kr(0.001, sustain, trig);
	freqenv2rel = sustain - freqenv2att;
	ratioenv2rel = sustain - ratioenv2att;
	indexenv2rel = sustain - indexenv2att;
	
	freqenv1 = EnvGen.kr(Env.perc(freqenv1att, freqenv1rel), trig);
	ratioenv1 = EnvGen.kr(Env.perc(ratioenv1att, ratioenv1rel), trig);	indexenv1 = EnvGen.kr(Env.perc(indexenv1att, indexenv1rel), trig);
	freqenv2 = EnvGen.kr(Env.perc(freqenv2att, freqenv2rel), trig);
	ratioenv2 = EnvGen.kr(Env.perc(ratioenv2att, ratioenv2rel), trig);	indexenv2 = EnvGen.kr(Env.perc(indexenv2att, indexenv2rel), trig);
	osc1 = PMOsc.ar(
		TRand.kr(10, freq1_1, trig) * freqenv1,
		TRand.kr(10, freq1_2, trig) * ratioenv1,
		TRand.kr(0.1, freq1_3, trig) * indexenv1
	);
	osc2 = PMOsc.ar(
		TRand.kr(10, freq2_1, trig) * freqenv2,
		TRand.kr(10, freq2_2, trig) * ratioenv2,
		TRand.kr(0.1, freq2_3, trig) * indexenv2
	);
	chain1 = FFT(LocalBuf(2048), osc1);
	chain2 = FFT(LocalBuf(2048), osc2);
	chain = PV_MagMul(chain1, chain2);
	out = IFFT(chain);
	out = Pan2.ar(out, TRand.kr(-1, 1, trig));
	Out.ar(outbus, out * amp);
}).load(s);
);

(
SynthDef("AuxInput", {|inch = 0, outbus = 0|
	var in;
	in = AudioIn.ar(inch) * 2;
	Out.ar(outbus, in);
}).load(s);
);

(
SynthDef("Bus1to2", {|inbus = 0, outbus1 = 0, outbus2 = 64, amp1 = 1, amp2 = 1, ampall = 1|
	var in;
	in = In.ar(inbus, 2);
	in = Limiter.ar(in, 1);
	Out.ar(outbus1, in * amp1 * ampall);
	Out.ar(outbus2, in * amp2 * ampall);
}
).load(s);
);

(
SynthDef("PinkNoise", {|inch = 0, outbus = 0|
	Out.ar(outbus, SinOsc.ar(1000));
}).load(s);
);

(
SynthDef("InGrain", {
	|inbus = 64, outbus = 0, 
	trig = 10.0, minlen = 0.01, maxlen = 1.0, pitch = 4.0,
	amp1 = 0, amp2 = 0, amp3 = 0, amp4 = 0, amp5 = 0|
	var in, out, buf, rec, grain, amplist;
	amplist = [amp1, amp2, amp3, amp4, amp5];
	in = In.ar(inbus, 1);
	in = Limiter.ar(in, 1);
	
	buf = LocalBuf(44100 * 6, 1).clear;
	rec = RecordBuf.ar(in, buf, loop:1);
	
	grain = Mix.ar(Array.fill(5, {|i|
		GrainBuf.ar(
			2, 
			Dust.ar(trig), 
			LFNoise2.kr.range(minlen, minlen + maxlen), 
			buf, 
			LFNoise2.kr.range(pitch * -1.0, pitch), 
			LFNoise2.kr(0.1).range(0, 1), 
			4, 
			LFNoise2.kr.range(-1, 1)) * amplist[i]}));
	Out.ar(outbus, grain);
}).load(s);
);

(
SynthDef("InFFT", {|inbus = 64, outbus = 0, wallfreq = 1, wallamp = 1, combval = 0.5, combfreq = 10|
	var in, chain;
	in = In.ar(inbus, 2);
	in = Limiter.ar(in, 1);

	chain = FFT(LocalBuf(2048), in);
	chain = PV_RandComb(chain, combval, Dust.kr(combfreq));
	chain = PV_BrickWall(chain, LFNoise2.kr(wallfreq, wallamp));
	Out.ar(outbus, IFFT(chain).dup);
}).load(s);
);

(
SynthDef("Reverb", {|inbus = 64, outbus = 0, mix = 0.33, room = 0.25, dump = 0.7|
	var in, out;
	in = In.ar(inbus, 2);
	out = FreeVerb2.ar(in[0], in[1], mix, room, dump);
	Out.ar(outbus, out);
	ScopeOut.ar(out, 0);
}).load(s);
);
)

/************************************************************************************
 ************************************************************************************
 **
 ** Live Main
 **
 ************************************************************************************
 ************************************************************************************/

//-- start!! --

(
var in, chaotic, clip, crackle, drone, fmfft;
var grain, fft, reverb;
var inputbus = Array.new(5);
var preeffectbus;
var grainbus, fftbus;
var xysliders = Array.newClear(3), win;
var knoblist = Array.newClear(9);
var faderlist = Array.newClear(9);

/************************************************************************************
 * Synth Init / Routing
 ************************************************************************************/

//generater init
in = Synth.tail(s, "AuxInput", [\inch, 1, \outbus, 64]);
chaotic = Synth.tail(s, "Chaotic01", [\outbus, 66]);
//clip = Synth.tail(s, "ClipNoise", [\outbus, 68]);
fmfft = Synth.tail(s, "FmFFT", [\outbus, 68]); 
crackle = Synth.tail(s, "Crackle01", [\outbus, 70]);
drone = Synth.tail(s, "Drone01", [\outbus, 72]);

//generater routing
5.do({|i|
	inputbus.add(
		Synth.tail(
			s, 
			"Bus1to2", 
			[\inbus, 64 + (i * 2), \outbus1, 84, \outbus2, 74, \amp1, 1, \amp2, 0, \ampall, 0]));
});

preeffectbus = Synth.tail(s, "Bus1to2", [\inbus, 74, \outbus1,76 , \outbus2, 78, \amp1, 1, \amp2, 0, \ampall, 1]);

//effect init
grain = Synth.tail(s, "InGrain", [\inbus, 76, \outbus, 80]);
grainbus = Synth.tail(s, "Bus1to2", [\inbus, 80, \outbus1,84 , \outbus2, 78, \amp1, 1, \amp2, 0, \ampall, 1]);

fft = Synth.tail(s, "InFFT", [\inbus, 78, \outbus, 82]);
fftbus = Synth.tail(s, "Bus1to2", [\inbus, 82, \outbus1,84 , \outbus2, 76, \amp1, 1, \amp2, 0, \ampall, 1]);

//reverb init
reverb = Synth.tail(s, "Reverb", [\inbus, 84, \outbus, 0, \mix, 0]);

/************************************************************************************
 * MIDI
 ************************************************************************************/
MIDIClient.init;
MIDIIn.connect(0, 0);
MIDIIn.control = {|src, chan, num, val|
	(src + ":" + chan + ":" + num + ":" + val).postln;
	case
		{(num >= 1) && (num <= 5)} {
			inputbus[num - 1].set(\ampall, val / 127);
		} {num == 6} {
			grainbus.set(\ampall, val / 127);
		} {num == 7} {
			fftbus.set(\ampall, val / 127);
		} {num == 8} {
			reverb.set(\dump, val / 127);
		} {num == 9} {
			reverb.set(\room, val / 127);
		} {(num >= 11) && (num <= 15)} {
			inputbus[num - 11].set(\amp1, min(((127 - val) / 64), 1));
			inputbus[num - 11].set(\amp2, min(val / 64, 1));
		} {num == 16} {
			grainbus.set(\amp1, min(((127 - val) / 64), 1));
			grainbus.set(\amp2, min(val / 64, 1));
		} {num == 17} {
			fftbus.set(\amp1, min(((127 - val) / 64), 1));
			fftbus.set(\amp2, min(val / 64, 1));
		} {num == 18} {
			preeffectbus.set(\amp1, min(((127 - val) / 64), 1));
			preeffectbus.set(\amp2, min(val / 64, 1));
		} {num == 19} {
			reverb.set(\mix, val / 127);
		};
};

/************************************************************************************
 * OSC
 ************************************************************************************/
//========================
// OSC - chaos
//========================
OSCresponder(nil, '/noise1/xy1', {|t, r, msg|
	postln(msg);
	chaotic.setn(\lfo1, (msg[1] + 0.1) * 10);
	chaotic.setn(\mul1, msg[2] * 1.5);
}).add;

OSCresponder(nil, '/noise1/xy2', {|t, r, msg|
	postln(msg);
	chaotic.setn(\lfo2, (msg[1] + 0.1) * 10);
	chaotic.setn(\mul2, msg[2] * 1.5);
}).add;

OSCresponder(nil, '/noise1/xy3', {|t, r, msg|
	postln(msg);
	chaotic.setn(\lfo3, (msg[1] + 0.1) * 10);
	chaotic.setn(\mul3, msg[2] * 0.5);
}).add;

OSCresponder(nil, '/noise1/xy4', {|t, r, msg|
	postln(msg);
	chaotic.setn(\lfo4, (msg[1] + 0.1) * 10);
	chaotic.setn(\mul4, msg[2] * 0.5);
}).add;

OSCresponder(nil, '/noise1/fader', {|t, r, msg|
	postln(msg);
	chaotic.setn(\freq, msg[1]);
}).add;

//========================
// OSC - clipnoise/crackle
//========================
OSCresponder(nil, '/noise2/xy1', {|t, r, msg|
	postln(msg);
	fmfft.setn(\freq1_1, msg[1] * 5000);
	fmfft.setn(\freq2_1, msg[1] * 5000);
}).add;

OSCresponder(nil, '/noise2/xy2', {|t, r, msg|
	postln(msg);
	fmfft.setn(\freq1_2, msg[1] * 5000);
	fmfft.setn(\freq2_2, msg[1] * 5000);
}).add;

OSCresponder(nil, '/noise2/xy3', {|t, r, msg|
	postln(msg);
	fmfft.setn(\freq1_3, msg[1] * 100);
	fmfft.setn(\freq2_3, msg[1] * 100);
}).add;

OSCresponder(nil, '/noise2/xy4', {|t, r, msg|
	postln(msg);
	fmfft.setn(\trig_freq, msg[1] * 10);
	fmfft.setn(\sus, msg[1] * 20);
}).add;

OSCresponder(nil, '/noise2/fader1', {|t, r, msg|
	postln(msg);
	//clip.setn(\freq, msg[1] * 10);
}).add;

OSCresponder(nil, '/noise2/xy5', {|t, r, msg|
	postln(msg);
	crackle.setn(\min, msg[1]);
	crackle.setn(\max, msg[1] + msg[2]);
}).add;

OSCresponder(nil, '/noise2/fader2', {|t, r, msg|
	postln(msg);
	crackle.setn(\freq, msg[1] * 10);
}).add;

//========================
// OSC - drone
//========================
6.do({|i|
	var num = (i + 1).wrap(1, 8).asString;
	var amplist = [\amp1, \amp2, \amp3, \amp4, \amp5, \amp6];
	var modfreqlist = [\modfreq1, \modfreq2, \modfreq3, \modfreq4, \modfreq5, \modfreq6];
	var modamplist = [\modamp1, \modamp2, \modamp3, \modamp4, \modamp5, \modamp6];
	OSCresponder(nil, '/drone/volume/' ++ num, {|t, r, msg|
		postln(msg);
		drone.setn(amplist[i], msg[1]);
	}).add;

	OSCresponder(nil, '/drone/xy' ++ num, {|t, r, msg|
		postln(msg);
		drone.setn(modfreqlist[i], msg[1] * 5);
		drone.setn(modamplist[i], msg[2]);
	}).add;
});

//========================
// OSC - effect
//========================
// grain
OSCresponder(nil, '/effect/grainxy1', {|t, r, msg|
	postln(msg);
	grain.setn(\pitch, (msg[1] ** 5) * 10);
	grain.setn(\trig, msg[2] * 10);
	}).add;

OSCresponder(nil, '/effect/grainxy2', {|t, r, msg|
	postln(msg);
	grain.setn(\minlen, msg[1]);
	grain.setn(\maxlen, msg[2]);
}).add;

5.do({|i|
	var num = (i + 1).wrap(1, 8).asString;
	var amplist = [\amp1, \amp2, \amp3, \amp4, \amp5];
	OSCresponder(nil, '/effect/grainvolume/' ++ num, {|t, r, msg|
		postln(msg);
		grain.setn(amplist[i], msg[1]);
	}).add;
});

// fft
OSCresponder(nil, '/effect/fftxy1', {|t, r, msg|
	postln(msg);
	fft.setn(\wallfreq, msg[1] * 20);
	fft.setn(\wallamp, msg[2]);
}).add;

OSCresponder(nil, '/effect/fftxy2', {|t, r, msg|
	postln(msg);
	fft.setn(\combfreq, msg[1] * 20);
	fft.setn(\combval, msg[2]);
}).add;


/************************************************************************************
 * GUI
 ************************************************************************************/

GUI.cocoa;

win = GUI.window.new("nano kontrol", Rect(10, 400, 300, 150));
win.view.background = Color(0, 0, 0);
win.front;

9.do({|i|
	var knob, fader;
	knob = GUI.knob.new(win, Rect(10 + (i * 25) + (i * 5), 10, 25, 25))
			.background_(Color(0.5, 0.5, 0.5));
	fader = GUI.slider.new(win, Rect(10 + (i * 25) + (i * 5), 40, 25, 100))
			.background_(Color(0.5, 0.5, 0.5))
			.knobColor_(Color(1, 1, 0.51));
			
	case
		{(i >= 0) && (i <= 5)} {
			knob.action_({|view|
				postln(view.value);
				inputbus[i].set(\amp1, 1 - view.value);
				inputbus[i].set(\amp2, view.value);
			});
			fader.action_({|view|
				postln(view.value);
				inputbus[i].set(\ampall, view.value);
			});

		} {i == 6} {
			knob.action_({|view|
				postln(view.value);
				grainbus.set(\amp1, 1 - view.value);
				grainbus.set(\amp2, view.value);
			});
			fader.action_({|view|
				postln(view.value);
				grainbus.set(\ampall, view.value);
			});

		} {i == 7} {
			knob.action_({|view|
				postln(view.value);
				fftbus.set(\amp1, 1 - view.value);
				fftbus.set(\amp2, view.value);
			});
			fader.action_({|view|
				postln(view.value);
				fftbus.set(\ampall, view.value);
			});
		} {i == 8} {
			knob.action_({|view|
				postln(view.value);
				reverb.set(\mix, view.value);
			});
			fader.action_({|view|
				postln(view.value);
				reverb.set(\room, view.value);
			});
		};
	knoblist.put(i, knob);
	faderlist.put(i, fader);
});
)

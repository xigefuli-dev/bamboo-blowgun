# Sound source and edits
User-provided MP3: YTDown.com_YouTube_CLASH-ROYALE-DART-GOBLIN-SOUND-EFFECTS-N_Media_5kKozS7yhj0_006_128k.mp3
First segment omitted. Firing always plays segment 2 followed immediately by segment 3 as one sound attached to the shooter. No random selection.
fire.ogg: concatenate source [1.65,1.90] and [2.15,2.54] seconds; remove intervening 0.25 seconds of silence. Result 0.64 seconds.
hit.ogg: source [2.69,3.78] seconds; attached to the living target successfully hit by a VENOM dart only. Ordinary darts retain vanilla impact and hurt sounds.
Mono Vorbis, 44100 Hz. Shared gain 3.9134837206220423 preserves relative loudness; 2 ms edge fades avoid clicks. Original MP3 unchanged.

## Quick Draw playback
Vanilla Quick Charge speeds up the complete firing cue in-game at playback rates 1.00/1.03/1.08/1.17, so its full 0.64-second clip plays for approximately 0.640/0.621/0.593/0.547 seconds before the next shot at 14/13/12/11 ticks. Three companion sounds are pitch-compensated while retaining the full 0.64-second source duration; after playback-rate adjustment, their final pitch is only about 2%/4%/6% above the original. No firing audio is cut off or shortened in the source files.

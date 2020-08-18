

"""Perform preprocessing and raw feature extraction."""

import argparse
import logging
import os

import librosa
import numpy as np
import soundfile as sf
import yaml

from tqdm import tqdm

from parallel_wavegan.datasets import AudioDataset
from parallel_wavegan.datasets import AudioSCPDataset
from parallel_wavegan.utils import write_hdf5
from scipy.fftpack import dct

def logmelfilterbank(audio,
                     sampling_rate,
                     eps=1e-10,
                     ):
    
    sample_rate = sampling_rate
    signal = audio
    signal = signal[0:int(3.5 * sample_rate)]  # Keep the first 3.5 seconds

    pre_emphasis = 0.97
    emphasized_signal = numpy.append(signal[0], signal[1:] - pre_emphasis * signal[:-1])

    frame_size = 0.025
    frame_stride = 0.01
    frame_length, frame_step = frame_size * sample_rate, frame_stride * sample_rate  # Convert from seconds to samples
    signal_length = len(emphasized_signal)
    frame_length = int(round(frame_length))
    frame_step = int(round(frame_step))
    num_frames = int(numpy.ceil(float(numpy.abs(signal_length - frame_length)) / frame_step))  # Make sure that we have at least 1 frame

    pad_signal_length = num_frames * frame_step + frame_length
    z = numpy.zeros((pad_signal_length - signal_length))
    pad_signal = numpy.append(emphasized_signal, z) # Pad Signal to make sure that all frames have equal number of samples without truncating any samples from the original signal

    indices = numpy.tile(numpy.arange(0, frame_length), (num_frames, 1)) + numpy.tile(numpy.arange(0, num_frames * frame_step, frame_step), (frame_length, 1)).T
    frames = pad_signal[indices.astype(numpy.int32, copy=False)]

    frames *= numpy.hamming(frame_length)
    # frames *= 0.54 - 0.46 * numpy.cos((2 * numpy.pi * n) / (frame_length - 1))  # Explicit Implementation **

    NFFT = 512
    mag_frames = numpy.absolute(numpy.fft.rfft(frames, NFFT))  # Magnitude of the FFT
    pow_frames = ((1.0 / NFFT) * ((mag_frames) ** 2))  # Power Spectrum

    nfilt = 40
    low_freq_mel = 0
    high_freq_mel = (2595 * numpy.log10(1 + (sample_rate / 2) / 700))  # Convert Hz to Mel
    mel_points = numpy.linspace(low_freq_mel, high_freq_mel, nfilt + 2)  # Equally spaced in Mel scale
    hz_points = (700 * (10**(mel_points / 2595) - 1))  # Convert Mel to Hz
    bin = numpy.floor((NFFT + 1) * hz_points / sample_rate)

    fbank = numpy.zeros((nfilt, int(numpy.floor(NFFT / 2 + 1))))
    for m in range(1, nfilt + 1):
        f_m_minus = int(bin[m - 1])   # left
        f_m = int(bin[m])             # center
        f_m_plus = int(bin[m + 1])    # right

        for k in range(f_m_minus, f_m):
            fbank[m - 1, k] = (k - bin[m - 1]) / (bin[m] - bin[m - 1])
        for k in range(f_m, f_m_plus):
            fbank[m - 1, k] = (bin[m + 1] - k) / (bin[m + 1] - bin[m])
    filter_banks = numpy.dot(pow_frames, fbank.T)
    filter_banks = numpy.where(filter_banks == 0, numpy.finfo(float).eps, filter_banks)  # Numerical Stability
    filter_banks = 20 * numpy.log10(filter_banks)  # dB

    num_ceps = 12
    mfcc = dct(filter_banks, type=2, axis=1, norm='ortho')[:, 1 : (num_ceps + 1)] # Keep 2-13

    mfcc -= (numpy.mean(mfcc, axis=0) + 1e-8)

    return np.log10(np.maximum(eps, mfcc))

def main():
    """Run preprocessing process."""
    parser = argparse.ArgumentParser(
        description="Preprocess audio and then extract features (See detail in parallel_wavegan/bin/preprocess.py).")
    parser.add_argument("--wav-scp", "--scp", default=None, type=str,
                        help="kaldi-style wav.scp file. you need to specify either scp or rootdir.")
    parser.add_argument("--segments", default=None, type=str,
                        help="kaldi-style segments file. if use, you must to specify both scp and segments.")
    parser.add_argument("--rootdir", default=None, type=str,
                        help="directory including wav files. you need to specify either scp or rootdir.")
    parser.add_argument("--dumpdir", type=str, required=True,
                        help="directory to dump feature files.")
    parser.add_argument("--config", type=str, required=True,
                        help="yaml format configuration file.")
    parser.add_argument("--verbose", type=int, default=1,
                        help="logging level. higher is more logging. (default=1)")
    args = parser.parse_args()

    # set logger
    if args.verbose > 1:
        logging.basicConfig(
            level=logging.DEBUG, format="%(asctime)s (%(module)s:%(lineno)d) %(levelname)s: %(message)s")
    elif args.verbose > 0:
        logging.basicConfig(
            level=logging.INFO, format="%(asctime)s (%(module)s:%(lineno)d) %(levelname)s: %(message)s")
    else:
        logging.basicConfig(
            level=logging.WARN, format="%(asctime)s (%(module)s:%(lineno)d) %(levelname)s: %(message)s")
        logging.warning('Skip DEBUG/INFO messages')

    # load config
    with open(args.config) as f:
        config = yaml.load(f, Loader=yaml.Loader)
    config.update(vars(args))

    # check arguments
    if (args.wav_scp is not None and args.rootdir is not None) or \
            (args.wav_scp is None and args.rootdir is None):
        raise ValueError("Please specify either --rootdir or --wav-scp.")

    # get dataset
    if args.rootdir is not None:
        dataset = AudioDataset(
            args.rootdir, "*.wav",
            audio_load_fn=sf.read,
            return_utt_id=True,
        )
    else:
        dataset = AudioSCPDataset(
            args.wav_scp,
            segments=args.segments,
            return_utt_id=True,
            return_sampling_rate=True,
        )

    # check directly existence
    if not os.path.exists(args.dumpdir):
        os.makedirs(args.dumpdir, exist_ok=True)

    # process each data
    for utt_id, (audio, fs) in tqdm(dataset):
        # check
        assert len(audio.shape) == 1, \
            f"{utt_id} seems to be multi-channel signal."
        assert np.abs(audio).max() <= 1.0, \
            f"{utt_id} seems to be different from 16 bit PCM."
        assert fs == config["sampling_rate"], \
            f"{utt_id} seems to have a different sampling rate."

        # trim silence
        if config["trim_silence"]:
            audio, _ = librosa.effects.trim(audio,
                                            top_db=config["trim_threshold_in_db"],
                                            frame_length=config["trim_frame_size"],
                                            hop_length=config["trim_hop_size"])

        if "sampling_rate_for_feats" not in config:
            x = audio
            sampling_rate = config["sampling_rate"]
            hop_size = config["hop_size"]
        else:
            # NOTE(kan-bayashi): this procedure enables to train the model with different
            #   sampling rate for feature and audio, e.g., training with mel extracted
            #   using 16 kHz audio and 24 kHz audio as a target waveform
            x = librosa.resample(audio, fs, config["sampling_rate_for_feats"])
            sampling_rate = config["sampling_rate_for_feats"]
            assert config["hop_size"] * config["sampling_rate_for_feats"] % fs == 0, \
                "hop_size must be int value. please check sampling_rate_for_feats is correct."
            hop_size = config["hop_size"] * config["sampling_rate_for_feats"] // fs

        # extract feature
        mel = logmelfilterbank(x,
                               sampling_rate=sampling_rate,
                               hop_size=hop_size,
                               fft_size=config["fft_size"],
                               win_length=config["win_length"],
                               window=config["window"],
                               num_mels=config["num_mels"],
                               fmin=config["fmin"],
                               fmax=config["fmax"])

        # make sure the audio length and feature length are matched
        audio = np.pad(audio, (0, config["fft_size"]), mode="edge")
        audio = audio[:len(mel) * config["hop_size"]]
        assert len(mel) * config["hop_size"] == len(audio)

        # apply global gain
        if config["global_gain_scale"] > 0.0:
            audio *= config["global_gain_scale"]
        if np.abs(audio).max() >= 1.0:
            logging.warn(f"{utt_id} causes clipping. "
                         f"it is better to re-consider global gain scale.")
            continue

        # save
        if config["format"] == "hdf5":
            write_hdf5(os.path.join(args.dumpdir, f"{utt_id}.h5"), "wave", audio.astype(np.float32))
            write_hdf5(os.path.join(args.dumpdir, f"{utt_id}.h5"), "feats", mel.astype(np.float32))
        elif config["format"] == "npy":
            np.save(os.path.join(args.dumpdir, f"{utt_id}-wave.npy"),
                    audio.astype(np.float32), allow_pickle=False)
            np.save(os.path.join(args.dumpdir, f"{utt_id}-feats.npy"),
                    mel.astype(np.float32), allow_pickle=False)
        else:
            raise ValueError("support only hdf5 or npy format.")


if __name__ == "__main__":
    main()

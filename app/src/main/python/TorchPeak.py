# !usr/bin/python
# coding=utf-8

import json
import matplotlib.mlab as mlab
from operator import itemgetter
import numpy as np
from scipy.io import wavfile
from scipy.ndimage.filters import maximum_filter
from scipy.ndimage.morphology import generate_binary_structure, iterate_structure, binary_erosion

IDX_FREQ_I = 0
IDX_TIME_J = 1

DEFAULT_FS = 44100

DEFAULT_WINDOW_SIZE = 4096

DEFAULT_OVERLAP_RATIO = 0.5

DEFAULT_FAN_VALUE = 15

DEFAULT_AMP_MIN = 10

PEAK_NEIGHBORHOOD_SIZE = 20

MIN_HASH_TIME_DELTA = 0
MAX_HASH_TIME_DELTA = 200

PEAK_SORT = True

FINGERPRINT_REDUCTION = 20


NT_REDUCTION = 20

SAMPLE_SECOND = 60


def fingerprint(channel_samples, Fs=DEFAULT_FS,
				wsize=DEFAULT_WINDOW_SIZE,
				wratio=DEFAULT_OVERLAP_RATIO,
				fan_value=DEFAULT_FAN_VALUE,
				amp_min=DEFAULT_AMP_MIN):
	"""
	FFT the channel, log transform output, find local maxima, then return
	locally sensitive hashes.
	"""
	# FFT the signal and extract frequency components
	arr2D = mlab.specgram(
		channel_samples,
		NFFT=wsize,
		Fs=Fs,
		window=mlab.window_hanning,
		noverlap=int(wsize * wratio))[0]
	# print(arr2D[0].shape)

	# apply log transform since specgram() returns linear array
	arr2D = 10 * np.log10(arr2D)
	arr2D[arr2D == -np.inf] = 0  # replace infs with zeros

	# find local maxima
	local_maxima = get_2D_peaks(arr2D, amp_min=amp_min)

	# return hashes
	return generate_hashes(local_maxima, fan_value=fan_value)


def get_2D_peaks(arr2D, amp_min=DEFAULT_AMP_MIN):
	# http://docs.scipy.org/doc/scipy/reference/generated/scipy.ndimage.morphology.iterate_structure.html#scipy.ndimage.morphology.iterate_structure
	struct = generate_binary_structure(2, 1)
	neighborhood = iterate_structure(struct, PEAK_NEIGHBORHOOD_SIZE)

	# find local maxima using our fliter shape
	local_max = maximum_filter(arr2D, footprint=neighborhood) == arr2D
	background = (arr2D == 0)
	eroded_background = binary_erosion(background, structure=neighborhood,
									   border_value=1)

	# Boolean mask of arr2D with True at peaks
	detected_peaks = local_max ^ eroded_background

	# extract peaks
	amps = arr2D[detected_peaks]
	j, i = np.where(detected_peaks)

	# filter peaks
	amps = amps.flatten()
	peaks = zip(i, j, amps)
	peaks_filtered = [x for x in peaks if x[2] > amp_min]  # freq, time, amp

	# get indices for frequency and time
	frequency_idx = [x[1] for x in peaks_filtered]
	time_idx = [x[0] for x in peaks_filtered]
	return [i for i in zip(frequency_idx, time_idx)]

def generate_hashes(peaks, fan_value=DEFAULT_FAN_VALUE):
	"""
	Hash list structure:
	   sha1_hash[0:20] time_offset
	[(e05b341a9b77a51fd26, 32), ... ]
	"""
	if PEAK_SORT:
		peaks.sort(key=itemgetter(1))

	result = []
	for i in range(len(peaks)):
		for j in range(1, fan_value):
			if (i + j) < len(peaks):
				
				freq1 = peaks[i][IDX_FREQ_I]
				freq2 = peaks[i + j][IDX_FREQ_I]
				t1 = peaks[i][IDX_TIME_J]
				t2 = peaks[i + j][IDX_TIME_J]
				t_delta = t2 - t1

				if t_delta >= MIN_HASH_TIME_DELTA and t_delta <= MAX_HASH_TIME_DELTA:
					# hstr = "%s|%s|%s"%(freq1, freq2, t_delta)
					result.append([freq1, freq2, t_delta])
					# hstr = hstr.encode('utf8')
					# print(hstr)
					# h = hashlib.sha1(hstr)
					# yield (h.hexdigest()[0:FINGERPRINT_REDUCTION], t1)
	return np.array(result)

def readFileWav(filename):
	fs, audiofile = wavfile.read(filename)
	audiofile = audiofile.T
	# channels = audiofile.shape[0]
	start = int((audiofile.shape[1] - (SAMPLE_SECOND * fs)) / 2)
	data = audiofile[:,start:(start + (SAMPLE_SECOND * fs))]
	return data, fs

def readFile(filename):
	from pydub import AudioSegment
	audiofile = AudioSegment.from_file(filename)
	data = np.frombuffer(audiofile._data, np.int16)
	start = int((audiofile.duration_seconds - SAMPLE_SECOND) / 2)
	audiofile = audiofile[start * 1000:(start + SAMPLE_SECOND) * 1000]
	data = np.frombuffer(audiofile._data, np.int16)
	channels = []
	for chn in range(audiofile.channels):
		channels.append(data[chn::audiofile.channels])
	channels = np.array(channels)
	return channels, audiofile.frame_rate


def getOneMp3(filename):
	channels, fs = readFileWav(filename)
	channel_samples = channels[0]
	# wsize = DEFAULT_WINDOW_SIZE
	# wratio = DEFAULT_OVERLAP_RATIO
	# arr2D = mlab.specgram(
	# 	channel_samples,
	# 	NFFT=wsize,
	# 	Fs=fs,
	# 	window=mlab.window_hanning,
	# 	noverlap=int(wsize * wratio))[0]
	# return arr2D
	return fingerprint(channel_samples, Fs = fs)
	# print(fs)
	# for channel in channels:
		# hashes = fingerprint(channel, Fs=fs)
		# print(len(set(hashes)))

def getSampleData(filename, retStr = False):
	# print(idx, filename)
	data = getOneMp3(filename)
	data = data.T
	ret = {'filename': filename}
	for k, v in enumerate(data):
		ret['mean_%s'%k] = np.mean(v)
		ret['std_%s'%k] = np.std(v)
		ret['max_%s'%k] = np.max(v)
		ret['min_%s'%k] = np.min(v)
	if retStr:
		return json.dumps(ret)
	return ret

def getSampleDataJava(filename):
	# print(idx, filename)
	data = getOneMp3(filename)
	data = data.T
	ret = {'filename': filename}
	for k, v in enumerate(data):
		ret['mean_%s'%k] = float(np.mean(v))
		ret['std_%s'%k] = float(np.std(v))
		ret['max_%s'%k] = float(np.max(v))
		ret['min_%s'%k] = float(np.min(v))
	# return filename
	return json.dumps(ret)

# if __name__ == '__main__':
# 	filenameList = glob('music/*.mp3')
# 	filenameList.sort()
# 	result = []
# 	for idx, filename in enumerate(filenameList):
# 		print(idx, filename)
# 		data = getOneMp3(filename)
# 		data = data.T
# 		# ret = []
# 		ret = getSampleData(filename)
# 		result.append(ret)
# 	# 	for k, v in enumerate(ret):
# 	# 		data['fft_%s'%k] = np.mean(v)
# 	# 	result.append(data)
# 	header = ['filename']
# 	for k in range(data.shape[0]):
# 		header.append('mean_%s'%k)
# 		header.append('std_%s'%k)
# 		header.append('max_%s'%k)
# 		header.append('min_%s'%k)
# 	data = json_normalize(result)
# 	data.to_csv('data_peak.csv', columns = header)
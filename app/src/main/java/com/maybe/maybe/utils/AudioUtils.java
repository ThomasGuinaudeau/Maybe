package com.maybe.maybe.utils;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioUtils {
    private static float[] pcmToFloat(byte[] pcm) {
        float[] result = new float[pcm.length / 2];
        for (int i = 0; i < result.length; i++) {
            int low = pcm[2 * i] & 0xff;
            int high = pcm[2 * i + 1];
            short sample = (short) ((high << 8) | low);
            result[i] = sample / 32768f;
        }
        return result;
    }

    private static int selectAudioTrack(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

    public static float decodeAndAnalyzeLoudness(String filePath) {
        long oneHourInMicroSec = 3600000000L;
        try {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(filePath);

            int trackIndex = selectAudioTrack(extractor);
            if (trackIndex == -1) throw new Exception("No audio track found");
            extractor.selectTrack(trackIndex);

            MediaFormat format = extractor.getTrackFormat(trackIndex);
            long durationUs = format.getLong(MediaFormat.KEY_DURATION);
            if (durationUs > oneHourInMicroSec)
                throw new IOException("Audio longer than 1 hour");
            long halfDurationUs = durationUs / 2;

            String mime = format.getString(MediaFormat.KEY_MIME);
            MediaCodec codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format, null, null, 0);
            codec.start();

            float lufs1 = decodeHalf(extractor, codec, 0, halfDurationUs);
            float lufs2 = decodeHalf(extractor, codec, halfDurationUs, durationUs);

            codec.stop();
            codec.release();

            return (lufs1 + lufs2) / 2;
        } catch (Exception e) {
            e.printStackTrace();
            return 99;
        }
    }

    private static float decodeHalf(MediaExtractor extractor, MediaCodec codec, long startUs, long endUs) {
        extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

        boolean inputDone = false;
        boolean outputDone = false;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        float energySum = 0;
        long sampleCount = 0;
        final long TIMEOUT_US = 10000;

        while (!outputDone) {
            if (!inputDone) {
                int inputIndex = codec.dequeueInputBuffer(TIMEOUT_US);
                if (inputIndex >= 0) {
                    ByteBuffer inputBuffer = codec.getInputBuffer(inputIndex);
                    int size = extractor.readSampleData(inputBuffer, 0);

                    if (size < 0 || extractor.getSampleTime() > endUs) {
                        codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                    } else {
                        long presentationTimeUs = extractor.getSampleTime();
                        codec.queueInputBuffer(inputIndex, 0, size, presentationTimeUs, 0);
                        extractor.advance();
                    }
                }
            }

            int outputIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US);
            if (outputIndex >= 0) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(outputIndex);
                byte[] pcm = new byte[info.size];
                outputBuffer.get(pcm);
                outputBuffer.clear();

                float[] samples = pcmToFloat(pcm);
                for (float s : samples) {
                    energySum += s * s;
                    sampleCount++;
                }

                codec.releaseOutputBuffer(outputIndex, false);

                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0 || extractor.getSampleTime() > endUs) {
                    outputDone = true;
                }
            }
        }

        double rms = Math.sqrt(energySum / sampleCount);
        double lufs = 20 * Math.log10(rms);
        return (float) lufs;
    }
}

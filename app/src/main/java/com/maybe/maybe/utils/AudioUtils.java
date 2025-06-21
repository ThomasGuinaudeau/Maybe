package com.maybe.maybe.utils;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;

public class AudioUtils {
    public static int[] getSampleRateAndChannels(String path) {
        int sampleRate = 44100;
        int channels = 2;
        try {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(path);
            MediaFormat format = extractor.getTrackFormat(0);

            if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            }
            if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            }
            extractor.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{ sampleRate, channels };
    }

    public static double getRMS(byte[] pcm, float sampleRate, int channels) {
        if (pcm == null)
            return 99;

        int size = 2048;
        int overlap = 1024;

        TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(sampleRate, 16, channels, true, false);

        ByteArrayInputStream bais = new ByteArrayInputStream(pcm);
        UniversalAudioInputStream audioStream = new UniversalAudioInputStream(bais, format);

        AudioDispatcher dispatcher = new AudioDispatcher(audioStream, size, overlap);

        final double[] sum = { 0.0 };
        final int[] count = { 0 };

        dispatcher.addAudioProcessor(new AudioProcessor() {
            @Override
            public void processingFinished() {}

            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] buffer = audioEvent.getFloatBuffer();
                for (float sample : buffer) {
                    sum[0] += sample * sample;
                    count[0]++;
                }
                return true;
            }
        });
        dispatcher.run();
        double mean = sum[0] / count[0];
        return 10 * Math.log10(mean);
    }

    public static byte[] audioToPCM(String path) {
        try {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(path);

            int audioTrackIndex = -1;
            MediaFormat format = null;

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i;
                    extractor.selectTrack(i);
                    break;
                }
            }

            if (audioTrackIndex == -1) {
                throw new IOException("No audio track found in ");
            }

            MediaCodec decoder = MediaCodec.createDecoderByType(Objects.requireNonNull(format.getString(MediaFormat.KEY_MIME)));
            decoder.configure(format, null, null, 0);
            decoder.start();

            ByteArrayOutputStream pcmOutput = new ByteArrayOutputStream();
            boolean inputDone = false;
            boolean outputDone = false;
            final long TIMEOUT_US = 1000;

            while (!outputDone) {
                if (!inputDone) {
                    int inputBufferId = decoder.dequeueInputBuffer(TIMEOUT_US);
                    if (inputBufferId >= 0) {
                        ByteBuffer inputBuffer = decoder.getInputBuffer(inputBufferId);
                        int size = extractor.readSampleData(inputBuffer, 0);
                        if (size < 0) {
                            decoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        } else {
                            long time = extractor.getSampleTime();
                            decoder.queueInputBuffer(inputBufferId, 0, size, time, 0);
                            extractor.advance();
                        }
                    }
                }

                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                int outputBufferId = decoder.dequeueOutputBuffer(info, TIMEOUT_US);
                if (outputBufferId >= 0) {
                    ByteBuffer outputBuffer = decoder.getOutputBuffer(outputBufferId);
                    byte[] pcm = new byte[info.size];
                    outputBuffer.get(pcm);
                    outputBuffer.clear();
                    pcmOutput.write(pcm);
                    decoder.releaseOutputBuffer(outputBufferId, false);

                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true;
                    }
                }
            }

            decoder.stop();
            decoder.release();
            extractor.release();

            return pcmOutput.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

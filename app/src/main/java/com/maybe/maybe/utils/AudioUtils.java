package com.maybe.maybe.utils;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;

import java.nio.ByteBuffer;

public class AudioUtils {

    public static byte[] extractPCM2(Context context, Uri uri) throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(context, uri, null);

        MediaFormat format = null;
        int audioTrackIndex = -1;
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat f = extractor.getTrackFormat(i);
            String mime = f.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                format = f;
                audioTrackIndex = i;
                break;
            }
        }
        extractor.selectTrack(audioTrackIndex);

        MediaCodec decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
        decoder.configure(format, null, null, 0);
        decoder.start();

        boolean sawInputEOS = false;
        boolean sawOutputEOS = false;
        final long TIMEOUT_US = 1000;

        byte[] pcm = null;
        while (!sawOutputEOS) {
            if (!sawInputEOS) {
                int inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = decoder.getInputBuffer(inputBufferIndex);
                    assert inputBuffer != null;
                    int sampleSize = extractor.readSampleData(inputBuffer, 0);

                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        sawInputEOS = true;
                    } else {
                        long presentationTimeUs = extractor.getSampleTime();
                        decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0);
                        extractor.advance();
                    }
                }
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);

            if (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = decoder.getOutputBuffer(outputBufferIndex);
                assert outputBuffer != null;

                byte[] pcmChunk = new byte[bufferInfo.size];
                outputBuffer.get(pcmChunk);
                outputBuffer.clear();

                pcm = pcmChunk;

                decoder.releaseOutputBuffer(outputBufferIndex, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    sawOutputEOS = true;
                }
            }
        }
        return pcm;
    }
}

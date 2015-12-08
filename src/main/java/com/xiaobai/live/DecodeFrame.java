package com.xiaobai.live;

import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.Global;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

/**
 * 视频截屏核心服务类
 * 
 * @author hpb
 * 
 */
public class DecodeFrame {

	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
	String lastName = "";
	MediaPicture picture;
	MediaPictureConverter converter;
	BufferedImage image = null;

	public void captureImage(String filename, String dir) throws IOException,
			InterruptedException {
		Demuxer demuxer = Demuxer.make();
		if (!new File(dir).exists()) {
			new File(dir).mkdir();
		}
		try {
			demuxer.open(filename, null, false, true, null, null);

			int numStreams = demuxer.getNumStreams();

			int videoStreamId = -1;
			long streamStartTime = Global.NO_PTS;
			Decoder videoDecoder = null;
			for (int i = 0; i < numStreams; i++) {
				final DemuxerStream stream = demuxer.getStream(i);
				streamStartTime = stream.getStartTime();
				final Decoder decoder = stream.getDecoder();
				if (decoder != null
						&& decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
					videoStreamId = i;
					videoDecoder = decoder;
					break;
				}
			}
			if (videoStreamId == -1)
				throw new RuntimeException(
						"could not find video stream in container: " + filename);

			videoDecoder.open(null, null);

			picture = MediaPicture.make(videoDecoder.getWidth(),
					videoDecoder.getHeight(), videoDecoder.getPixelFormat());
			converter = MediaPictureConverterFactory.createConverter(
					MediaPictureConverterFactory.HUMBLE_BGR_24, picture);

			long systemStartTime = System.nanoTime();
			final Rational systemTimeBase = Rational.make(1, 10000000);
			final Rational streamTimebase = videoDecoder.getTimeBase();

			final MediaPacket packet = MediaPacket.make();
			while (demuxer.read(packet) >= 0) {
				if (packet.getStreamIndex() == videoStreamId) {
					int offset = 0;
					int bytesRead = 0;
					do {
						bytesRead += videoDecoder.decode(picture, packet,
								offset);
						if (picture.isComplete()) {
							image = displayVideoAtCorrectTime(streamStartTime,
									systemStartTime, systemTimeBase,
									streamTimebase, dir);
						}
						offset += bytesRead;
					} while (offset < packet.getSize());
				}
			}
			do {
				videoDecoder.decode(picture, null, 0);
				if (picture.isComplete()) {
					image = displayVideoAtCorrectTime(streamStartTime,
							systemStartTime, systemTimeBase, streamTimebase,
							dir);
				}
			} while (picture.isComplete());

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			demuxer.close();
		}
	}

	long first = 0, lastSystemTimestamp;

	private BufferedImage displayVideoAtCorrectTime(long streamStartTime,
			long systemStartTime, final Rational systemTimeBase,
			final Rational streamTimebase, final String dir)
			throws InterruptedException {
		long streamTimestamp = picture.getTimeStamp();
		streamTimestamp = systemTimeBase.rescale(streamTimestamp
				- streamStartTime, streamTimebase);
		long systemTimestamp = System.nanoTime();
		int waitCount = 0;
		while (streamTimestamp > (systemTimestamp - systemStartTime + 1000000)) {
			Thread.sleep(100);
			systemTimestamp = System.nanoTime();
			waitCount++;
			if (waitCount > 8) {
				break;
			}
		}

		image = converter.toImage(image, picture);
		lastName = df.format(new Date());
		try {
			ImageIO.write(image, "jpg", new File(dir + lastName + ".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}

}

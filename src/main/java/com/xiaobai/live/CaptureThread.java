package com.xiaobai.live;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 视频截屏线程
 * 
 * @author hpb
 * 
 */
public class CaptureThread extends Thread {
	private String url;
	private String dir;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public CaptureThread(String url, String dir) {
		this.dir = dir;
		this.url = url;
	}

	@Override
	public void run() {
		logger.info("start CaptureThread,save:" + dir);
		while (true) {
			try {
				new DecodeFrame().captureImage(url, dir + "/");
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.info("DecodeFrame is Interrupted:");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("restart CaptureThread,save:" + dir);
		}

	}
}

package com.xiaobai.live;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptureEntrance {
	private static final Logger logger = LoggerFactory
			.getLogger(CaptureEntrance.class);

	public static void main(String[] args) {
		Properties pro = new Properties();
		FileInputStream in;
		String root = System.getProperty("user.dir");
		File[] videoConfigs = new File(root).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".properties");
			}
		});
		if (videoConfigs == null || videoConfigs.length == 0) {
			logger.error("无视频配置信息");
			return;
		}
		for (File configFile : videoConfigs) {
			String url = "", collectImagePath = "";
			try {
				in = new FileInputStream(configFile);
				pro.load(in);
				url = (String) pro.get("videoUrl");
				collectImagePath = (String) pro.get("saveImagePath");
				if (!new File(collectImagePath).exists()) {
					new File(collectImagePath).mkdir();
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			new CaptureThread(url, collectImagePath).start();
		}

	}
}

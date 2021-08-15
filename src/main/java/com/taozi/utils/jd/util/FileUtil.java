package com.taozi.utils.jd.util;

import cn.hutool.core.date.DateUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

/**
 * 文件操作工具类
 */
public class FileUtil {
	/**
	 * 读取文件内容为二进制数组
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static byte[] read(String filePath) throws IOException {

		InputStream in = new FileInputStream(filePath);
		byte[] data = inputStream2ByteArray(in);
		in.close();

		return data;
	}

	/**
	 * 流转二进制数组
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	static byte[] inputStream2ByteArray(InputStream in) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}

	/**
	 * 保存文件
	 * 
	 * @param filePath
	 * @param fileName
	 * @param content
	 */
	public static void save(String filePath, String fileName, byte[] content) {
		try {
			File filedir = new File(filePath);
			if (!filedir.exists()) {
				filedir.mkdirs();
			}
			File file = new File(filedir, fileName);
			OutputStream os = new FileOutputStream(file);
			os.write(content, 0, content.length);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * multipartfile文件保存到本地
	 *
	 * @param file
	 * @param localPath
	 * @return 拼接url
	 */
	public static String multipartfileToLocal(MultipartFile file, String localPath) {
		//获取上传文件名,包含后缀
		String originalFilename = file.getOriginalFilename();
		//获取后缀
		String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
		//保存的文件名
		String dFileName = UUID.randomUUID() + substring;
		//保存路径
		//springboot 默认情况下只能加载 resource文件夹下静态资源文件
		//确保文件路径存在
		File file1 = new File(localPath + "/" + DateUtil.today());
		if (!file1.exists()) {
			file1.mkdirs();
		}
		//生成保存文件
		File uploadFile = new File(localPath + "/" + DateUtil.today() + "/" + dFileName);
		System.out.println("完整上传路径为： " + uploadFile);
		//将上传文件保存到路径
		try {
			file.transferTo(uploadFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return localPath + "/" + DateUtil.today() + "/" + dFileName;
	}
}

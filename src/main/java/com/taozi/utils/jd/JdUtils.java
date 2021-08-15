package com.taozi.utils.jd;

import com.alibaba.fastjson.JSONObject;
import com.bfz.core.tool.xunfei.jd.entity.Property;
import com.bfz.core.tool.xunfei.jd.util.FileUtil;
import com.bfz.core.tool.xunfei.jd.util.HttpUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 车牌识别 JD WebAPI 接口工具类
 * 运行前：请先填写Appid、APIKey、APISecret
 * 接口文档（必看）：https://www.xfyun.cn/doc/words/vehicleLicensePlateRecg/API.html
 *
 * @author taozi
 */
public class JdUtils {

	/**
	 * 构建传递的参数
	 * @param imageBase64
	 * @param imageEncoding
	 * @return String params
	 */
	public static String getParam(String imageBase64, String imageEncoding) {
		JSONObject jso = new JSONObject();

		// header
		JSONObject header = new JSONObject();
		header.put("app_id", Property.APP_ID);
		header.put("status", 3);

		jso.put("header", header);

		// parameter
		JSONObject parameter = new JSONObject();
		JSONObject service = new JSONObject();

		JSONObject faceCompareResult = new JSONObject();
		faceCompareResult.put("encoding", "utf8");
		faceCompareResult.put("format", "json");
		faceCompareResult.put("compress", "raw");
		service.put("carLicenseRes", faceCompareResult);
		parameter.put("jd_ocr_car", service);
		jso.put("parameter", parameter);

		// payload
		JSONObject payload = new JSONObject();
		JSONObject inputImage = new JSONObject();
		inputImage.put("encoding", imageEncoding);
		inputImage.put("image", imageBase64);
		//3:一次性传完
		inputImage.put("status", 3);
		payload.put("carImgBase64Str", inputImage);

		System.out.println(jso.toString());
		jso.put("payload", payload);
		return jso.toString();
	}

	/**
	 * 读取image
	 * @param imagePath
	 * @return imageByteArray1
	 * @throws IOException
	 */
	public static byte[] readImage(String imagePath) throws IOException {
		return FileUtil.read(imagePath);
	}

	/**
	 * 通过指定路径发送
	 * @param imageUrl
	 * @return String
	 * @throws Exception
	 */
	public static String imageContrast(String imageUrl) throws Exception {
		// 图片转换成base64
		String imageBase64 = Base64.getEncoder().encodeToString(readImage(imageUrl));
		// 图片格式
		String imageEncoding = imageUrl.substring(imageUrl.lastIndexOf(".") + 1);
		// send url
		String url = assembleRequestUrl(Property.REQUEST_URL, Property.API_KEY, Property.API_SECRET);
		// 发送给指定url指定的参数
		String respData = handleImageContrastRes(url, getParam(imageBase64, imageEncoding));
		if (null == respData) {
			return null;
		}
		// 处理返回的内容，只返回给前端 “text”
		JSONObject jsonObject = JSONObject.parseObject(respData);
		JSONObject payload = (JSONObject) jsonObject.get("payload");
		JSONObject carLicenseRes = (JSONObject) payload.get("carLicenseRes");
		String text = carLicenseRes.getString("text");
		return new String(org.apache.commons.codec.binary.Base64.decodeBase64(text.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 直接传入 MultipartFile file 进行处理
	 * @param file
	 * @return String
	 * @throws Exception
	 */
	public static String imageContrast(MultipartFile file) throws Exception {
		// 图片转换成base64
		String imageBase64 = Base64.getEncoder().encodeToString(file.getBytes());
		// 图片名称
		String originalFilename = file.getOriginalFilename();
		// 提取图片后缀
		String imageEncoding = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
		// 拼接发送的url
		String url = assembleRequestUrl(Property.REQUEST_URL, Property.API_KEY, Property.API_SECRET);
		// 接收 response 内容
		String respData = handleImageContrastRes(url, getParam(imageBase64, imageEncoding));
		if (null == respData) {
			return null;
		}
		// 处理 response 内容，只返回 “text”
		JSONObject jsonObject = JSONObject.parseObject(respData);
		JSONObject payload = (JSONObject) jsonObject.get("payload");
		JSONObject carLicenseRes = (JSONObject) payload.get("carLicenseRes");
		String text = carLicenseRes.getString("text");
		return new String(org.apache.commons.codec.binary.Base64.decodeBase64(text.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 发送参数到指定url
	 * @param url
	 * @param bodyParam
	 * @return String result
	 */
	private static String handleImageContrastRes(String url, String bodyParam) {
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-type", "application/json");
		String result = HttpUtil.doPost2(url, headers, bodyParam);
		if (result != null) {
			System.out.println("车牌识别接口调用结果：" + result);
			return result;
		} else {
			return null;
		}
	}

	/**
	 * 构建url
	 * @param requestUrl
	 * @param apiKey
	 * @param apiSecret
	 * @return String url
	 */
	public static String assembleRequestUrl(String requestUrl, String apiKey, String apiSecret) {
		URL url = null;
		// 替换调schema前缀 ，原因是URL库不支持解析包含ws,wss schema的url
		String httpRequestUrl = requestUrl.replace("ws://", "http://").replace("wss://", "https://");
		try {
			url = new URL(httpRequestUrl);
			//获取当前日期并格式化
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			String date = format.format(new Date());

			String host = url.getHost();
			if (url.getPort() != 80 && url.getPort() != 443) {
				host = host + ":" + String.valueOf(url.getPort());
			}
			StringBuilder builder = new StringBuilder("host: ").append(host).append("\n").
					append("date: ").append(date).append("\n").
					append("POST ").append(url.getPath()).append(" HTTP/1.1");
			Charset charset = Charset.forName("UTF-8");
			Mac mac = Mac.getInstance("hmacsha256");
			SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
			mac.init(spec);
			byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
			String sha = Base64.getEncoder().encodeToString(hexDigits);

			String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
			String authBase = Base64.getEncoder().encodeToString(authorization.getBytes(charset));
			return String.format("%s?authorization=%s&host=%s&date=%s", requestUrl, URLEncoder.encode(authBase), URLEncoder.encode(host), URLEncoder.encode(date));

		} catch (Exception e) {
			throw new RuntimeException("assemble requestUrl error:" + e.getMessage());
		}
	}
}

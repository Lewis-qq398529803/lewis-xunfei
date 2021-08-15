package com.taozi.controller;

import com.alibaba.fastjson.JSONArray;
import com.taozi.utils.StringUtils;
import com.taozi.utils.jd.JdUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 讯飞车牌识别jd接口
 *
 * @author taozi - 2021年8月10日, 010 - 17:11:59
 */
@Api(tags = "科大讯飞：jd")
@RestController
@RequestMapping("/xf/jd")
@Slf4j
public class XunFeiJdController {

	@PostMapping("/getJdData")
	@ApiOperation(value = "识别车牌", notes = "传入MultipartFile file，将返回一个数组字符串，因为不一定只识别到一个车牌，有可能是许多个，按需取数据。")
	public JSONArray getJdData(@RequestParam(value="file", required=false) MultipartFile file) throws Exception {
		String contentType = file.getContentType();
		String jpeg = "image/jpeg";
		String png = "image/png";
		if (!StringUtils.equals(contentType, jpeg) && !StringUtils.equals(contentType, png)) {
			log.error("请检查图片格式是否为：jpg、png、jpeg，其他格式暂不支持，或者不为图片格式。");
			return new JSONArray();
		}
		// 路径提供工具类 得到讯飞返回的数据
		String respData = JdUtils.imageContrast(file);
		if (null == respData) {
			log.error("车牌识别结果为空，或者出现异常，例如appid等已过期！");
			return new JSONArray();
		}
		log.info("车牌识别结果(text)base64解码后：\n" + respData);
		return JSONArray.parseArray(respData);
	}

}

package com.study.gmalllogger.controller;

import com.alibaba.fastjson.JSONObject;
import com.study.gmall.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * @author: helloWorld
 * @date  : Created in 2020/12/15 17:57
 */
@RestController   // 等价于: @Controller + @ResponseBody
public class LoggerController {
    private Logger logger = LoggerFactory.getLogger(LoggerController.class);

    // 使用注入的方式来实例化 KafkaTemplate. Spring boot 会自动完成
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    //    @RequestMapping(value = "/log", method = RequestMethod.POST)
    //    @ResponseBody  //表示返回值是一个 字符串, 而不是 页面名
    @PostMapping("/log")  // 等价于: @RequestMapping(value = "/log", method = RequestMethod.POST)
    public String doLog(@RequestParam("log") String log) {
        System.out.println(log);
        //1. 给日志添加时间戳 (客户端的时间有可能不准, 所以使用服务器端的时间)
        JSONObject jsonObject = addTs(log);
        //2. 日志落盘,可以供其他离线使用(使用flum监听日志文件)
        saveLogToFile(jsonObject);
        //3. 日志发送 kafka
        sendToKafka(jsonObject);
        return "success";
    }

    private void sendToKafka(JSONObject jsonObject) {
        String logType = jsonObject.getString("logType");
        String topicName = Constant.TOPIC_EVENT;
        if ("startup".equals(logType)) {
            topicName = Constant.TOPIC_STARTUP;
        }
        kafkaTemplate.send(topicName, jsonObject.toJSONString());
    }

    //使用log4j将日志写入到文件
    private void saveLogToFile(JSONObject log) {
        logger.info(log.toJSONString());
    }

    private JSONObject addTs(String log) {
        JSONObject jsonObject = JSONObject.parseObject(log);
        jsonObject.put("ts", System.currentTimeMillis());
        return jsonObject;
    }
}

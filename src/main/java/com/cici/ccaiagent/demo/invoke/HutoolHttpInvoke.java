package com.cici.ccaiagent.demo.invoke;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用 Hutool HTTP 工具调用阿里云通义千问 API
 * 对应 curl 命令的 Java 实现
 */
public class HutoolHttpInvoke {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    /**
     * 方式 1: 最简调用 - 直接传入问题和系统提示
     *
     * @param apiKey       API Key
     * @param model        模型名称（如：qwen-plus）
     * @param systemPrompt 系统提示词
     * @param userQuestion 用户问题
     * @return AI 回答内容
     */
    public static String simpleCall(String apiKey, String model, 
                                    String systemPrompt, String userQuestion) {
        // 构建消息列表
        List<Map<String, String>> messages = Arrays.asList(
            MapUtil.builder(new HashMap<String, String>())
                .put("role", "system")
                .put("content", systemPrompt)
                .build(),
            MapUtil.builder(new HashMap<String, String>())
                .put("role", "user")
                .put("content", userQuestion)
                .build()
        );

        return callApi(apiKey, model, messages);
    }

    /**
     * 方式 2: 灵活调用 - 传入完整的消息列表
     *
     * @param apiKey   API Key
     * @param model    模型名称
     * @param messages 消息列表，每个消息包含 role 和 content
     * @return AI 回答内容
     */
    public static String flexibleCall(String apiKey, String model, 
                                      List<Map<String, String>> messages) {
        return callApi(apiKey, model, messages);
    }

    /**
     * 方式 3: 原始调用 - 返回完整响应
     *
     * @param apiKey   API Key
     * @param model    模型名称
     * @param messages 消息列表
     * @return 完整的 JSON 响应
     */
    public static String rawCall(String apiKey, String model, 
                                 List<Map<String, String>> messages) {
        // 构建请求体（完全对应 curl 的 --data）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);
        requestBody.put("input", input);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("result_format", "message");
        requestBody.put("parameters", parameters);

        // 发送 POST 请求（对应 curl 的所有参数）
        try (HttpResponse response = HttpRequest.post(API_URL)
                .header("Authorization", "Bearer " + apiKey)  // --header "Authorization"
                .header("Content-Type", "application/json")   // --header "Content-Type"
                .body(JSONUtil.toJsonStr(requestBody))        // --data
                .execute()) {
            
            return response.body();
        }
    }

    /**
     * 核心调用方法
     */
    private static String callApi(String apiKey, String model, 
                                  List<Map<String, String>> messages) {
        // 获取原始响应
        String rawResponse = rawCall(apiKey, model, messages);
        
        // 解析响应
        JSONObject jsonResponse = JSONUtil.parseObj(rawResponse);
        
        // 检查是否有错误
        if (jsonResponse.containsKey("code")) {
            String errorMsg = jsonResponse.getStr("message", "未知错误");
            throw new RuntimeException("API 调用失败：" + errorMsg);
        }

        // 提取 AI 回复的内容
        try {
            JSONObject output = jsonResponse.getJSONObject("output");
            if (output == null) {
                throw new RuntimeException("响应中缺少 output 字段");
            }
            
            var choices = output.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("响应中没有 choices");
            }
            
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            if (message == null) {
                throw new RuntimeException("响应中没有 message");
            }
            
            String content = message.getStr("content");
            if (content == null) {
                throw new RuntimeException("AI 回答内容为空");
            }
            
            return content;
            
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("解析响应失败：" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 测试 Hutool 调用通义千问 API ===\n");

        try {
            // 测试 1: 最简调用方式
            System.out.println("【测试 1】最简调用方式");
            String answer1 = simpleCall(
                TestApiKey.API_KEY,
                "qwen-plus",
                "You are a helpful assistant.",
                "你是谁？"
            );
            System.out.println("AI 回答：" + answer1);
            System.out.println();

            // 测试 2: 多轮对话
            System.out.println("【测试 2】多轮对话（灵活调用）");
            List<Map<String, String>> messages = Arrays.asList(
                MapUtil.builder(new HashMap<String, String>())
                    .put("role", "system")
                    .put("content", "你是一个专业的编程助手。")
                    .build(),
                MapUtil.builder(new HashMap<String, String>())
                    .put("role", "user")
                    .put("content", "你好，我是 Lucky，我正在学习服务端相关知识")
                    .build(),
                MapUtil.builder(new HashMap<String, String>())
                    .put("role", "assistant")
                    .put("content", "你好 Lucky！很高兴能帮助你学习服务端知识。有什么问题尽管问我！")
                    .build(),
                MapUtil.builder(new HashMap<String, String>())
                    .put("role", "user")
                    .put("content", "Java 好学吗？")
                    .build()
            );
            
            String answer2 = flexibleCall(TestApiKey.API_KEY, "qwen-plus", messages);
            System.out.println("AI 回答：" + answer2);
            System.out.println();

            // 测试 3: 查看完整响应
            System.out.println("【测试 3】查看完整 JSON 响应");
            List<Map<String, String>> simpleMessages = Arrays.asList(
                MapUtil.builder(new HashMap<String, String>())
                    .put("role", "system")
                    .put("content", "You are a helpful assistant.")
                    .build(),
                MapUtil.builder(new HashMap<String, String>())
                    .put("role", "user")
                    .put("content", "用一句话介绍你自己")
                    .build()
            );
            
            String rawResponse = rawCall(TestApiKey.API_KEY, "qwen-plus", simpleMessages);
            System.out.println("完整响应:\n" + JSONUtil.formatJsonStr(rawResponse));

        } catch (Exception e) {
            System.err.println("❌ 调用失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}

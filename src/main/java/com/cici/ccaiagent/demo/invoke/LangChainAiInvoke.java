package com.cici.ccaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class LangChainAiInvoke {
    public static void main(String[] args) {
        ChatLanguageModel model = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();

        String answer = model.chat("你好，我是Lucky，我目前正在学习ai agent项目");
        System.out.println(answer);
    }
}

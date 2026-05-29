package com.li.ai_job_market.AI.tools;

import okhttp3.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 全网智能搜索工具，基于千帆 AI 搜索引擎进行联网检索
 */
@Component
public class SearchTool {
    @Value("${spring.ai.qianfan.Bearer}")
    private String Bearer;

    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().readTimeout(300, TimeUnit.SECONDS).build();

    @Tool(description = "全网智能搜索工具，根据用户问题联网检索最新网络资讯")
    public String search(@ToolParam(description = "用户需要上网搜索查询的具体问题") String query) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        String requestJson = "{" +
                "\"instruction\":\"请根据联网搜索结果，客观总结回答用户问题，引用搜索来源，简洁准确作答\"," +
                "\"messages\":[{" +
                "\"role\":\"user\",\"content\":\"" + query + "\"}" +
                "]," +
                "\"resource_type_filter\":[" +
                "{\"type\":\"web\",\"top_k\":20}," +
                "{\"type\":\"video\",\"top_k\":1}," +
                "{\"type\":\"image\",\"top_k\":1}" +
                "]," +
                "\"search_filter\":{" +
                "\"match\":{\"site\":[\"tieba.baidu.com\",\"baike.baidu.com\"]}," +
                "\"range\":{\"page_time\":{\"gt\":\"now-1w/d\"}}" +
                "}" +
                "}";
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url("https://qianfan.baidubce.com/v2/ai_search/web_summary")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", Bearer)
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            // 增加安全判断，防止空指针
            if (response.body() != null) {
                return response.body().string();
            } else {
                return "搜索接口返回空";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "搜索异常：" + e.getMessage();
        }
    }

}

package com.li.ai_job_market.AI.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 网页爬取工具，用于抓取指定 URL 的网页内容
 */
public class WebScrapingTool {

    @Tool(description = "抓取网页内容")
    public String scrapeWebPage(@ToolParam(description = "要抓取的网页的URL") String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.html();
        } catch (IOException e) {
            return "抓取网页时出错：" + e.getMessage();
        }
    }
}

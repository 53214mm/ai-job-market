package com.li.ai_job_market.controller;

import com.li.ai_job_market.AI.agent.JobAgent;
import com.li.ai_job_market.AI.app.JobApp;
import com.li.ai_job_market.constant.FileConstant;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private JobApp jobApp;
    @Resource
    private ToolCallback[] allTools;
    @Resource
    @Qualifier("dashScopeChatModel")
    private ChatModel dashscopeChatModel;

    @GetMapping("/love_app/chat/sync")
    public String chat(String message, String chatId) {
        return jobApp.doChat(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return jobApp.doChatByStream(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return jobApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }

    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        SseEmitter emitter = new SseEmitter(180000L);
        jobApp.doChatByStream(message, chatId).subscribe(
                chunk -> {
                    try { emitter.send(chunk); } catch (IOException e) { emitter.completeWithError(e); }
                },
                emitter::completeWithError,
                emitter::complete
        );
        return emitter;
    }

    private final java.util.concurrent.ConcurrentHashMap<String, JobAgent> manusSessions = new java.util.concurrent.ConcurrentHashMap<>();

    @GetMapping(value = "/manus/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithManus(String message, String chatId) {
        JobAgent jobAgent = manusSessions.computeIfAbsent(
            chatId != null ? chatId : "default",
            k -> new JobAgent(allTools, dashscopeChatModel)
        );
        return jobAgent.runStream(message);
    }

    /**
     * 文件下载接口 —— 将 tmp/ 目录下的文件提供给前端下载
     */
    @GetMapping("/file/download")
    public ResponseEntity<FileSystemResource> downloadFile(String path) throws IOException {
        File file = new File(FileConstant.FILE_SAVE_DIR, path);
        String canonicalPath = file.getCanonicalPath();
        String allowedBase = new File(FileConstant.FILE_SAVE_DIR).getCanonicalPath();
        if (!canonicalPath.startsWith(allowedBase) || !file.exists() || file.isDirectory()) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}

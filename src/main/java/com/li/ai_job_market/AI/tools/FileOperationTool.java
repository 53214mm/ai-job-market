package com.li.ai_job_market.AI.tools;

import cn.hutool.core.io.FileUtil;
import com.li.ai_job_market.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类
 */
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "读取文件内容")
    public String readFile(@ToolParam(description = "要读取的文件名") String fileName){
        String filePath = FILE_DIR + "/" + fileName;
        // 读取文件内容并返回
        try{
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "读取文件失败: " + e.getMessage();
        }
    }

    @Tool(description = "写入文件内容")
    public String writeFile(@ToolParam(description = "要写入的文件名")String fileName ,
                            @ToolParam(description = "要写入的内容")String content){
        String filePath = FILE_DIR + "/" + fileName;
        FileUtil.touch(filePath);
        // 写入文件内容并返回
        try{
            FileUtil.writeUtf8String(content,filePath);
            return "文件写入成功: " + filePath;
        } catch (Exception e) {
            return "写入文件失败: " + e.getMessage();
        }
    }

}

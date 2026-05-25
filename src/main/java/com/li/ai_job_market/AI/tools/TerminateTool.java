package com.li.ai_job_market.AI.tools;

import org.springframework.ai.tool.annotation.Tool;

public class TerminateTool {
  
    @Tool(description = """  
            当请求得到满足时，或当助手无法进一步执行任务时，终止交互。
            当你完成所有任务后，调用此工具以结束工作。
            """)  
    public String doTerminate() {  
        return "任务结束";  
    }  
}

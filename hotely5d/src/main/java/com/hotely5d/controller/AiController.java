package com.hotely5d.controller;

import com.hotely5d.entity.model.Result;
import com.hotely5d.entity.model.StatusCode;
import com.hotely5d.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("chat")
    public Result chat(@RequestBody Map<String, Object> body) {
        Object messageObj = body.get("message");
        String message = messageObj == null ? "" : String.valueOf(messageObj).trim();
        if (message.isEmpty()) {
            return new Result(StatusCode.ERROR, false, "消息不能为空");
        }
        List<Map<String, String>> history = parseHistory(body.get("history"));
        return new Result(true, StatusCode.OK, "成功", aiService.chat(message, history));
    }

    private List<Map<String, String>> parseHistory(Object historyObj) {
        List<Map<String, String>> result = new ArrayList<>();
        if (!(historyObj instanceof List)) {
            return result;
        }
        List<?> list = (List<?>) historyObj;
        for (Object item : list) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) item;
            Object roleObj = map.get("role");
            Object contentObj = map.get("content");
            if (roleObj == null || contentObj == null) {
                continue;
            }
            String role = String.valueOf(roleObj);
            String content = String.valueOf(contentObj).trim();
            if (content.isEmpty()) {
                continue;
            }
            if (!"user".equals(role) && !"assistant".equals(role)) {
                continue;
            }
            Map<String, String> msg = new HashMap<>();
            msg.put("role", role);
            msg.put("content", content);
            result.add(msg);
        }
        return result;
    }
}

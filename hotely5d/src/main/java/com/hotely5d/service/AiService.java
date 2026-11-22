package com.hotely5d.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotely5d.entity.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.api-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Autowired
    private StatsService statsService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> chat(String userMessage, List<Map<String, String>> history) {
        Map<String, Object> result = new HashMap<>();
        String question = userMessage == null ? "" : userMessage.trim();
        if (question.isEmpty()) {
            result.put("reply", "请输入您要咨询的问题。");
            result.put("source", "local");
            return result;
        }

        if (!isApiKeyConfigured()) {
            result.put("reply", buildFallbackReply(question));
            result.put("source", "local");
            return result;
        }

        try {
            List<Map<String, String>> messages = buildMessages(question, history);

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1024);
            requestBody.put("messages", messages);

//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Authorization", "Bearer " + apiKey.trim());
//
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
//            JsonNode root = objectMapper.readTree(response.getBody());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey.trim());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("[DeepSeek] 发送请求到: " + apiUrl);
            System.out.println("[DeepSeek] 请求体: " + objectMapper.writeValueAsString(requestBody));

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            System.out.println("[DeepSeek] 响应状态码: " + response.getStatusCode());
            System.out.println("[DeepSeek] 响应内容类型: " + response.getHeaders().getContentType());
            System.out.println("[DeepSeek] 响应正文前200字符: " +
                    (response.getBody() != null && response.getBody().length() > 200
                            ? response.getBody().substring(0, 200)
                            : response.getBody()));

            JsonNode root = objectMapper.readTree(response.getBody());


            if (root.has("error")) {
                String errMsg = root.path("error").path("message").asText("DeepSeek 接口返回错误");
                System.err.println("[DeepSeek] API error: " + errMsg);
                result.put("reply", buildFallbackReply(question));
                result.put("source", "local");
                return result;
            }

            String reply = root.path("choices").path(0).path("message").path("content").asText("").trim();
            if (reply.isEmpty()) {
                result.put("reply", buildFallbackReply(question));
                result.put("source", "local");
                return result;
            }

            result.put("reply", reply);
            result.put("source", "deepseek");
        } catch (HttpStatusCodeException e) {
            String errDetail = e.getResponseBodyAsString();
            System.err.println("[DeepSeek] HTTP " + e.getStatusCode().value() + ": " + errDetail);
            result.put("reply", buildFallbackReply(question));
            result.put("source", "local");
            result.put("error", parseApiError(errDetail));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("reply", buildFallbackReply(question));
            result.put("source", "local");
            result.put("error", e.getMessage());
        }
        return result;
    }

    private List<Map<String, String>> buildMessages(String userMessage, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> system = new LinkedHashMap<>();
        system.put("role", "system");
        system.put("content", buildSystemPromptSafe());
        messages.add(system);

        messages.addAll(sanitizeHistory(history));

        Map<String, String> user = new LinkedHashMap<>();
        user.put("role", "user");
        user.put("content", userMessage);
        messages.add(user);

        return messages;
    }

    private List<Map<String, String>> sanitizeHistory(List<Map<String, String>> history) {
        List<Map<String, String>> result = new ArrayList<>();
        if (history == null) {
            return result;
        }
        for (Map<String, String> msg : history) {
            if (msg == null) {
                continue;
            }
            String role = msg.get("role");
            String content = msg.get("content");
            if (content == null) {
                continue;
            }
            content = content.trim();
            if (content.isEmpty()) {
                continue;
            }
            if (!"user".equals(role) && !"assistant".equals(role)) {
                continue;
            }
            if (!result.isEmpty() && role.equals(result.get(result.size() - 1).get("role"))) {
                Map<String, String> last = result.get(result.size() - 1);
                last.put("content", last.get("content") + "\n" + content);
            } else {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("role", role);
                item.put("content", content);
                result.add(item);
            }
        }
        while (!result.isEmpty() && "assistant".equals(result.get(0).get("role"))) {
            result.remove(0);
        }
        if (result.size() > 10) {
            return new ArrayList<>(result.subList(result.size() - 10, result.size()));
        }
        return result;
    }

    private String parseApiError(String errDetail) {
        if (errDetail == null || errDetail.isEmpty()) {
            return "unknown";
        }
        try {
            JsonNode err = objectMapper.readTree(errDetail);
            return err.path("error").path("message").asText(errDetail);
        } catch (Exception e) {
            return errDetail;
        }
    }

    private boolean isApiKeyConfigured() {
        if (apiKey == null) {
            return false;
        }
        String key = apiKey.trim();
        return !key.isEmpty() && !"your-deepseek-api-key".equals(key);
    }

    private String buildSystemPromptSafe() {
        try {
            return buildSystemPrompt();
        } catch (Exception e) {
            e.printStackTrace();
            return "你是斌安居酒店的智能客服。请用中文友好、简洁地回答用户关于酒店预订、房型、价格、设施的问题。";
        }
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("你是斌安居酒店的官方智能客服。\n");
        sb.append("自我介绍时称「智能客服」即可，不要使用个人昵称或化名。\n");
        sb.append("请基于下方实时酒店数据回答，不要编造房型和价格。用中文简洁回答。\n\n");
        sb.append("【实时酒店数据】\n");
        sb.append(statsService.buildHotelContext());
        sb.append("\n【最新公告】\n");
        List<Notice> notices = noticeService.getTop5();
        if (notices == null || notices.isEmpty()) {
            sb.append("暂无公告。\n");
        } else {
            for (Notice notice : notices) {
                String title = notice.getTitle() != null ? notice.getTitle() : "公告";
                sb.append("- ").append(title).append("\n");
            }
        }
        sb.append("\n【酒店服务】健身房、泳池、水疗、餐厅、会议室、WiFi、24小时前台。\n");
        sb.append("【地址】长沙市商业核心区域，近五一广场。\n");
        sb.append("预订引导：请用户在网站「预约入住」页面选择房型和日期提交预约。");
        return sb.toString();
    }

    private String buildFallbackReply(String userMessage) {
        Map<String, Object> publicStats = statsService.getPublicStats();
        String msg = userMessage.toLowerCase();

        if (msg.contains("房型") || msg.contains("房间") || msg.contains("有哪些")) {
            StringBuilder sb = new StringBuilder("我们目前有以下房型：\n");
            List<Map<String, Object>> cats = (List<Map<String, Object>>) publicStats.get("categories");
            if (cats != null) {
                for (Map<String, Object> cat : cats) {
                    int avail = toInt(cat.get("availableRooms"));
                    sb.append(String.format("- %s：¥%s/晚，%s\n",
                            cat.get("categoryName"), cat.get("price"),
                            avail > 0 ? "剩余 " + avail + " 间可订" : "当前已满"));
                }
            }
            sb.append("\n点击顶部「预约入住」即可预订。");
            return sb.toString();
        }
        if (msg.contains("价格") || msg.contains("多少钱") || msg.contains("费用")) {
            StringBuilder sb = new StringBuilder("各房型参考价格：\n");
            List<Map<String, Object>> cats = (List<Map<String, Object>>) publicStats.get("categories");
            if (cats != null) {
                for (Map<String, Object> cat : cats) {
                    sb.append(String.format("- %s：¥%s/晚\n", cat.get("categoryName"), cat.get("price")));
                }
            }
            return sb.toString();
        }
        if (msg.contains("预订") || msg.contains("预约") || msg.contains("入住") || msg.contains("怎么订")) {
            return String.format("当前有 %d 间客房可预订。\n\n预订流程：\n1. 点击「预约入住」\n2. 选择房型和入住日期\n3. 提交预约等待酒店确认",
                    toInt(publicStats.get("availableRooms")));
        }
        if (msg.contains("地址") || msg.contains("位置") || msg.contains("在哪") || msg.contains("交通")) {
            return "斌安居酒店位于长沙市商业核心区域，举步可达五一广场，交通便利。欢迎您的光临！";
        }
        if (msg.contains("设施") || msg.contains("服务") || msg.contains("游泳") || msg.contains("健身")) {
            return "酒店配备：健身房、室内恒温游泳池、水疗中心、餐厅酒吧、会议宴会场地、24小时前台服务。";
        }
        if (msg.contains("公告") || msg.contains("活动") || msg.contains("优惠")) {
            List<Notice> notices = noticeService.getTop5();
            if (notices == null || notices.isEmpty()) {
                return "暂无最新公告，您可关注首页「最新公告」栏目。";
            }
            StringBuilder sb = new StringBuilder("最新公告：\n");
            for (Notice n : notices) {
                sb.append(String.format("- %s\n", n.getTitle()));
            }
            return sb.toString();
        }
        return String.format("您好！我是斌安居酒店智能客服。当前可用房间 %d 间，今日入住率 %.1f%%。您可以问我房型价格、预订方式、酒店位置等。",
                toInt(publicStats.get("availableRooms")), toDouble(publicStats.get("todayOccupancyRate")));
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }

    private double toDouble(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0; }
    }
}

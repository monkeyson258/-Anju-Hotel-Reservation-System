package com.hotely5d.controller;

import com.hotely5d.entity.model.Result;
import com.hotely5d.entity.model.StatusCode;
import com.hotely5d.service.StatsService;
import com.hotely5d.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("overview")
    public Result overview(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new Result(StatusCode.ACCESSERROR, false, "无权访问");
        }
        return new Result(true, StatusCode.OK, "查询成功", statsService.getOverview());
    }

    @GetMapping("checkInTrend")
    public Result checkInTrend(@RequestParam(defaultValue = "7") int days, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new Result(StatusCode.ACCESSERROR, false, "无权访问");
        }
        return new Result(true, StatusCode.OK, "查询成功", statsService.getCheckInTrend(days));
    }

    @GetMapping("categoryOccupancy")
    public Result categoryOccupancy(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new Result(StatusCode.ACCESSERROR, false, "无权访问");
        }
        return new Result(true, StatusCode.OK, "查询成功", statsService.getCategoryOccupancy());
    }

    @GetMapping("revenueTrend")
    public Result revenueTrend(@RequestParam(defaultValue = "6") int months, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new Result(StatusCode.ACCESSERROR, false, "无权访问");
        }
        return new Result(true, StatusCode.OK, "查询成功", statsService.getRevenueTrend(months));
    }

    @GetMapping("intelligence")
    public Result intelligence(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new Result(StatusCode.ACCESSERROR, false, "无权访问");
        }
        return new Result(true, StatusCode.OK, "查询成功", statsService.getIntelligence());
    }

    @GetMapping("public")
    public Result publicStats() {
        return new Result(true, StatusCode.OK, "查询成功", statsService.getPublicStats());
    }

    private boolean isAdmin(HttpServletRequest request) {
        Long role = JWTUtils.getRole(request);
        return role != null && role == 1;
    }
}

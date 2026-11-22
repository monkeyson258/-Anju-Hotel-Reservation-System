package com.hotely5d.service;

import com.hotely5d.dao.StatsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class StatsService {

    @Autowired
    private StatsMapper statsMapper;

    public Map<String, Object> getOverview() {
        Map<String, Object> overview = statsMapper.selectOverview();
        if (overview == null) {
            overview = new HashMap<>();
        }
        int totalRooms = toInt(overview.get("totalRooms"));
        List<Map<String, Object>> categoryOccupancy = statsMapper.selectCategoryOccupancy();
        int occupied = 0;
        for (Map<String, Object> item : categoryOccupancy) {
            occupied += toInt(item.get("occupiedRooms"));
        }
        double todayRate = totalRooms > 0 ? (occupied * 100.0 / totalRooms) : 0;
        overview.put("todayOccupancyRate", round(todayRate));
        overview.put("occupiedRooms", occupied);
        overview.put("availableRooms", statsMapper.selectAvailableRoomCount());
        return overview;
    }

    public Map<String, Object> getCheckInTrend(int days) {
        List<Map<String, Object>> dbData = statsMapper.selectCheckInTrend(days);
        Map<String, Integer> dataMap = new LinkedHashMap<>();
        for (Map<String, Object> row : dbData) {
            String date = formatDate(row.get("statDate"));
            dataMap.put(date, toInt(row.get("checkInCount")));
        }
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -(days - 1));
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        for (int i = 0; i < days; i++) {
            String key = sdf.format(cal.getTime());
            String fullKey = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            dates.add(key);
            counts.add(dataMap.getOrDefault(fullKey, 0));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        return result;
    }

    public Map<String, Object> getCategoryOccupancy() {
        List<Map<String, Object>> list = statsMapper.selectCategoryOccupancy();
        List<String> names = new ArrayList<>();
        List<Integer> occupied = new ArrayList<>();
        List<Integer> available = new ArrayList<>();
        for (Map<String, Object> item : list) {
            names.add(String.valueOf(item.get("categoryName")));
            int occ = toInt(item.get("occupiedRooms"));
            int total = toInt(item.get("totalRooms"));
            occupied.add(occ);
            available.add(total - occ);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("categoryNames", names);
        result.put("occupied", occupied);
        result.put("available", available);
        result.put("details", list);
        return result;
    }

    public Map<String, Object> getRevenueTrend(int months) {
        List<Map<String, Object>> dbData = statsMapper.selectRevenueTrend(months);
        List<String> monthLabels = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();
        for (Map<String, Object> row : dbData) {
            monthLabels.add(String.valueOf(row.get("statMonth")));
            revenues.add(toBigDecimal(row.get("revenue")));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("months", monthLabels);
        result.put("revenues", revenues);
        return result;
    }

    public Map<String, Object> getIntelligence() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> dailyRates = statsMapper.selectDailyOccupancyRate(7);
        double totalRate = 0;
        int validDays = 0;
        List<Map<String, Object>> rateDetails = new ArrayList<>();
        for (Map<String, Object> day : dailyRates) {
            int total = toInt(day.get("totalRooms"));
            int occ = toInt(day.get("occupiedRooms"));
            double rate = total > 0 ? (occ * 100.0 / total) : 0;
            Map<String, Object> detail = new HashMap<>();
            detail.put("date", formatDate(day.get("statDate")));
            detail.put("occupancyRate", round(rate));
            detail.put("occupiedRooms", occ);
            detail.put("totalRooms", total);
            rateDetails.add(detail);
            totalRate += rate;
            validDays++;
        }
        double avgRate = validDays > 0 ? totalRate / validDays : 0;
        result.put("avgOccupancyRate", round(avgRate));
        result.put("dailyRates", rateDetails);

        List<Map<String, Object>> warnings = new ArrayList<>();
        if (avgRate < 30) {
            Map<String, Object> w = new HashMap<>();
            w.put("level", "danger");
            w.put("title", "入住率过低警告");
            w.put("message", String.format("近7天平均入住率仅 %.1f%%，低于30%%阈值。建议开展促销活动、推出限时折扣或加强线上推广。", avgRate));
            w.put("suggestion", "可针对热门房型推出「连住优惠」或「周末特惠」活动，同时加大社交媒体宣传力度。");
            warnings.add(w);
        } else if (avgRate > 80) {
            Map<String, Object> w = new HashMap<>();
            w.put("level", "warning");
            w.put("title", "入住率较高提醒");
            w.put("message", String.format("近7天平均入住率达 %.1f%%，超过80%%阈值。建议预留部分房间以满足突发需求。", avgRate));
            w.put("suggestion", "建议保留2-3间备用房间，并提前与周边合作酒店建立应急调配机制。");
            warnings.add(w);
        } else {
            Map<String, Object> w = new HashMap<>();
            w.put("level", "success");
            w.put("title", "入住率正常");
            w.put("message", String.format("近7天平均入住率为 %.1f%%，处于健康区间。", avgRate));
            w.put("suggestion", "继续保持当前运营策略，可适当优化高入住率房型的定价。");
            warnings.add(w);
        }
        result.put("warnings", warnings);

        List<Map<String, Object>> categorySales = statsMapper.selectCategorySales(30);
        List<Map<String, Object>> suggestions = new ArrayList<>();
        if (!categorySales.isEmpty()) {
            Map<String, Object> best = categorySales.get(0);
            Map<String, Object> s1 = new HashMap<>();
            s1.put("type", "hot");
            s1.put("title", "热销房型");
            s1.put("message", String.format("「%s」近30天订单量最高（%d单），建议保证该房型库存充足。",
                    best.get("categoryName"), toInt(best.get("orderCount"))));
            suggestions.add(s1);

            Map<String, Object> worst = categorySales.get(categorySales.size() - 1);
            if (toInt(worst.get("orderCount")) == 0) {
                Map<String, Object> s2 = new HashMap<>();
                s2.put("type", "cold");
                s2.put("title", "滞销房型提醒");
                s2.put("message", String.format("「%s」近30天无订单，建议优化定价或加强宣传。", worst.get("categoryName")));
                s2.put("suggestion", "可考虑套餐组合销售或限时降价吸引客户体验。");
                suggestions.add(s2);
            } else if (categorySales.size() > 1) {
                Map<String, Object> s2 = new HashMap<>();
                s2.put("type", "cold");
                s2.put("title", "低销量房型");
                s2.put("message", String.format("「%s」近30天仅 %d 单，销量偏低，建议分析原因并优化。",
                        worst.get("categoryName"), toInt(worst.get("orderCount"))));
                s2.put("suggestion", "可尝试升级房间设施描述、增加实拍图或推出体验价。");
                suggestions.add(s2);
            }
        }
        result.put("categorySuggestions", suggestions);
        result.put("categorySales", categorySales);

        Map<String, Object> overview = getOverview();
        int pending = toInt(overview.get("pendingAppointments"));
        if (pending > 0) {
            Map<String, Object> s3 = new HashMap<>();
            s3.put("type", "info");
            s3.put("title", "待处理预约");
            s3.put("message", String.format("当前有 %d 条预约待确认，请及时处理以提升客户满意度。", pending));
            suggestions.add(s3);
        }
        return result;
    }

    public Map<String, Object> getPublicStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("availableRooms", statsMapper.selectAvailableRoomCount());
        List<Map<String, Object>> categories = statsMapper.selectPublicCategoryInfo();
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (Map<String, Object> cat : categories) {
            int total = toInt(cat.get("totalRooms"));
            int occ = toInt(cat.get("occupiedRooms"));
            int avail = total - occ;
            double rate = total > 0 ? (occ * 100.0 / total) : 0;
            Map<String, Object> item = new HashMap<>(cat);
            item.put("availableRooms", avail);
            item.put("occupancyRate", round(rate));
            if (avail > 0 && rate < 70) {
                item.put("recommend", true);
                item.put("recommendReason", "房源充足，预订成功率较高");
            } else if (avail > 0 && rate >= 70) {
                item.put("recommend", true);
                item.put("recommendReason", "热门房型，建议尽早预订");
            } else {
                item.put("recommend", false);
                item.put("recommendReason", "当前已满，建议选择其他房型");
            }
            recommendations.add(item);
        }
        result.put("categories", recommendations);
        Map<String, Object> overview = getOverview();
        result.put("todayOccupancyRate", overview.get("todayOccupancyRate"));
        result.put("totalRooms", overview.get("totalRooms"));
        return result;
    }

    public String buildHotelContext() {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> overview = getOverview();
        sb.append(String.format("总房间：%d 间 | 当前可订：%d 间 | 今日入住率：%.1f%% | 待处理预约：%d 条\n\n",
                toInt(overview.get("totalRooms")), toInt(overview.get("availableRooms")),
                toDouble(overview.get("todayOccupancyRate")), toInt(overview.get("pendingAppointments"))));

        sb.append("房型明细：\n");
        List<Map<String, Object>> categories = statsMapper.selectPublicCategoryInfo();
        for (Map<String, Object> cat : categories) {
            int total = toInt(cat.get("totalRooms"));
            int occ = toInt(cat.get("occupiedRooms"));
            int avail = total - occ;
            sb.append(String.format("- %s | ¥%s/晚 | 可住%d人 | 可用%d间/%d间 | %s\n",
                    cat.get("categoryName"), cat.get("price"), toInt(cat.get("liveNum")),
                    avail, total,
                    cat.get("introduce") != null ? cat.get("introduce") : ""));
        }
        return sb.toString();
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

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        try { return new BigDecimal(val.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private String formatDate(Object val) {
        if (val == null) return "";
        if (val instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd").format((Date) val);
        }
        return val.toString().substring(0, Math.min(10, val.toString().length()));
    }
}

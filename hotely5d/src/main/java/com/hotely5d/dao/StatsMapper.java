package com.hotely5d.dao;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StatsMapper {

    List<Map<String, Object>> selectCheckInTrend(@Param("days") int days);

    List<Map<String, Object>> selectCategoryOccupancy();

    List<Map<String, Object>> selectRevenueTrend(@Param("months") int months);

    Map<String, Object> selectOverview();

    List<Map<String, Object>> selectCategorySales(@Param("days") int days);

    List<Map<String, Object>> selectDailyOccupancyRate(@Param("days") int days);

    List<Map<String, Object>> selectPublicCategoryInfo();

    Integer selectAvailableRoomCount();
}

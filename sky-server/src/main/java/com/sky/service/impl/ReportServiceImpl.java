package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: ChuYangjie
 * @date: 2024/3/27 23:03
 * @version: 1.0
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 获取营业额统计数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = this.getDateList(begin, end);

        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            // 封装查找条件至map中
            Map map = new HashMap();
            map.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            map.put("end", LocalDateTime.of(date, LocalTime.MAX));
            map.put("status", Orders.COMPLETED);
            // 查找date这一天、状态为已完成的订单总营业额
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户数量统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        // 封装每天新用户
        for (LocalDate date : dateList) {
            Map map = new HashMap();

            map.put("end", LocalDateTime.of(date, LocalTime.MAX));
            totalUserList.add(userMapper.countByMap(map));// 此时map只有end，动态查询出的是这天之前的总用户

            map.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            newUserList.add(userMapper.countByMap(map));// 此时map有begin，end，动态查询出今天的新增用户
        }


        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 订单数量统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 查询每天的订单总数
            orderCountList.add(this.getOrderCount(beginTime, endTime, null));
            // 查询每天的有效订单数
            validOrderCountList.add(this.getOrderCount(beginTime, endTime, Orders.COMPLETED));
        }

        Integer totalOrderCount = getOrderCount(null, null, null);
        Integer validOrderCount = getOrderCount(null, null, Orders.COMPLETED);
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量前十菜品统计
     * @param begin
     * @param end
     * @return
     */
    @Override
//    //商品名称列表，以逗号分隔，例如：鱼香肉丝,宫保鸡丁,水煮鱼
//    private String nameList;
//
//    //销量列表，以逗号分隔，例如：260,215,200
//    private String numberList;
    public SalesTop10ReportVO getSalesTop10Statistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 获取销量前十的菜品名称和销量，封装到GoodsSalesDTO
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        // 把salesTop10数组的名字拼到一起
        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    /**
     * 导出数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        // 查询数据库，获取营业数据（最近三十天）
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX));

        // 通过Apache POI将数据写入excel
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);

            XSSFSheet sheet = excel.getSheet("sheet1");
            sheet.getRow(1)
                    .getCell(1)
                    .setCellValue("时间：" + dateBegin + "至" + dateEnd);
            sheet.getRow(3)
                    .getCell(2)
                    .setCellValue(businessDataVO.getTurnover());
            sheet.getRow(3)
                    .getCell(4)
                    .setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(3)
                    .getCell(6)
                    .setCellValue(businessDataVO.getNewUsers());
            sheet.getRow(4)
                    .getCell(2)
                    .setCellValue(businessDataVO.getValidOrderCount());
            sheet.getRow(4)
                    .getCell(4)
                    .setCellValue(businessDataVO.getUnitPrice());

            // 填充30天明细
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                // 查询date这一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                XSSFRow row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 通过输出流将Excel下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            // 关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * 工具方法，调用mapper获取动态查询的订单数量
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    public Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map map = new HashMap();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }

    /**
     * 工具方法，获取begin-end间的所有日期列表
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        return dateList;
    }
}

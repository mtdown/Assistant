package com.itheima.consultant.tools;

import com.itheima.consultant.pojo.Reservation;
import com.itheima.consultant.service.ReservationService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
@Component
public class ReservationTool {
    @Autowired
    private ReservationService reservationService;
    @Tool("预约支援填报")
    public void addReservation(
            @P("考生姓名")String name,
            @P("考生性别")String gender,
            @P("考生电话")String phone,
            @P("预约沟通时间，格式是：yyyy-MM-dd'T'HH:mm")String communicationTime,
            @P("考生省份")String province,
            @P("考生分数")int estimatedScore) {
        Reservation reservation = new Reservation(null,name,gender,phone,LocalDateTime.parse(communicationTime),province,estimatedScore);
        reservationService.insert( reservation );
    }

    @Tool("根据考生手机号查询预约单")
    public Reservation findReservation(@P("考生手机号")String phone){
        return reservationService.findByPhone(phone);
    }

}

package com.itheima.consultant;

import com.itheima.consultant.pojo.Reservation;
import com.itheima.consultant.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;
    //测试添加
    @Test
    void testInsert(){
        Reservation reservation = new Reservation(23, "小明", "男", "1255", LocalDateTime.now(), "ca", 233);
        reservationService.insert(reservation);
    }
    @Test
    void testFindByPhone(){
        String phone="1255";
//        Reservation byPhone = reservationService.findByPhone(phone);
        System.out.println(reservationService.findByPhone(phone));
    }
}

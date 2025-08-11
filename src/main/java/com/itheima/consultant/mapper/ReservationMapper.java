package com.itheima.consultant.mapper;

import com.itheima.consultant.pojo.Reservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
@Mapper
public interface ReservationMapper {

    @Insert("insert into reservation(name,gender,phone,communication_time,province,estimated_score) values(#{name},#{gender},#{phone},#{communicationTime},#{province},#{estimatedScore})")
    void insert(Reservation reservation);

    @Select("select * from reservation where phone=#{phone}")
    Reservation findByPhone(String phone);

//    @Select("select * from reservation where phone=#{phone}")
//    List<Reservation> findByPhone(String phone);
}

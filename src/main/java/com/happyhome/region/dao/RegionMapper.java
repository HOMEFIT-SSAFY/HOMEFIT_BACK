package com.happyhome.region.dao;

import com.happyhome.region.dto.RegionOptionDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RegionMapper {

    List<RegionOptionDto> findSidos();

    List<RegionOptionDto> findGuguns(String sidoName);

    List<RegionOptionDto> findDongs(@Param("sidoName") String sidoName, @Param("gugunName") String gugunName);
}

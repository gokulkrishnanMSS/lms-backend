package com.inspeedia.toyotsu.lms.dto;

import com.inspeedia.toyotsu.lms.model.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeWrapper {
    @Mapping(target = "name", source = "name")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "companyName", source = "companyName")
    @Mapping(target = "photo", source = "photo")
    @Mapping(target = "mainProcess", source = "mainProcess")
    @Mapping(target = "subProcess", source = "subProcess")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "employmentPeriod", source = "employmentPeriod")
    @Mapping(target = "qualityCases", source = "qualityCases")
    @Mapping(target = "productivity", source = "productivity")
    @Mapping(target = "suitableArea", source = "suitableArea")
    @Mapping(target = "groupId", source = "group")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "id", ignore = true)
    Employee toEntity(EmployeeDTO employeeDTO);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "companyName", source = "companyName")
    @Mapping(target = "photo", source = "photo")
    @Mapping(target = "mainProcess", source = "mainProcess")
    @Mapping(target = "subProcess", source = "subProcess")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "employmentPeriod", source = "employmentPeriod")
    @Mapping(target = "qualityCases", source = "qualityCases")
    @Mapping(target = "productivity", source = "productivity")
    @Mapping(target = "suitableArea", source = "suitableArea")
    @Mapping(target = "group", source = "groupId")
    EmployeeDTO toDto(Employee employee);
}

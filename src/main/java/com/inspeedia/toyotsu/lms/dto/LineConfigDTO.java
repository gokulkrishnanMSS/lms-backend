package com.inspeedia.toyotsu.lms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LineConfigDTO {
    private Map<String, List<Integer>>  lineConfig;
}

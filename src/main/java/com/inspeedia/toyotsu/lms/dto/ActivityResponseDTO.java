package com.inspeedia.toyotsu.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ActivityResponseDTO {
    private String LineName;
    private List<Integer> listOfSupplier;

}

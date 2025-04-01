package com.inspeedia.toyotsu.lms.dto;

import lombok.Data;
import java.util.List;

@Data
public class BackScreenData {
    private List<String> suddenLeave;
    private List<String> administrator;
    private List<String> exterior;
    private List<String> sorting;
    private List<String> tac;
    private List<String> others;
}

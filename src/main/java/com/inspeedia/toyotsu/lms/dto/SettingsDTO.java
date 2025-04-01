package com.inspeedia.toyotsu.lms.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingsDTO {
    private String companyName;
    private String workerName;
    private String adminName;
    private String managerName;
    private String actualNoOfPeople;
    private String workStartTime;
    private String workEndTime;

    @Override
    public String toString() {
        return "SettingsDTO{" + "companyName='" + companyName + '\'' + ", workerName='" + workerName + '\'' + ", adminName='" + adminName + '\'' + ", managerName='" + managerName + '\'' + ", actualNoOfPeople='" + actualNoOfPeople + '\'' + ", workStartTime='" + workStartTime + '\'' + ", workEndTime='" + workEndTime + '\'' + '}';
    }


}

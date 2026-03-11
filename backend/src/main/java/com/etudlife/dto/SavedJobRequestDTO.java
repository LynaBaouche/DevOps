package com.etudlife.dto;

import com.etudlife.model.JobStatus;
import lombok.Data;

@Data
public class SavedJobRequestDTO {
    private Long compteId;
    private String externalJobId;
    private String title;
    private String company;
    private String location;
    private String applyLink;
    private JobStatus status;
}
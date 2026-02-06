package com.etudlife.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore les 50 autres champs de l'API
public class JobDTO {
    @JsonProperty("job_title")
    private String title;

    @JsonProperty("employer_name")
    private String company;

    @JsonProperty("job_city")
    private String city;

    @JsonProperty("job_apply_link")
    private String applyLink;

    @JsonProperty("job_description")
    private String description;

    @JsonProperty("job_publisher")
    private String source;

    public String getJobPublisher() {
        return source;
    }
}
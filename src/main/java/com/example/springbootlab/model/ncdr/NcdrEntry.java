package com.example.springbootlab.model.ncdr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NcdrEntry {
    private String id;
    private String title;
    private String updated;
    
    @JsonProperty("summary")
    private NcdrSummary summary;
}

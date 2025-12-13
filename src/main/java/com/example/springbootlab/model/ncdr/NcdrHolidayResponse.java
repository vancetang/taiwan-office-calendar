package com.example.springbootlab.model.ncdr;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NcdrHolidayResponse {
    private String title;
    private String updated;
    
    @JsonProperty("entry")
    private List<NcdrEntry> entry;
}

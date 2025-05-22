package com.ducbn.busService.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyDTO {

    @JsonProperty("name")
    @NotBlank(message = "Company name must not be blank")
    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String name;

    @JsonProperty("description")
    @Size(max = 255, message = "Contact info must not exceed 255 characters")
    private String description;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("address")
    private String address;
}

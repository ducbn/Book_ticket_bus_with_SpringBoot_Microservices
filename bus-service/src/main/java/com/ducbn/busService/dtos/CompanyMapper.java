package com.ducbn.busService.dtos;


import com.ducbn.busService.models.Company;
import com.ducbn.busService.responses.CompanyResponse;

public class CompanyMapper {

    public static CompanyResponse toDto(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .phone(company.getPhone())
                .email(company.getEmail())
                .address(company.getAddress())
                .build();
    }

    public static Company toEntity(CompanyDTO request) {
        return Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .build();
    }
}
package com.ducbn.busService.services;

import com.ducbn.busService.dtos.CompanyDTO;
import com.ducbn.busService.models.Company;
import com.ducbn.busService.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService implements ICompanyService {
    private final CompanyRepository companyRepository;

    @Override
    public Company createCompany(CompanyDTO companyDTO) {
        Company company = Company.builder()
                .name(companyDTO.getName())
                .description(companyDTO.getDescription())
                .email(companyDTO.getEmail())
                .address(companyDTO.getAddress())
                .phone(companyDTO.getPhone())
                .build();

        return companyRepository.save(company);
    }

    @Override
    public List<Company> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return companies;
    }

    @Override
    public Company getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
        return company;
    }

    @Override
    public Company updateCompany(Long id, CompanyDTO companyDTO) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        company.setName(companyDTO.getName());
        company.setDescription(companyDTO.getDescription());
        company.setEmail(companyDTO.getEmail());
        company.setAddress(companyDTO.getAddress());
        company.setPhone(companyDTO.getPhone());

        Company updated = companyRepository.save(company);
        return updated;
    }

    @Override
    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }

}

package com.seera.lumi.partner.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "branch-service", url = "${api.branch.baseUrl}")
public interface BranchClient {

    // TODO: Define branch service endpoints
    // Example: @GetMapping("/api/v1/branches")
    // List<BranchDto> getBranches();
}

package com.seera.lumi.partner.gateway.client;

import com.seera.lumi.partner.gateway.client.response.BranchDetailResponse;
import com.seera.lumi.partner.gateway.client.response.BranchListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "branch-service", url = "${api.branch.baseUrl}")
public interface BranchClient {

    @GetMapping("/branch/list")
    BranchListResponse getBranches();

    @GetMapping("/branch/{id}")
    BranchDetailResponse getBranchById(@PathVariable("id") Long id);
}

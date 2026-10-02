package com.b2b.leadpipeline2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/reps")
public class SalesRepController {

    @Autowired
    private SalesRepRepository salesRepRepository;

    @Autowired
    private LeadRepository leadRepository;

    @GetMapping
    public List<SalesRep> getAllReps() {
        return salesRepRepository.findAll();
    }

    @PostMapping
    public SalesRep createRep(@RequestBody SalesRep rep) {
        return salesRepRepository.save(rep);
    }

    @DeleteMapping("/{id}")
    public void deleteRep(@PathVariable int id) {
        salesRepRepository.deleteById(id);
    }

    // ---------- Rep performance view (new in this version) ----------
    // shows every lead assigned to this rep, plus their total pipeline value
    @GetMapping("/{id}/performance")
    public Map<String, Object> getRepPerformance(@PathVariable int id) {
        SalesRep rep = salesRepRepository.findById(id).orElse(null);
        if (rep == null) return null;

        List<Lead> assignedLeads = leadRepository.findByRepId(id);
        double totalValue = 0;
        for (Lead l : assignedLeads) {
            totalValue += l.getDealValue();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("repId", rep.getRepId());
        result.put("name", rep.getName());
        result.put("department", rep.getDepartment());
        result.put("activeLeadCount", assignedLeads.size());
        result.put("totalPipelineValue", totalValue);
        result.put("leads", assignedLeads);
        return result;
    }
}

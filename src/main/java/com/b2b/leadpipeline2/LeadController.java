package com.b2b.leadpipeline2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/leads")
public class LeadController {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private SalesRepRepository salesRepRepository;

    // ---------- Basic CRUD ----------

    @GetMapping
    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    @GetMapping("/{id}")
    public Lead getLeadById(@PathVariable int id) {
        return leadRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Lead createLead(@RequestBody Lead lead) {
        if (lead.getStage() == null || lead.getStage().isEmpty()) {
            lead.setStage("NEW");
        }
        if (lead.getLastContactedDate() == null || lead.getLastContactedDate().isEmpty()) {
            lead.setLastContactedDate(LocalDate.now().toString());
        }
        if (lead.isHot() == null) {
            lead.setHot(false);
        }
        return leadRepository.save(lead);
    }

    @DeleteMapping("/{id}")
    public void deleteLead(@PathVariable int id) {
        leadRepository.deleteById(id);
    }

    // ---------- Update stage / deal value (new in this version) ----------

    @PutMapping("/{id}/stage")
    public Lead updateStage(@PathVariable int id, @RequestBody Map<String, String> body) {
        Lead lead = leadRepository.findById(id).orElse(null);
        if (lead == null) return null;

        String newStage = body.get("stage");
        List<String> validStages = List.of("NEW", "CONTACTED", "QUALIFIED", "WON", "LOST");
        if (newStage != null && validStages.contains(newStage.toUpperCase())) {
            lead.setStage(newStage.toUpperCase());
            leadRepository.save(lead);
        }
        return lead;
    }

    @PutMapping("/{id}/value")
    public Lead updateDealValue(@PathVariable int id, @RequestBody Map<String, Double> body) {
        Lead lead = leadRepository.findById(id).orElse(null);
        if (lead == null) return null;

        Double newValue = body.get("dealValue");
        if (newValue != null && newValue >= 0) {
            lead.setDealValue(newValue);
            leadRepository.save(lead);
        }
        return lead;
    }

    // ---------- Assign lead to rep (new in this version) ----------

    @PutMapping("/{id}/assign/{repId}")
    public Lead assignToRep(@PathVariable int id, @PathVariable int repId) {
        Lead lead = leadRepository.findById(id).orElse(null);
        SalesRep rep = salesRepRepository.findById(repId).orElse(null);
        if (lead == null || rep == null) return null;

        lead.setRepId(repId);
        return leadRepository.save(lead);
    }

    @PutMapping("/{id}/unassign")
    public Lead unassignFromRep(@PathVariable int id) {
        Lead lead = leadRepository.findById(id).orElse(null);
        if (lead == null) return null;

        lead.setRepId(null);
        return leadRepository.save(lead);
    }

    // ---------- Priority Score ----------

    @GetMapping("/priority")
    public List<Map<String, Object>> getLeadsByPriority() {
        List<Lead> leads = leadRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Lead l : leads) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("leadId", l.getLeadId());
            item.put("companyName", l.getCompanyName());
            item.put("stage", l.getStage());
            item.put("hot", l.isHot());
            item.put("dealValue", l.getDealValue());
            item.put("priorityScore", calculatePriorityScore(l));
            result.add(item);
        }

        result.sort((a, b) -> Double.compare(
                (double) b.get("priorityScore"), (double) a.get("priorityScore")));
        return result;
    }

    // ---------- Analytics Summary ----------

    @GetMapping("/analytics")
    public Map<String, Object> getAnalyticsSummary() {
        List<Lead> leads = leadRepository.findAll();

        double openPipelineValue = 0;
        double wonValue = 0;
        int wonCount = 0;
        int lostCount = 0;

        Map<String, Integer> stageCounts = new LinkedHashMap<>();
        stageCounts.put("NEW", 0);
        stageCounts.put("CONTACTED", 0);
        stageCounts.put("QUALIFIED", 0);
        stageCounts.put("WON", 0);
        stageCounts.put("LOST", 0);

        for (Lead l : leads) {
            String stage = l.getStage() == null ? "NEW" : l.getStage();
            stageCounts.put(stage, stageCounts.getOrDefault(stage, 0) + 1);

            if (stage.equals("WON")) {
                wonCount++;
                wonValue += l.getDealValue();
            } else if (stage.equals("LOST")) {
                lostCount++;
            } else {
                openPipelineValue += l.getDealValue();
            }
        }

        int closedDeals = wonCount + lostCount;
        double winRate = closedDeals == 0 ? 0.0 : (wonCount * 100.0) / closedDeals;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalLeads", leads.size());
        summary.put("openPipelineValue", openPipelineValue);
        summary.put("wonValue", wonValue);
        summary.put("winRate", Math.round(winRate * 10.0) / 10.0);
        summary.put("wonCount", wonCount);
        summary.put("closedDeals", closedDeals);
        summary.put("stageCounts", stageCounts);
        return summary;
    }

    // ---------- Overdue Follow-ups ----------

    @GetMapping("/overdue")
    public List<Map<String, Object>> getOverdueLeads(@RequestParam(defaultValue = "3") int days) {
        List<Lead> leads = leadRepository.findAll();
        List<Map<String, Object>> overdue = new ArrayList<>();

        for (Lead l : leads) {
            String stage = l.getStage() == null ? "NEW" : l.getStage();
            boolean isClosed = stage.equals("WON") || stage.equals("LOST");
            long daysSince = daysSinceContact(l.getLastContactedDate());

            if (!isClosed && daysSince > days) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("leadId", l.getLeadId());
                item.put("companyName", l.getCompanyName());
                item.put("stage", stage);
                item.put("daysSinceContact", daysSince);
                overdue.add(item);
            }
        }
        return overdue;
    }

    @PutMapping("/{id}/contact")
    public Lead markContacted(@PathVariable int id) {
        Lead lead = leadRepository.findById(id).orElse(null);
        if (lead != null) {
            lead.setLastContactedDate(LocalDate.now().toString());
            leadRepository.save(lead);
        }
        return lead;
    }

    // ---------- Helpers ----------

    private long daysSinceContact(String lastContactedDate) {
        if (lastContactedDate == null || lastContactedDate.isEmpty()) {
            return 0;
        }
        try {
            LocalDate last = LocalDate.parse(lastContactedDate);
            return ChronoUnit.DAYS.between(last, LocalDate.now());
        } catch (Exception e) {
            return 0;
        }
    }

    private double calculatePriorityScore(Lead l) {
        double score = l.getDealValue() / 1000.0;

        if (Boolean.TRUE.equals(l.isHot())) {
            score += 50;
        }

        String stage = l.getStage() == null ? "NEW" : l.getStage();
        switch (stage) {
            case "QUALIFIED": score += 30; break;
            case "CONTACTED": score += 15; break;
            case "NEW": score += 5; break;
            default: break;
        }

        if (!stage.equals("WON") && !stage.equals("LOST")) {
            score += daysSinceContact(l.getLastContactedDate()) * 2;
        }

        return Math.round(score * 10.0) / 10.0;
    }
}

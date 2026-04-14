package com.example.springai.agents.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.springai.agents.service.AgentPatternsService;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin(origins = "*")
public class AgentPatternsController {

    private final AgentPatternsService agentService;

    public AgentPatternsController(AgentPatternsService agentService) {
        this.agentService = agentService;
    }

    // --- Request / Response DTOs ---

    public record ChainRequest(String input) {}
    public record ParallelRequest(String prompt, List<String> inputs) {}
    public record RoutingRequest(String input) {}
    public record OrchestratorRequest(String task) {}
    public record EvaluatorRequest(String task) {}
    public record PatternResponse(String result, long timestamp) {
        public PatternResponse(String result) {
            this(result, System.currentTimeMillis());
        }
    }

    // --- Endpoints ---

    @PostMapping("/chain")
    public ResponseEntity<PatternResponse> chain(@RequestBody ChainRequest request) {
        String result = agentService.runChainWorkflow(request.input());
        return ResponseEntity.ok(new PatternResponse(result));
    }

    @PostMapping("/parallelization")
    public ResponseEntity<PatternResponse> parallelization(@RequestBody ParallelRequest request) {
        String result = agentService.runParallelizationWorkflow(request.prompt(), request.inputs());
        return ResponseEntity.ok(new PatternResponse(result));
    }

    @PostMapping("/routing")
    public ResponseEntity<PatternResponse> routing(@RequestBody RoutingRequest request) {
        String result = agentService.runRoutingWorkflow(request.input());
        return ResponseEntity.ok(new PatternResponse(result));
    }

    @PostMapping("/orchestrator")
    public ResponseEntity<PatternResponse> orchestrator(@RequestBody OrchestratorRequest request) {
        String result = agentService.runOrchestratorWorkersWorkflow(request.task());
        return ResponseEntity.ok(new PatternResponse(result));
    }

    @PostMapping("/evaluator")
    public ResponseEntity<PatternResponse> evaluator(@RequestBody EvaluatorRequest request) {
        String result = agentService.runEvaluatorOptimizerWorkflow(request.task());
        return ResponseEntity.ok(new PatternResponse(result));
    }
}

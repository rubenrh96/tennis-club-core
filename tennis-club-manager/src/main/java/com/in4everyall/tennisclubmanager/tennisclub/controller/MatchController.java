package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.*;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.service.MatchService;
import com.in4everyall.tennisclubmanager.tennisclub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/my")
    public List<MatchResponse> myMatches(
            @RequestParam String license,
            @RequestParam String phaseCode
    ) {
        return matchService.findMyMatches(license, phaseCode);
    }

    @GetMapping("/my-phases")
    public List<String> myPhases(@RequestParam String license) {
        return matchService.getPhaseCodesForPlayer(license);
    }

    @GetMapping("/my/all")
    public List<MatchResponse> myMatchesAll(@RequestParam String license) {
        return matchService.findAllMyMatches(license);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsMatch(
            @RequestParam String phaseCode,
            @RequestParam String p1,
            @RequestParam String p2
    ) {
        boolean exists = matchService.existsByPhaseAndPlayers(phaseCode, p1, p2);
        return ResponseEntity.ok(exists);
    }

    @PostMapping
    public ResponseEntity<MatchResponse> addMatch(@RequestBody MatchRequest request) {
        MatchResponse response = matchService.addMatch(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<MatchResponse> confirmMatch(
            @PathVariable UUID id,
            @RequestParam String confirmerLicense
    ) {
        MatchResponse response = matchService.confirmMatch(id, confirmerLicense);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/reject")
    public ResponseEntity<MatchResponse> rejectMatch(
    @PathVariable UUID id,
    @RequestParam String rejecterLicense
    ) {
	    MatchResponse response = matchService.rejectMatch(id, rejecterLicense);
	    return ResponseEntity.ok(response);
    }


}

package com.cardmanager.controller;

import com.cardmanager.common.Result;
import com.cardmanager.dto.VerifyCardRequest;
import com.cardmanager.dto.CardQueryRequest;
import com.cardmanager.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/verify")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VerifyController {

    private final CardService cardService;

    @PostMapping("/use")
    public Result<Void> verifyCard(@Valid @RequestBody VerifyCardRequest request) {
        try {
            cardService.verifyCard(request);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result<org.springframework.data.domain.Page<com.cardmanager.entity.CardInfo>> getVerificationHistory(
            CardQueryRequest request) {
        request.setStatus(1); // Force query for used/verified cards
        // If sorting is not specified, default to useTime descending for history
        if (request.getSortBy() == null || request.getSortBy().isEmpty()) {
            request.setSortBy("useTime");
        }
        return Result.success(cardService.queryCards(request));
    }
}

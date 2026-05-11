package com.cardmanager.controller;

import com.cardmanager.common.Result;
import com.cardmanager.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicController {

    private final CardService cardService;

    @GetMapping("/query")
    public Result<Map<String, Object>> queryCardStatus(
            @RequestParam String cardNumber,
            @RequestParam String cardPassword) {
        try {
            return Result.success(cardService.queryCardStatus(cardNumber, cardPassword));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

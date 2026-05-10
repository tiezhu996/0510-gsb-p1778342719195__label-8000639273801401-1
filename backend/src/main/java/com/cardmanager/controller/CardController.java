package com.cardmanager.controller;

import com.cardmanager.common.Result;
import com.cardmanager.dto.CardQueryRequest;
import com.cardmanager.dto.GenerateCardRequest;
import com.cardmanager.entity.CardBatch;
import com.cardmanager.entity.CardInfo;
import com.cardmanager.entity.CardInfo;
import com.cardmanager.service.CardService;
import com.cardmanager.repository.CardInfoRepository;
import com.cardmanager.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CardController {

    private final CardService cardService;
    private final CardInfoRepository cardInfoRepository;
    private final SysUserRepository sysUserRepository;

    @PostMapping("/generate")
    public Result<Map<String, Object>> generateCards(@Valid @RequestBody GenerateCardRequest request) {
        try {
            return Result.success(cardService.generateCards(request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Long>> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalCards", cardInfoRepository.count());
        stats.put("usedCards", cardInfoRepository.countByStatus(1));
        stats.put("unusedCards", cardInfoRepository.countByStatus(0));
        stats.put("recycledCards", cardInfoRepository.countByStatus(2));
        stats.put("totalUsers", sysUserRepository.count());
        return Result.success(stats);
    }

    @GetMapping("/list")
    public Result<Page<CardInfo>> queryCards(CardQueryRequest request) {
        return Result.success(cardService.queryCards(request));
    }

    @GetMapping("/batches")
    public Result<List<CardBatch>> getAllBatches() {
        return Result.success(cardService.getAllBatches());
    }

    @PutMapping("/recycle/batch/{batchNumber}")
    public Result<Integer> recycleByBatch(@PathVariable String batchNumber) {
        return Result.success(cardService.recycleByBatch(batchNumber));
    }

    @PutMapping("/recycle/{cardNumber}")
    public Result<Boolean> recycleByCardNumber(@PathVariable String cardNumber) {
        return Result.success(cardService.recycleByCardNumber(cardNumber));
    }

    @GetMapping("/export")
    public void exportCards(CardQueryRequest request, HttpServletResponse response) throws IOException {
        request.setPage(1);
        request.setSize(100000);
        Page<CardInfo> cards = cardService.queryCards(request);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("卡密列表");
            Row headerRow = sheet.createRow(0);
            String[] headers = { "卡号", "密码", "批次号", "状态", "生成时间", "核销时间", "操作员" };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (CardInfo card : cards.getContent()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(card.getCardNumber());
                row.createCell(1).setCellValue(card.getCardPassword());
                row.createCell(2).setCellValue(card.getBatchNumber());
                row.createCell(3).setCellValue(card.getStatus() == 0 ? "未使用" : (card.getStatus() == 1 ? "已核销" : "已回收"));
                row.createCell(4).setCellValue(card.getCreateTime().format(formatter));
                row.createCell(5).setCellValue(card.getUseTime() != null ? card.getUseTime().format(formatter) : "");
                row.createCell(6).setCellValue(card.getOperator() != null ? card.getOperator() : "");
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=cards.xlsx");
            workbook.write(response.getOutputStream());
        }
    }
}

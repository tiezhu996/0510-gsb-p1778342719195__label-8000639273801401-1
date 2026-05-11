package com.cardmanager.service;

import com.cardmanager.dto.GenerateCardRequest;
import com.cardmanager.dto.CardQueryRequest;
import com.cardmanager.dto.VerifyCardRequest;
import com.cardmanager.entity.CardBatch;
import com.cardmanager.entity.CardInfo;
import com.cardmanager.repository.CardBatchRepository;
import com.cardmanager.repository.CardInfoRepository;
import com.cardmanager.util.CardNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardInfoRepository cardInfoRepository;
    private final CardBatchRepository cardBatchRepository;
    private final CardNumberGenerator cardNumberGenerator;

    private static final DateTimeFormatter BATCH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateCards(GenerateCardRequest request) {
        String batchNumber = LocalDateTime.now().format(BATCH_FORMATTER);

        CardBatch batch = new CardBatch();
        batch.setBatchNumber(batchNumber);
        batch.setTotalCount(request.getCount());
        batch.setUsedCount(0);
        batch.setOperator(request.getOperator());
        cardBatchRepository.save(batch);

        List<CardInfo> cards = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < request.getCount(); i++) {
            try {
                CardInfo card = new CardInfo();
                String cardNumber;
                int retryCount = 0;
                do {
                    cardNumber = cardNumberGenerator.generateCardNumber();
                    retryCount++;
                    if (retryCount > 10)
                        throw new RuntimeException("生成卡号重试次数超限");
                } while (cardInfoRepository.existsByCardNumber(cardNumber));

                card.setCardNumber(cardNumber);
                card.setCardPassword(cardNumberGenerator.generatePassword());
                card.setBatchNumber(batchNumber);
                card.setStatus(0);
                card.setOperator(request.getOperator());

                cards.add(card);
                successCount++;

                if (cards.size() >= 1000) {
                    cardInfoRepository.saveAll(cards);
                    cards.clear();
                }
            } catch (Exception e) {
                log.error("生成单张卡密失败: {}", e.getMessage());
            }
        }

        if (!cards.isEmpty()) {
            cardInfoRepository.saveAll(cards);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("batchNumber", batchNumber);
        result.put("totalCount", successCount);
        return result;
    }

    public Page<CardInfo> queryCards(CardQueryRequest request) {
        Specification<CardInfo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getCardNumber() != null && !request.getCardNumber().isEmpty()) {
                predicates.add(cb.like(root.get("cardNumber"), "%" + request.getCardNumber() + "%"));
            }
            if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
                predicates.add(cb.equal(root.get("batchNumber"), request.getBatchNumber()));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // Added verification time range filtering
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (request.getUseTimeStart() != null && !request.getUseTimeStart().isEmpty()) {
                LocalDateTime start = LocalDateTime.parse(request.getUseTimeStart(), df);
                predicates.add(cb.greaterThanOrEqualTo(root.get("useTime"), start));
            }
            if (request.getUseTimeEnd() != null && !request.getUseTimeEnd().isEmpty()) {
                LocalDateTime end = LocalDateTime.parse(request.getUseTimeEnd(), df);
                predicates.add(cb.lessThanOrEqualTo(root.get("useTime"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        String sortField = "createTime";
        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            sortField = request.getSortBy();
        }

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(),
                Sort.by(Sort.Direction.DESC, sortField));
        return cardInfoRepository.findAll(spec, pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public int recycleByBatch(String batchNumber) {
        return cardInfoRepository.recycleByBatchNumber(batchNumber);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean recycleByCardNumber(String cardNumber) {
        return cardInfoRepository.recycleByCardNumber(cardNumber) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public void verifyCard(VerifyCardRequest request) {
        CardInfo card = cardInfoRepository
                .findByCardNumberAndCardPassword(request.getCardNumber(), request.getCardPassword())
                .orElseThrow(() -> new RuntimeException("卡号或密码错误"));

        if (card.getStatus() != 0) {
            throw new RuntimeException("卡密状态无效");
        }

        card.setStatus(1);
        card.setUseTime(LocalDateTime.now());
        card.setOperator(request.getOperator());
        cardInfoRepository.save(card);

        cardBatchRepository.findById(card.getBatchNumber()).ifPresent(batch -> {
            batch.setUsedCount(batch.getUsedCount() + 1);
            cardBatchRepository.save(batch);
        });
    }

    public Map<String, Object> queryCardStatus(String cardNumber, String cardPassword) {
        CardInfo card = cardInfoRepository.findByCardNumberAndCardPassword(cardNumber, cardPassword)
                .orElseThrow(() -> new RuntimeException("卡号或密码错误"));

        Map<String, Object> result = new HashMap<>();
        result.put("cardNumber", card.getCardNumber());
        result.put("status", card.getStatus());
        result.put("createTime", card.getCreateTime());
        result.put("useTime", card.getUseTime());
        return result;
    }

    public List<CardBatch> getAllBatches() {
        return cardBatchRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime"));
    }
}

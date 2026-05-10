package com.cardmanager.entity;

import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "card_info", indexes = {
        @Index(name = "idx_card_number", columnList = "card_number", unique = true),
        @Index(name = "idx_batch_number", columnList = "batch_number"),
        @Index(name = "idx_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE card_info SET is_deleted = 1 WHERE id = ?")
@Where(clause = "is_deleted = 0")
public class CardInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true, length = 9)
    private String cardNumber;

    @Column(name = "card_password", nullable = false, length = 6)
    private String cardPassword;

    @Column(name = "batch_number", nullable = false, length = 14)
    private String batchNumber;

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0-未使用, 1-已核销, 2-已回收

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "use_time")
    private LocalDateTime useTime;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}

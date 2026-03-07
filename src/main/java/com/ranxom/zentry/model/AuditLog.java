package com.ranxom.zentry.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "actor_id")
    private Long actorId; // The ID of the Admin/User who performed the action

    @Column(name = "action_type")
    private String actionType; // e.g., "ROLE_ASSIGNED", "ACCOUNT_LOCKED"

    @Column(name = "target_entity_id")
    private Long targetEntityId; // The ID of the user being modified

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details; // Metadata about the change

    @Column(name = "ip_address")
    private String ipAddress;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
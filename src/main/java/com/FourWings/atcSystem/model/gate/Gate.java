package com.FourWings.atcSystem.model.gate;

import com.FourWings.atcSystem.model.terminal.Terminal;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "gates")
public class Gate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gate_id")
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", referencedColumnName = "terminal_id")
    private Terminal terminal;

    @Column(name = "code", length = 20, nullable = false)
    private String code;

    @Convert(converter = GateStatusConverter.class)
    @Column(name = "status")
    private GateStatus status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "Gate{" +
                "id=" + id +
                ", terminal=" + terminal +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
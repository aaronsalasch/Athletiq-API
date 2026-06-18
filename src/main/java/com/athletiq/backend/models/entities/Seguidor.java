package com.athletiq.backend.models.entities;

import com.athletiq.backend.models.keys.SeguidorKey;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Entity
@Table(name = "seguidor")
@Check(constraints = "id_seguidor <> id_seguido")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seguidor {

    @EmbeddedId
    private SeguidorKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idSeguidor")
    @JoinColumn(name = "id_seguidor", nullable = false)
    private Usuario seguidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idSeguido")
    @JoinColumn(name = "id_seguido", nullable = false)
    private Usuario seguido;

    @NotNull
    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDateTime fechaSeguimiento;

    @PrePersist
    @PreUpdate
    protected void checkIntegrity() {
        if (id != null && id.getIdSeguidor() != null && id.getIdSeguidor().equals(id.getIdSeguido())) {
            throw new IllegalStateException("Un usuario no puede seguirse a sí mismo");
        }
        if (this.fechaSeguimiento == null) {
            this.fechaSeguimiento = LocalDateTime.now();
        }
    }
}

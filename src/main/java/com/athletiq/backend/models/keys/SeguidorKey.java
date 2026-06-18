package com.athletiq.backend.models.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeguidorKey implements Serializable {

    @Column(name = "id_seguidor", nullable = false)
    private UUID idSeguidor;

    @Column(name = "id_seguido", nullable = false)
    private UUID idSeguido;
}

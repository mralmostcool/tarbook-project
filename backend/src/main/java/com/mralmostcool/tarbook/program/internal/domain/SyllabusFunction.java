package com.mralmostcool.tarbook.program.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "syllabus_functions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusFunction {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private StcwProgram program;

    @Column(name = "function_code", nullable = false, length = 50)
    private String functionCode;

    @Column(nullable = false)
    private String title;

    @Column(name = "stcw_code", nullable = false, length = 50)
    private String stcwCode;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}

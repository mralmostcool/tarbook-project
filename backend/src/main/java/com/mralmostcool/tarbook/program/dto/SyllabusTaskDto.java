package com.mralmostcool.tarbook.program.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusTaskDto {
    private UUID id;
    private UUID functionId;
    private String taskCode;
    private String title;
    private String description;
    private Integer requiredSignOffs;
    private Integer minimumWatchkeepingHours;
    private boolean unlocked;
}

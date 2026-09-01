package com.devcaiqueoliveira.nexus_api.dto;

public record SubjectProgressResponse(
        Integer targetHours,
        Double totalHoursStudied,
        Double remainingHours,
        Double completionPercentage
) {
}

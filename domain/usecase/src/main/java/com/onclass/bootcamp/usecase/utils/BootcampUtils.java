package com.onclass.bootcamp.usecase.utils;

import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import com.onclass.bootcamp.model.bootcamp.BootcampWithCapabilities;
import com.onclass.bootcamp.model.capability.CapabilitySummary;
import com.onclass.bootcamp.model.technology.TechnologySummary;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.List;

@UtilityClass
public class BootcampUtils {
    public static boolean isValidCapabilitiesCount(List<Long> capabilityIds, int min, int max) {
        return capabilityIds != null && capabilityIds.size() >= min && capabilityIds.size() <= max;
    }

    public static boolean hasNoRepeatedCapabilities(List<Long> capabilityIds) {
        return capabilityIds != null && capabilityIds.stream().distinct().count() == capabilityIds.size();
    }

    public static CapabilitySummary buildCapabilitySummaryWithTechnologies(CapabilitySummary capability, List<TechnologySummary> technologies) {
        return CapabilitySummary.builder()
                .id(capability.getId())
                .name(capability.getName())
                .technologies(technologies)
                .build();
    }

    public static BootcampWithCapabilities buildBootcampWithCapabilities(Bootcamp bootcamp, List<CapabilitySummary> capabilities) {
        return BootcampWithCapabilities.builder()
                .id(bootcamp.getId())
                .name(bootcamp.getName())
                .description(bootcamp.getDescription())
                .releaseDate(bootcamp.getReleaseDate())
                .duration(bootcamp.getDuration())
                .capabilities(capabilities)
                .build();
    }
}

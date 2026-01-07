package com.onclass.bootcamp.usecase.utils;

import lombok.experimental.UtilityClass;
import java.util.List;

@UtilityClass
public class BootcampUtils {
    public static boolean isValidCapabilitiesCount(List<Long> capabilityIds, int min, int max) {
        return capabilityIds != null && capabilityIds.size() >= min && capabilityIds.size() <= max;
    }

    public static boolean hasNoRepeatedCapabilities(List<Long> capabilityIds) {
        return capabilityIds != null && capabilityIds.stream().distinct().count() == capabilityIds.size();
    }
}


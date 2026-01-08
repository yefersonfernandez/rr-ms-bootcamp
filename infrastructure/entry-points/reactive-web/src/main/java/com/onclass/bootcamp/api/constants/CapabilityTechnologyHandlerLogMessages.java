package com.onclass.bootcamp.api.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CapabilityTechnologyHandlerLogMessages {
    public static final String RECEIVED_ASSOCIATION_REQUEST = "[HANDLER] Received capability-technology association request: {}";
    public static final String ASSOCIATION_COMPLETED = "[HANDLER] Association completed for capabilityId={}, technologyIds={}";
    public static final String RECEIVED_TECHS_BY_CAPABILITY_REQUEST = "[HANDLER] Received technologies-capabilityId request: {}";
    public static final String TECHS_BY_CAPABILITY_RESPONSE = "[HANDLER] Response for capabilityId {}: {}";
    public static final String SUCCESS_CLEANUP = "[HANDLER] Success cleanup: {}";
    public static final String ERROR_CLEANUP = "[HANDLER] Error: {}";
}

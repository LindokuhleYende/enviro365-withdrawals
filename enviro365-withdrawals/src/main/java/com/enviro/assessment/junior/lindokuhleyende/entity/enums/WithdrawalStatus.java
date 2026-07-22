package com.enviro.assessment.junior.lindokuhleyende.entity.enums;

/**
 * Lifecycle status of a withdrawal notice.
 * A notice is only ever persisted once it has passed all business-rule validation,
 * so PENDING represents "accepted and awaiting back-office processing".
 */
public enum WithdrawalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

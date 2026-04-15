package com.company.module.esgindicator.entity;

/**
 * 데이터 입력 상태
 */
public enum InputStatus {
    NOT_STARTED("미입력"),
    IN_PROGRESS("입력중"),
    SUBMITTED("제출완료"),
    CONFIRMED("최종확정");

    private final String label;

    InputStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}

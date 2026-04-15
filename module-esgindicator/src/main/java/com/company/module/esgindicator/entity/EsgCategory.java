package com.company.module.esgindicator.entity;

/**
 * ESG 구분 (환경/사회/지배구조)
 */
public enum EsgCategory {
    E("환경",   "Environment"),
    S("사회",   "Social"),
    G("지배구조", "Governance");

    private final String korName;
    private final String engName;

    EsgCategory(String korName, String engName) {
        this.korName = korName;
        this.engName = engName;
    }

    public String getKorName() { return korName; }
    public String getEngName() { return engName; }
}

package com.company.module.esgindicator.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지표값 최종 확정 요청
 */
@Getter
@NoArgsConstructor
public class ValueConfirmRequest {

    /** 확정 비고 (선택) */
    private String confirmRemark;
}

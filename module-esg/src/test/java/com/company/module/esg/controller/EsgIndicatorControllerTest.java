package com.company.module.esg.controller;

import com.company.module.esg.config.TestSecurityConfig;
import com.company.module.esg.dto.response.EsgIndicatorResponse;
import com.company.module.esg.entity.EsgCategory;
import com.company.module.esg.service.EsgIndicatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EsgIndicatorController 슬라이스 테스트
 */
@WebMvcTest(EsgIndicatorController.class)
@Import(TestSecurityConfig.class)
@DisplayName("EsgIndicatorController 테스트")
class EsgIndicatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EsgIndicatorService indicatorService;

    @Test
    @DisplayName("GET /esg-api/indicators → 200 OK")
    @WithMockUser(roles = "USER")
    void getIndicators_success() throws Exception {
        // given
        EsgIndicatorResponse response = buildResponse(1L, "E_GHG_SCOPE1", EsgCategory.E);
        given(indicatorService.getActiveIndicators()).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/esg-api/indicators")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].indicatorCode").value("E_GHG_SCOPE1"))
                .andExpect(jsonPath("$.data[0].categoryName").value("환경"))
                .andDo(print());
    }

    @Test
    @DisplayName("GET /esg-api/indicators?category=E → 200 OK")
    @WithMockUser(roles = "USER")
    void getIndicatorsByCategory_success() throws Exception {
        // given
        EsgIndicatorResponse response = buildResponse(1L, "E_GHG_SCOPE1", EsgCategory.E);
        given(indicatorService.getIndicatorsByCategory(EsgCategory.E)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/esg-api/indicators")
                        .param("category", "E")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("GET /esg-api/indicators/{id} → 200 OK")
    @WithMockUser(roles = "USER")
    void getIndicator_success() throws Exception {
        // given
        given(indicatorService.getIndicator(1L))
                .willReturn(buildResponse(1L, "E_GHG_SCOPE1", EsgCategory.E));

        // when & then
        mockMvc.perform(get("/esg-api/indicators/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.indicatorId").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("비인증 요청 → 401 or 403 (Spring Security 기본 응답)")
    void getIndicators_unauthorized() throws Exception {
        // Spring Security 기본 동작: 미인증 요청은 401 또는 403 반환
        mockMvc.perform(get("/esg-api/indicators"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status)
                            .as("미인증 요청은 401 또는 403 이어야 합니다.")
                            .isIn(401, 403);
                })
                .andDo(print());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private EsgIndicatorResponse buildResponse(Long id, String code, EsgCategory category) {
        return EsgIndicatorResponse.builder()
                .indicatorId(id)
                .category(category)
                .categoryName(category.getKorName())
                .indicatorCode(code)
                .indicatorName("테스트 지표")
                .unit("tCO2eq")
                .activeYn(true)
                .build();
    }
}

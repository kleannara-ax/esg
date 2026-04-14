package com.company.module.esg.service;

import com.company.exception.BusinessException;
import com.company.module.esg.dto.request.EsgDataEntryRequest;
import com.company.module.esg.dto.response.EsgDataEntryResponse;
import com.company.module.esg.entity.EntryStatus;
import com.company.module.esg.entity.EsgCategory;
import com.company.module.esg.entity.EsgDataEntry;
import com.company.module.esg.entity.EsgIndicator;
import com.company.module.esg.repository.EsgDataEntryRepository;
import com.company.module.esg.repository.EsgIndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EsgDataEntryService 단위 테스트")
class EsgDataEntryServiceTest {

    @InjectMocks
    private EsgDataEntryService dataEntryService;

    @Mock
    private EsgDataEntryRepository dataEntryRepository;

    @Mock
    private EsgIndicatorRepository indicatorRepository;

    private EsgIndicator sampleIndicator;
    private EsgDataEntry draftEntry;
    private EsgDataEntry submittedEntry;

    @BeforeEach
    void setUp() {
        sampleIndicator = EsgIndicator.builder()
                .category(EsgCategory.E)
                .indicatorCode("E_GHG_SCOPE1")
                .indicatorName("온실가스 배출량 Scope 1")
                .unit("tCO2eq")
                .activeYn(true)
                .build();

        draftEntry = EsgDataEntry.builder()
                .indicator(sampleIndicator)
                .targetYear(2024)
                .targetMonth(1)
                .actualValue(new BigDecimal("1250.5"))
                .targetValue(new BigDecimal("1200.0"))
                .entryStatus(EntryStatus.DRAFT)
                .build();

        submittedEntry = EsgDataEntry.builder()
                .indicator(sampleIndicator)
                .targetYear(2024)
                .targetMonth(2)
                .actualValue(new BigDecimal("1100.0"))
                .targetValue(new BigDecimal("1200.0"))
                .entryStatus(EntryStatus.SUBMITTED)
                .build();
    }

    @Nested
    @DisplayName("데이터 입력")
    class CreateDataEntry {

        @Test
        @DisplayName("정상 입력 성공")
        void create_success() {
            // given
            EsgDataEntryRequest request = buildRequest(1L, 2024, 3);
            given(indicatorRepository.findById(1L)).willReturn(Optional.of(sampleIndicator));
            given(dataEntryRepository.findByIndicator_IndicatorIdAndTargetYearAndTargetMonth(
                    1L, 2024, 3)).willReturn(Optional.empty());
            given(dataEntryRepository.save(any(EsgDataEntry.class))).willReturn(draftEntry);

            // when
            EsgDataEntryResponse response = dataEntryService.createDataEntry(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEntryStatus()).isEqualTo(EntryStatus.DRAFT);
        }

        @Test
        @DisplayName("중복 입력 시 예외 발생")
        void create_duplicate_throws() {
            // given
            EsgDataEntryRequest request = buildRequest(1L, 2024, 1);
            given(indicatorRepository.findById(1L)).willReturn(Optional.of(sampleIndicator));
            given(dataEntryRepository.findByIndicator_IndicatorIdAndTargetYearAndTargetMonth(
                    1L, 2024, 1)).willReturn(Optional.of(draftEntry));

            // when & then
            assertThatThrownBy(() -> dataEntryService.createDataEntry(request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("상태 전환")
    class StatusTransition {

        @Test
        @DisplayName("DRAFT → SUBMITTED 제출 성공")
        void submit_success() {
            // given
            given(dataEntryRepository.findById(1L)).willReturn(Optional.of(draftEntry));

            // when
            dataEntryService.submitDataEntry(1L);

            // then
            assertThat(draftEntry.getEntryStatus()).isEqualTo(EntryStatus.SUBMITTED);
        }

        @Test
        @DisplayName("SUBMITTED → APPROVED 승인 성공")
        void approve_success() {
            // given
            given(dataEntryRepository.findById(1L)).willReturn(Optional.of(submittedEntry));

            // when
            dataEntryService.approveDataEntry(1L);

            // then
            assertThat(submittedEntry.getEntryStatus()).isEqualTo(EntryStatus.APPROVED);
        }

        @Test
        @DisplayName("DRAFT 상태에서 승인 시 예외 발생")
        void approve_from_draft_throws() {
            // given
            given(dataEntryRepository.findById(1L)).willReturn(Optional.of(draftEntry));

            // when & then
            assertThatThrownBy(() -> dataEntryService.approveDataEntry(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("상태 전환이 불가합니다");
        }

        @Test
        @DisplayName("SUBMITTED → REJECTED 반려 성공")
        void reject_success() {
            // given
            given(dataEntryRepository.findById(1L)).willReturn(Optional.of(submittedEntry));

            // when
            dataEntryService.rejectDataEntry(1L, "데이터 오류");

            // then
            assertThat(submittedEntry.getEntryStatus()).isEqualTo(EntryStatus.REJECTED);
        }
    }

    @Nested
    @DisplayName("달성률 계산")
    class AchievementRate {

        @Test
        @DisplayName("달성률 정상 계산")
        void achievementRate_calculation() {
            // given: actualValue=1100, targetValue=1200 → 91.67%
            BigDecimal rate = submittedEntry.calculateAchievementRate();

            // then
            assertThat(rate).isEqualByComparingTo(new BigDecimal("91.67"));
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private EsgDataEntryRequest buildRequest(Long indicatorId, int year, int month) {
        try {
            EsgDataEntryRequest req = new EsgDataEntryRequest();
            setField(req, "indicatorId",  indicatorId);
            setField(req, "targetYear",   year);
            setField(req, "targetMonth",  month);
            setField(req, "actualValue",  new BigDecimal("1250.5"));
            setField(req, "targetValue",  new BigDecimal("1200.0"));
            return req;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}

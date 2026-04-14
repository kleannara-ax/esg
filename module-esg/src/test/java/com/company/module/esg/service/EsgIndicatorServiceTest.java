package com.company.module.esg.service;

import com.company.exception.BusinessException;
import com.company.module.esg.dto.request.EsgIndicatorCreateRequest;
import com.company.module.esg.dto.request.EsgIndicatorUpdateRequest;
import com.company.module.esg.dto.response.EsgIndicatorResponse;
import com.company.module.esg.entity.EsgCategory;
import com.company.module.esg.entity.EsgIndicator;
import com.company.module.esg.repository.EsgIndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EsgIndicatorService 단위 테스트")
class EsgIndicatorServiceTest {

    @InjectMocks
    private EsgIndicatorService indicatorService;

    @Mock
    private EsgIndicatorRepository indicatorRepository;

    private EsgIndicator sampleIndicator;

    @BeforeEach
    void setUp() {
        sampleIndicator = EsgIndicator.builder()
                .category(EsgCategory.E)
                .indicatorCode("E_GHG_SCOPE1")
                .indicatorName("온실가스 배출량 Scope 1")
                .description("직접 온실가스 배출량")
                .unit("tCO2eq")
                .activeYn(true)
                .build();
    }

    @Nested
    @DisplayName("지표 등록")
    class CreateIndicator {

        @Test
        @DisplayName("정상 등록 성공")
        void create_success() {
            // given
            EsgIndicatorCreateRequest request = createRequest("E_GHG_SCOPE1_NEW");
            given(indicatorRepository.existsByIndicatorCode(anyString())).willReturn(false);
            given(indicatorRepository.save(any(EsgIndicator.class))).willReturn(sampleIndicator);

            // when
            EsgIndicatorResponse response = indicatorService.createIndicator(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getIndicatorCode()).isEqualTo("E_GHG_SCOPE1");
            then(indicatorRepository).should(times(1)).save(any(EsgIndicator.class));
        }

        @Test
        @DisplayName("중복 코드 등록 시 예외 발생")
        void create_duplicate_code_throws() {
            // given
            EsgIndicatorCreateRequest request = createRequest("E_GHG_SCOPE1");
            given(indicatorRepository.existsByIndicatorCode("E_GHG_SCOPE1")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> indicatorService.createIndicator(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("E_GHG_SCOPE1");
        }

        private EsgIndicatorCreateRequest createRequest(String code) {
            try {
                EsgIndicatorCreateRequest req = new EsgIndicatorCreateRequest();
                var field = EsgIndicatorCreateRequest.class;
                // 리플렉션으로 필드 설정
                setField(req, "category", EsgCategory.E);
                setField(req, "indicatorCode", code);
                setField(req, "indicatorName", "테스트 지표");
                setField(req, "description", "테스트 설명");
                setField(req, "unit", "tCO2eq");
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

    @Nested
    @DisplayName("지표 조회")
    class GetIndicator {

        @Test
        @DisplayName("활성 지표 전체 조회")
        void getActiveIndicators_success() {
            // given
            given(indicatorRepository.findAllByActiveYnTrue()).willReturn(List.of(sampleIndicator));

            // when
            List<EsgIndicatorResponse> responses = indicatorService.getActiveIndicators();

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getCategoryName()).isEqualTo("환경");
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 예외 발생")
        void getIndicator_notFound_throws() {
            // given
            given(indicatorRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> indicatorService.getIndicator(999L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("지표 수정")
    class UpdateIndicator {

        @Test
        @DisplayName("지표 비활성화 성공")
        void deactivate_success() {
            // given
            given(indicatorRepository.findById(1L)).willReturn(Optional.of(sampleIndicator));

            // when
            indicatorService.deactivateIndicator(1L);

            // then
            assertThat(sampleIndicator.isActiveYn()).isFalse();
        }
    }
}

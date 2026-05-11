package com.example.coffeeshop.domain.menu.service;

import com.example.coffeeshop.common.exception.ServiceException;
import com.example.coffeeshop.domain.menu.dto.MenuDto;
import com.example.coffeeshop.domain.menu.dto.RankingDto;
import com.example.coffeeshop.domain.menu.dto.RankingMenuDto;
import com.example.coffeeshop.domain.menu.entity.Category;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.entity.MenuStatus;
import com.example.coffeeshop.domain.menu.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @InjectMocks
    private MenuService menuService;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuRankingService menuRankingService;

    private Menu menu;

    @BeforeEach
    void setUp() {
        menu = new Menu("아메리카노", Category.COFFEE, 4500L, 100);
        ReflectionTestUtils.setField(menu, "id", 1L);
        ReflectionTestUtils.setField(menu, "status", MenuStatus.AVAILABLE);
    }

    @Nested
    @DisplayName("메뉴 단건 조회")
    class GetOneTest {

        @Test
        @DisplayName("성공 - 정상 조회")
        void getOne_success() {
            given(menuRepository.findById(1L)).willReturn(Optional.of(menu));

            MenuDto result = menuService.getOne(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("아메리카노");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 메뉴")
        void getOne_notFound() {
            given(menuRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> menuService.getOne(999L))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("메뉴 목록 조회")
    class GetAllTest {

        @Test
        @DisplayName("성공 - 전체 조회")
        void getAll_success() {
            Pageable pageable = PageRequest.of(0, 5);
            MenuDto menuDto = MenuDto.from(menu);
            given(menuRepository.findMenus(null, null, pageable)).willReturn(List.of(menuDto));

            List<MenuDto> result = menuService.getAll(null, null, pageable);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("아메리카노");
        }
    }

    @Nested
    @DisplayName("인기 메뉴 Top3 조회")
    class GetTop3Test {

        @Test
        @DisplayName("성공 - 랭킹 기반 조회")
        void getTop3_success() {
            List<RankingDto> rankings = List.of(
                    new RankingDto("1", 150.0)
            );
            given(menuRankingService.findTop3Today()).willReturn(rankings);
            given(menuRepository.findById(1L)).willReturn(Optional.of(menu));

            List<RankingMenuDto> result = menuService.getTop3();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("아메리카노");
            assertThat(result.get(0).score()).isEqualTo(150L);
        }

        @Test
        @DisplayName("성공 - 랭킹 데이터 없으면 빈 리스트")
        void getTop3_empty() {
            given(menuRankingService.findTop3Today()).willReturn(List.of());

            List<RankingMenuDto> result = menuService.getTop3();

            assertThat(result).isEmpty();
        }
    }
}
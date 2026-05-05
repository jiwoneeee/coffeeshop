package com.example.coffeeshop.common.util;

import com.example.coffeeshop.domain.member.entity.Member;
import com.example.coffeeshop.domain.member.repository.MemberRepository;
import com.example.coffeeshop.domain.menu.entity.Category;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitData implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final MenuRepository menuRepository;

    @Override
    public void run(String... args) {
        if (memberRepository.count() > 0) {
            log.info("이미 데이터가 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        initMembers();
        initMenus();
        log.info("더미 데이터 초기화 완료");
    }

    private void initMembers() {
        String[] names = {"지원", "길중", "현지", "지민", "백젼", "은샘", "시은", "유민", "민선", "타코"};

        for (String name : names) {
            memberRepository.save(new Member(name));
        }
        log.info("회원 {}명 생성 완료", names.length);
    }

    private void initMenus() {
        // COFFEE - 10개
        createMenu("아메리카노", Category.COFFEE, 4_500L, null);
        createMenu("카페라떼", Category.COFFEE, 5_000L, 20);
        createMenu("바닐라라떼", Category.COFFEE, 5_500L, 10);
        createMenu("카푸치노", Category.COFFEE, 5_000L, 10);
        createMenu("카라멜마키아또", Category.COFFEE, 5_500L, 20);
        createMenu("에스프레소", Category.COFFEE, 3_500L, null);
        createMenu("콜드브루", Category.COFFEE, 5_000L, 50);
        createMenu("헤이즐넛라떼", Category.COFFEE, 5_500L, 20);
        createMenu("아인슈페너", Category.COFFEE, 6_000L, 10);
        createMenu("플랫화이트", Category.COFFEE, 5_500L, 20);

        // TEA - 10개
        createMenu("얼그레이", Category.TEA, 4_500L, 30);
        createMenu("캐모마일", Category.TEA, 4_500L, 30);
        createMenu("페퍼민트", Category.TEA, 4_500L, 20);
        createMenu("자스민", Category.TEA, 4_500L, 20);
        createMenu("히비스커스", Category.TEA, 5_000L, 10);
        createMenu("루이보스", Category.TEA, 5_000L, 20);
        createMenu("녹차", Category.TEA, 4_000L, null);
        createMenu("유자차", Category.TEA, 5_500L, null);
        createMenu("레몬티", Category.TEA, 5_000L, null);
        createMenu("밀크티", Category.TEA, 5_500L, 40);

        // JUICE - 5개
        createMenu("딸기주스", Category.JUICE, 6_000L, 10);
        createMenu("오렌지주스", Category.JUICE, 5_500L, 5);
        createMenu("망고스무디", Category.JUICE, 6_500L, 5);
        createMenu("키위주스", Category.JUICE, 6_000L, 2);
        createMenu("자몽에이드", Category.JUICE, 5_500L, 2);

        // DESSERT - 2개 (한정 수량)
        createMenu("두쫀쿠", Category.DESSERT, 4_000L, 30);
        createMenu("버터떡", Category.DESSERT, 3_500L, 20);

        log.info("메뉴 27개 생성 완료 (COFFEE 10, TEA 10, JUICE 5, DESSERT 2)");
    }

    private void createMenu(String name, Category category, Long price, Integer stock) {
        menuRepository.save(new Menu(name, category, price, stock));
    }
}

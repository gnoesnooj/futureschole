package com.futureschole.eventpipeline.infrastructure;

import com.futureschole.eventpipeline.domain.DeviceType;
import com.futureschole.eventpipeline.domain.EventLog;
import com.futureschole.eventpipeline.domain.EventType;
import com.futureschole.eventpipeline.domain.repository.EventRepository;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventGenerator implements CommandLineRunner {

    private static final int SCENARIO_COUNT = 300;

    private static final List<UUID> USER_POOL = List.of(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
            UUID.fromString("00000000-0000-0000-0000-000000000003"),
            UUID.fromString("00000000-0000-0000-0000-000000000004"),
            UUID.fromString("00000000-0000-0000-0000-000000000005")
    );

    private static final List<String> IP_POOL = List.of(
            "1.1.1.1",
            "2.2.2.2",
            "3.3.3.3",
            "4.4.4.4",
            "5.5.5.5"
    );

    private final EventRepository eventRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== EventGenerator 시작: {}개 시나리오 실행 ===", SCENARIO_COUNT);

        for (int i = 0; i < SCENARIO_COUNT; i++) {
            int scenario = new Random().nextInt(3); // 0, 1, 2
            switch (scenario) {
                case 0 -> runPurchaseSuccessScenario();
                case 1 -> runPurchaseFailScenario();
                case 2 -> runSearchScenario();
            }
        }

        log.info("=== EventGenerator 완료: 총 {}건 저장 ===", eventRepository.count());
    }

    /**
     * 시나리오 1: 강의 구매 성공 PAGE_VIEW -> PURCHASE
     */
    private void runPurchaseSuccessScenario() {
        UUID userId = randomUser();
        String session = UUID.randomUUID().toString();

        eventRepository.save(EventLog.builder()
                .userId(userId)
                .sessionId(session)
                .eventType(EventType.PAGE_VIEW)
                .ipAddress(randomIp())
                .deviceType(randomDevice())
                .build());

        eventRepository.save(EventLog.builder()
                .userId(userId)
                .sessionId(session)
                .eventType(EventType.PURCHASE)
                .ipAddress(randomIp())
                .deviceType(randomDevice())
                .build());
    }

    /**
     * 시나리오 2: 강의 구매 실패 PAGE_VIEW -> PURCHASE -> ERROR
     */
    private void runPurchaseFailScenario() {
        UUID userId = randomUser();
        String session = UUID.randomUUID().toString();

        eventRepository.save(EventLog.builder()
                .userId(userId)
                .sessionId(session)
                .eventType(EventType.PAGE_VIEW)
                .ipAddress(randomIp())
                .deviceType(randomDevice())
                .build());

        eventRepository.save(EventLog.builder()
                .userId(userId)
                .sessionId(session)
                .eventType(EventType.PURCHASE)
                .ipAddress(randomIp())
                .deviceType(randomDevice())
                .build());

        eventRepository.save(EventLog.builder()
                .userId(userId)
                .sessionId(session)
                .eventType(EventType.ERROR)
                .ipAddress(randomIp())
                .deviceType(randomDevice())
                .build());
    }

    /**
     * 시나리오 3: 강의 검색 SEARCH -> PAGE_VIEW
     */
    private void runSearchScenario() {
        UUID userId = randomUser();
        String session = UUID.randomUUID().toString();

        eventRepository.save(EventLog.builder()
                .userId(userId)
                .sessionId(session)
                .eventType(EventType.SEARCH)
                .ipAddress(randomIp())
                .deviceType(randomDevice())
                .build());

        eventRepository.save(EventLog.builder()
                .userId(userId)
                .sessionId(session)
                .eventType(EventType.PAGE_VIEW)
                .ipAddress(randomIp())
                .deviceType(randomDevice())
                .build());
    }

    private UUID randomUser() {
        return USER_POOL.get(new Random().nextInt(USER_POOL.size()));
    }

    private String randomIp() {
        return IP_POOL.get(new Random().nextInt(IP_POOL.size()));
    }

    private DeviceType randomDevice() {
        return DeviceType.values()[new Random().nextInt(DeviceType.values().length)];
    }
}

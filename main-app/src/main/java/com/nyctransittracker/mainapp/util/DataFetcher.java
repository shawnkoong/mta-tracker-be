package com.nyctransittracker.mainapp.util;

import com.nyctransittracker.mainapp.dto.MtaResponse;
import com.nyctransittracker.mainapp.service.NotificationService;
import com.nyctransittracker.mainapp.service.RedisService;
import com.nyctransittracker.mainapp.service.TimeService;
import com.nyctransittracker.mainapp.service.TrainPositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DataFetcher {

    private final WebClient webClient;
    private final static String url = "https://www.goodservice.io/api/routes/?detailed=1";
    private final RedisService redisService;
    private final TrainPositionService trainPositionService;
    private final TimeService timeService;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 15000)
    public void fetchData() {
        log.info("Starting fetching data: " + Instant.now().toString());
        MtaResponse mtaResponse = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(MtaResponse.class)
                .block();
        redisService.saveData(mtaResponse);
        log.info("Done with fetching data: " + Instant.now().toString());

        trainPositionService.processTrainPositions();
        timeService.processTimeInfo();
//        notificationService.processNotifications();
    }
}

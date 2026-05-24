package com.LastBite.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    @Bean
    public Clock appClock() {
        return Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
}

package com.seniordesigndbgt.dashboard.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TwitterSchedule {

    @Scheduled (fixedDelay = 5000)
    public void gatherTwitter()
    {

    }
}
package com.finance.loader.tasks;

import com.finance.loader.executor.FinanceTasksExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@EnableScheduling
@Component
public class FinanceInfoPrinter {

    private final FinanceTasksExecutor executor;
    private final Logger logger = LoggerFactory.getLogger(FinanceInfoPrinter.class.getName());

    public FinanceInfoPrinter(FinanceTasksExecutor executor) {
        this.executor = executor;
    }

    @PostConstruct
    public void init() {
        executor.start();
    }

    @Scheduled(fixedRate = 5000L)
    public void scheduleInstitutionsLoading() {
        logger.info(String.format("The highest stock: %s \n ||||||||||||||| \n The gr-st change percent %s2: ",
                executor.highestStockCompanies(),
                executor.highestChangePercentCompanies()));
    }
}

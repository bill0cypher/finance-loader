package com.finance.loader.tasks;

import com.finance.loader.executor.FinanceTasksExecutor;
import com.finance.loader.model.enums.AuthStatus;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class AuthListener {

    private final FinanceTasksExecutor tasksExecutor;

    public AuthListener(FinanceTasksExecutor tasksExecutor) {
        this.tasksExecutor = tasksExecutor;
    }

    @SqsListener(value = "auth-notification-queue")
    public void authListener(AuthStatus authStatus) {
        if (authStatus.equals(AuthStatus.AUTHORIZED))
            tasksExecutor.start();
        else if (authStatus.equals(AuthStatus.FORBIDDEN))
            tasksExecutor.setShutdown(true);
    }
}

package com.mykola2312.mptv.task;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessService implements Task {
    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    private final ArrayList<TaskProcess> processes = new ArrayList<>();

    private static final String TASK_NAME = "processService";

    public void registerProcess(TaskProcess process) {
        processes.add(process);
    }

    public void unregisterProcess(TaskProcess process) {
        processes.remove(process);
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public void dispatch() {
        for (TaskProcess process : processes) {
            if (!process.isAlive()) {
                try {
                    process.spawn();
                } catch (Exception e) {
                    logger.error("failed to respawn process: ", e);
                }
            }
        }
    }
}

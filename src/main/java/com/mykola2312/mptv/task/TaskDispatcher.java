package com.mykola2312.mptv.task;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jooq.*;
import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.*;
import static com.mykola2312.mptv.tables.Task.*;

import com.mykola2312.mptv.config.TaskItem;
import com.mykola2312.mptv.db.DB;
import com.mykola2312.mptv.tables.records.TaskRecord;

public class TaskDispatcher implements Runnable {
    private static final Logger logger = Logger.getLogger(TaskDispatcher.class);

    private final HashMap<String, Task> taskHandles = new HashMap<>();
    private boolean isRunning = false;
    private Integer smallestInterval = null;

    public void updateTaskConfig(List<TaskItem> items) {
        ArrayList<UpdatableRecord<TaskRecord>> tasks = new ArrayList<>();
        for (var item : items) {
            UpdatableRecord<TaskRecord> task = new UpdatableRecordImpl<>(TASK);
            task.set(TASK.NAME, item.name);
            task.set(TASK.INTERVAL, item.interval);
            
            tasks.add(task);
        }

        DSL.using(DB.CONFIG)
            .batchMerge(tasks)
            .execute();
    }

    public void registerTask(Task task) {
        taskHandles.put(task.getTaskName(), task);
        // find smallest interval
        try {
            smallestInterval = DSL.using(DB.CONFIG)
                .select(TASK.INTERVAL)
                .from(TASK)
                .orderBy(TASK.INTERVAL.asc())
                .limit(1)
                .fetchSingleInto(Integer.class);
        } catch (NoDataFoundException e) {
            logger.error("no tasks present in database!");
        }
    }

    private void dispatchTask(Task task) {
        logger.info(String.format("dispatching task %s", task.getTaskName()));
        try {
            task.dispatch();
        } catch (Exception e) {
            logger.error(e);
        }
        
        DSL.using(DB.CONFIG)
            .update(TASK)
            .set(TASK.LAST_TIME, Instant.now().toEpochMilli() / 1000L)
            .where(TASK.NAME.eq(task.getTaskName()))
            .execute();
    }

    @Override
    public void run() {
        // task dispatching loop
        isRunning = true;
        while (isRunning) {
            try {
                Thread.sleep(smallestInterval != null 
                    ? smallestInterval * 1000 : 1000);
            } catch (InterruptedException e) {
                logger.info("interrupted. exiting");
                isRunning = false;
                break;
            }

            long thisTime = Instant.now().toEpochMilli() / 1000L;
            List<String> taskNames = DSL.using(DB.CONFIG)
               .select(TASK.NAME)
               .from(TASK)
               .where(String.format("%d > task.last_time + task.interval", thisTime))
               .fetchInto(String.class);
            for (String name: taskNames) {
                Task task = taskHandles.get(name);
                if (task == null) {
                    logger.error(String.format("task %s is not registered!", name));
                    continue;
                }

                dispatchTask(task);
            }
        }
    }
}

package com.gly091020.CreateTreadmill.little_mad;

import com.gly091020.CreateTreadmill.maid.treadmill.UseTreadmillTask;
import com.gly091020.touhouLittleMad.util.TaskMoodRegistry;

public class LittleMadRegistry {
    public static void registry(){
        TaskMoodRegistry.registry(UseTreadmillTask.class, 1/1300f);
    }
}

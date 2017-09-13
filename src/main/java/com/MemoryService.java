package com;

/**
 * Created by andrius on 12/09/2017.
 */
public class MemoryService {

    public static int getFreeMemoryPerc() {
        return (int) Math.round(Runtime.getRuntime().freeMemory() * 100 / Runtime.getRuntime().totalMemory());
    }
}

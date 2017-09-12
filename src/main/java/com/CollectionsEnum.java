package com;

import com.entit.*;

/**
 * Created by andrius on 10/09/2017.
 */
public enum CollectionsEnum {

    RUNNERS(Runner.class),
    WORDS_STATISTICS(WordsStatistics.class),
    CHUNKS(Chunk.class),
    SYSTEM(Global.class);

    private Class entityClass;

    CollectionsEnum(Class entityClass) {
        this.entityClass = entityClass;
    }

    public static <T> String getCollectionByClass(Class<T> entityClass) {
        if (entityClass != null) {
            for (CollectionsEnum var : CollectionsEnum.values()) {
                if (entityClass == var.getEntityClass()) {
                    return var.name().toLowerCase();
                }
            }
        }
        return null;
    }

    public Class getEntityClass() {
        return entityClass;
    }
}

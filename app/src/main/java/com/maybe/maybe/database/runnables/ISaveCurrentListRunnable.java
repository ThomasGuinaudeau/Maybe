package com.maybe.maybe.database.runnables;

public interface ISaveCurrentListRunnable {
    void onSaveCurrentListRunnableStart(int max);

    void onSaveCurrentListRunnableProgress(int progress);

    void onSaveCurrentListRunnableFinish();
}

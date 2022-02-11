package com.maybe.maybe.database.async_tasks;

public interface OnSaveCurrentListAsyncTaskFinish {
    void onSaveCurrentListAsyncTaskStart(int max);

    void onSaveCurrentListAsyncTaskProgress(int progress);

    void onSaveCurrentListAsyncTaskFinish();
}

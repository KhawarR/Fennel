package wal.fennel.models;

import io.realm.RealmObject;

/**
 * Created by irfanayaz on 12/29/16.
 */

public class DashboardTask extends RealmObject {

    String taskId;
    String taskName;
    String dueDate;
    String completionDate;
    int totalCount;
    int completed;
    int state;

    public DashboardTask() {

    }

    public DashboardTask(String taskId, String taskName, String dueDate, String completionDate, int totalCount, int completed, int state) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.dueDate = dueDate;
        this.completionDate = completionDate;
        this.totalCount = totalCount;
        this.completed = completed;
        this.state = state;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}

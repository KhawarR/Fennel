package wal.fennel.models;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by irfanayaz on 12/29/16.
 */

public class DashboardFieldAgent extends RealmObject {

    String agentName;
    String agentNumber;
    String agentType;
    String agentId;
    String agentEmployeeId;
    String agentAttachmentUrl;

    RealmList<DashboardTask> dashboardTasks = new RealmList<>();

    public boolean isHeader = false;

    public DashboardFieldAgent() {}

    public DashboardFieldAgent(String agentName, String agentNumber, String agentType, String agentId, String agentEmployeeId, String agentAttachmentUrl, RealmList<DashboardTask> dashboardTasks, boolean header) {
        this.agentName = agentName;
        this.agentNumber = agentNumber;
        this.agentType = agentType;
        this.agentId = agentId;
        this.agentEmployeeId = agentEmployeeId;
        this.agentAttachmentUrl = agentAttachmentUrl;
        this.dashboardTasks = dashboardTasks;
        this.isHeader = header;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentNumber() {
        return agentNumber;
    }

    public void setAgentNumber(String agentNumber) {
        this.agentNumber = agentNumber;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentEmployeeId() {
        return agentEmployeeId;
    }

    public void setAgentEmployeeId(String agentEmployeeId) {
        this.agentEmployeeId = agentEmployeeId;
    }

    public String getAgentAttachmentUrl() {
        return agentAttachmentUrl;
    }

    public void setAgentAttachmentUrl(String agentAttachmentUrl) {
        this.agentAttachmentUrl = agentAttachmentUrl;
    }

    public RealmList<DashboardTask> getDashboardTasks() {
        return dashboardTasks;
    }

    public void setDashboardTasks(RealmList<DashboardTask> dashboardTasks) {
        this.dashboardTasks = dashboardTasks;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }
}

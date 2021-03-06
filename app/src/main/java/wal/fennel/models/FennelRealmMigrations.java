package wal.fennel.models;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Khawar on 6/12/2016.
 */

public class FennelRealmMigrations implements RealmMigration{

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        // DynamicRealm exposes an editable schema
        RealmSchema schema = realm.getSchema();

        // Migrate to version 1: Add a new class.
        // Example:
        // public Person extends RealmObject {
        //     private String name;
        //     private int age;
        //     // getters and setters left out for brevity
        // }
        if (oldVersion == 0) {
            schema.get("Farmer").addField("lastModifiedTime", Date.class);
            oldVersion++;
        }

        if (oldVersion == 1) {
            // Migrate from develop to phase2 (v50)

            RealmObjectSchema taskItemOptionSchema = schema.create("TaskItemOption")
                    .addField("id", String.class)
                    .addField("name", String.class)
                    .addField("isValue", boolean.class)
                    .addField("isDataDirty", boolean.class);

            RealmObjectSchema taskItemSchema = schema.create("TaskItem")
                    .addField("sequence", int.class)
                    .addField("id", String.class)
                    .addField("farmingTaskId", String.class)
                    .addField("name", String.class)
                    .addField("recordType", String.class)
                    .addField("description", String.class)
                    .addField("textValue", String.class)
                    .addField("fileType", String.class)
                    .addField("fileActionType", String.class)
                    .addField("fileActionPerformed", String.class)
                    .addField("gpsTakenTime", String.class)
                    .addField("latitude", double.class)
                    .addField("longitude", double.class)
                    .addRealmListField("options", taskItemOptionSchema)
                    .addField("isDataDirty", boolean.class)
                    .addField("isPicUploadDirty", boolean.class)
                    .addField("attachmentPath", String.class)
                    .addField("attachmentId", String.class)
                    .addField("dateModified", Date.class)
                    .addField("agentName", String.class)
                    .addField("farmerName", String.class)
                    .addField("agentAttachmentId", String.class)
                    .addField("isTaskDone", boolean.class);

            RealmObjectSchema taskSchema = schema.create("Task")
                    .addField("taskId", String.class)
                    .addField("name", String.class)
                    .addField("startedDate", String.class)
                    .addField("completionDate", String.class)
                    .addField("dueDate", String.class)
                    .addField("status", String.class)
                    .addField("isHeader", boolean.class)
                    .addRealmListField("taskItems", taskItemSchema)
                    .addField("agentId", String.class)
                    .addField("agentName", String.class)
                    .addField("agentPhoneNumber", String.class)
                    .addField("agentEmployeeId", String.class)
                    .addField("agentType", String.class)
                    .addField("farmerName", String.class)
                    .addField("shambaName", String.class)
                    .addField("taskShambaId", String.class)
                    .addField("taskFarmerId", String.class)
                    .addField("isDataDirty", boolean.class);

            schema.get("Farmer")
                    .addRealmListField("farmerTasks", taskSchema)
                    .addField("farmerType", String.class);

            RealmObjectSchema dashboardTaskSchema = schema.create("DashboardTask")
                    .addField("taskId", String.class)
                    .addField("taskName", String.class)
                    .addField("dueDate", String.class)
                    .addField("completionDate", String.class)
                    .addField("totalCount", int.class)
                    .addField("completed", int.class)
                    .addField("state", int.class);

            RealmObjectSchema dashboardFieldAgentSchema = schema.create("DashboardFieldAgent")
                    .addField("agentName", String.class)
                    .addField("agentNumber", String.class)
                    .addField("agentType", String.class)
                    .addField("agentId", String.class)
                    .addField("agentEmployeeId", String.class)
                    .addField("agentAttachmentUrl", String.class)
                    .addRealmListField("dashboardTasks", dashboardTaskSchema)
                    .addField("isHeader", boolean.class);

            RealmObjectSchema farmVisitSchema = schema.create("FarmVisit")
                    .addField("farmVisitId", String.class)
                    .addField("loggedInPersonRole", String.class)
                    .addField("loggedInPersonId", String.class)
                    .addField("shambaId", String.class)
                    .addField("farmerId", String.class)
                    .addField("visitedDate", long.class)
                    .addField("isDataDirty", boolean.class);

            RealmObjectSchema farmVisitLogSchema = schema.create("FarmVisitLog")
                    .addField("farmVisitId", String.class)
                    .addField("farmingTaskId", String.class)
                    .addField("isDataDirty", boolean.class);

            RealmObjectSchema logTaskItemSchema = schema.create("LogTaskItem")
                    .addField("sequence", int.class)
                    .addField("id", String.class)
                    .addField("farmingTaskId", String.class)
                    .addField("name", String.class)
                    .addField("recordType", String.class)
                    .addField("description", String.class)
                    .addField("textValue", String.class)
                    .addField("fileType", String.class)
                    .addField("fileActionType", String.class)
                    .addField("fileActionPerformed", String.class)
                    .addField("gpsTakenTime", String.class)
                    .addField("latitude", double.class)
                    .addField("longitude", double.class)
                    .addRealmListField("options", taskItemOptionSchema)
                    .addField("isDataDirty", boolean.class)
                    .addField("isPicUploadDirty", boolean.class)
                    .addField("attachmentPath", String.class)
                    .addField("attachmentId", String.class)
                    .addField("dateModified", Date.class)
                    .addField("agentName", String.class)
                    .addField("farmerName", String.class)
                    .addField("agentAttachmentId", String.class)
                    .addField("isTaskDone", boolean.class);

            RealmObjectSchema fieldAgentSchema = schema.create("FieldAgent")
                    .addField("name", String.class)
                    .addField("phoneNumber", String.class)
                    .addField("agentType", String.class)
                    .addField("agentId", String.class)
                    .addField("agentEmployeeId", String.class)
                    .addField("agentAttachmentUrl", String.class)
                    .addRealmListField("visitLogs", logTaskItemSchema)
                    .addField("isHeader", boolean.class);

            oldVersion++;
        }

        if (oldVersion == 2) {

            schema.get("DashboardTask")
                    .addField("shambaId", String.class)
                    .addField("farmerId", String.class);

            oldVersion++;
        }

        if (oldVersion == 3) {

            schema.get("FarmVisitLog")
                    .addField("taskItemId", String.class)
                    .addField("taskModifiedTime", Date.class)
                    .addField("logMessage", String.class);

            oldVersion++;
        }

        if (oldVersion == 4) {

            schema.get("TaskItem")
                    .addField("logbookMessage", String.class);

            schema.get("LogTaskItem")
                    .addField("logbookMessage", String.class);

            oldVersion++;
        }

        if (oldVersion == 5) {

            schema.get("TaskItem").removeField("gpsTakenTime");
            schema.get("TaskItem").addField("gpsTakenTime", Date.class);

            schema.get("LogTaskItem").removeField("gpsTakenTime");
            schema.get("LogTaskItem").addField("gpsTakenTime", Date.class);

            oldVersion++;
        }

        if (oldVersion < newVersion) {
            throw new IllegalStateException(String.format("Migration missing from v%d to v%d", oldVersion, newVersion));
        }
    }
}

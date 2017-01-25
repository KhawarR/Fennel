package wal.fennel.models;

import android.os.Parcelable;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObject;
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
                    .addField("isValue", Boolean.class)
                    .addField("isDataDirty", Boolean.class);

            RealmObjectSchema taskItemSchema = schema.create("TaskItem")
                    .addField("sequence", Integer.class)
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
                    .addField("latitude", Double.class)
                    .addField("longitude", Double.class)
                    .addRealmListField("options", taskItemOptionSchema)
                    .addField("isDataDirty", Boolean.class)
                    .addField("isPicUploadDirty", Boolean.class)
                    .addField("attachmentPath", String.class)
                    .addField("attachmentId", String.class)
                    .addField("dateModified", Date.class)
                    .addField("agentName", String.class)
                    .addField("farmerName", String.class)
                    .addField("agentAttachmentId", String.class)
                    .addField("isTaskDone", Boolean.class);

            RealmObjectSchema taskSchema = schema.create("Task")
                    .addField("taskId", String.class)
                    .addField("name", String.class)
                    .addField("startedDate", String.class)
                    .addField("completionDate", String.class)
                    .addField("dueDate", String.class)
                    .addField("status", String.class)
                    .addField("isHeader", Boolean.class)
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
                    .addField("isDataDirty", Boolean.class);

            schema.get("Farmer")
                    .addRealmListField("farmerTasks", taskSchema)
                    .addField("farmerType", String.class);

            RealmObjectSchema dashboardTaskSchema = schema.create("DashboardTask")
                    .addField("taskId", String.class)
                    .addField("taskName", String.class)
                    .addField("dueDate", String.class)
                    .addField("completionDate", String.class)
                    .addField("totalCount", Integer.class)
                    .addField("completed", Integer.class)
                    .addField("state", Integer.class);

            RealmObjectSchema dashboardFieldAgentSchema = schema.create("DashboardFieldAgent")
                    .addField("agentName", String.class)
                    .addField("agentNumber", String.class)
                    .addField("agentType", String.class)
                    .addField("agentId", String.class)
                    .addField("agentEmployeeId", String.class)
                    .addField("agentAttachmentUrl", String.class)
                    .addRealmListField("dashboardTasks", dashboardTaskSchema)
                    .addField("isHeader", Boolean.class);

            RealmObjectSchema farmVisitSchema = schema.create("FarmVisit")
                    .addField("farmVisitId", String.class)
                    .addField("loggedInPersonRole", String.class)
                    .addField("loggedInPersonId", String.class)
                    .addField("shambaId", String.class)
                    .addField("farmerId", String.class)
                    .addField("visitedDate", Long.class)
                    .addField("isDataDirty", Boolean.class);

            RealmObjectSchema farmVisitLogSchema = schema.create("FarmVisitLog")
                    .addField("farmVisitId", String.class)
                    .addField("farmingTaskId", String.class)
                    .addField("isDataDirty", Boolean.class);

            RealmObjectSchema logTaskItemSchema = schema.create("LogTaskItem")
                    .addField("sequence", Integer.class)
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
                    .addField("latitude", Double.class)
                    .addField("longitude", Double.class)
                    .addRealmListField("options", taskItemOptionSchema)
                    .addField("isDataDirty", Boolean.class)
                    .addField("isPicUploadDirty", Boolean.class)
                    .addField("attachmentPath", String.class)
                    .addField("attachmentId", String.class)
                    .addField("dateModified", Date.class)
                    .addField("agentName", String.class)
                    .addField("farmerName", String.class)
                    .addField("agentAttachmentId", String.class)
                    .addField("isTaskDone", Boolean.class);

            RealmObjectSchema fieldAgentSchema = schema.create("FieldAgent")
                    .addField("name", String.class)
                    .addField("phoneNumber", String.class)
                    .addField("agentType", String.class)
                    .addField("agentId", String.class)
                    .addField("agentEmployeeId", String.class)
                    .addField("agentAttachmentUrl", String.class)
                    .addRealmListField("visitLogs", logTaskItemSchema)
                    .addField("isHeader", Boolean.class);

            oldVersion++;
        }

        if (oldVersion < newVersion) {
            throw new IllegalStateException(String.format("Migration missing from v%d to v%d", oldVersion, newVersion));
        }
    }
}

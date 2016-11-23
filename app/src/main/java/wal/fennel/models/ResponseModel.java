package wal.fennel.models;

/**
 * Created by irfanayaz on 10/5/16.
 */
public class ResponseModel {

    private String id;
    private boolean success;
    private Object[] errors;

    public void setId(String id) {
        this.id = id;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setErrors(Object[] errors) {
        this.errors = errors;
    }

    public String getId() {
        return id;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object[] getErrors() {
        return errors;
    }
}

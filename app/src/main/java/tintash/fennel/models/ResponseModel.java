package tintash.fennel.models;

/**
 * Created by irfanayaz on 10/5/16.
 */
public class ResponseModel {

    public String id;
    public boolean success;
    public Object[] errors;

    public void setId(String id) {
        this.id = id;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setErrors(Object[] errors) {
        this.errors = errors;
    }
}

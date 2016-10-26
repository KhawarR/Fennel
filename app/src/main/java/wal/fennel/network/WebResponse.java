package wal.fennel.network;

/**
 * Created by Faizan on 10/21/2015.
 */
public class WebResponse<T> {

    String Status;
    String Description;
    T Value;


    public boolean isSuccess() {
        return Status.equals("Success");
    }

    public String getDescription() {
        return Description;
    }

    public T getResult() {
        return Value;
    }
}

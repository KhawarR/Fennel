package tintash.fennel.datamodels;


import java.util.ArrayList;
import java.util.List;

public class LoginResponse {


    public Integer totalSize;
    public Boolean done;
    public List<Record> records = new ArrayList<Record>();
}
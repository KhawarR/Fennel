package tintash.fennel.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import tintash.fennel.models.Farmer;

/**
 * Created by irfanayaz on 10/5/16.
 */
public class FarmerFieldsExclusion implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return (f.getDeclaringClass() == Farmer.class && f.getName().equals("address"))||
                (f.getDeclaringClass() == Farmer.class && f.getName().equals("signupStatus")) ||
                (f.getDeclaringClass() == Farmer.class && f.getName().equals("isHeader")) ||
                (f.getDeclaringClass() == Farmer.class && f.getName().equals("thumbUrl")) ||
                (f.getDeclaringClass() == Farmer.class && f.getName().equals("farmerIdPhotoUrl")) ||
                (f.getDeclaringClass() == Farmer.class && f.getName().equals("villageName")) ||
                (f.getDeclaringClass() == Farmer.class && f.getName().equals("treeSpecies")) ||
                (f.getDeclaringClass() == Farmer.class && f.getName().equals("farmerHome"));
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}

package pt.tecnico.contacttracing.model;

import java.util.ArrayList;
import java.util.List;


public class SignedBatch {
    public List<NumberKey> nk_array = new ArrayList<>();
    public String signature; // base64 encoded

    public SignedBatch(List<NumberKey> nk_array, int n, String signature){
        this.signature = signature;

        for (int i=0; i<n; i++){
            this.nk_array.add(nk_array.get(i));
        }
    }
}

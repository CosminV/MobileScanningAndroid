package Requests;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class T1Request extends StringRequest {
    private static final String T1_REQUESTURL = "http://docscanner.netne.net/T1SaveMeta.php";
    private Map<String, String> params;

    public T1Request(String docname, String secretariat, String id, String address, Response.Listener<String> listener){
        super(Method.POST, T1_REQUESTURL, listener, null);
        params = new HashMap<>();
        params.put("docname", docname);
        params.put("secretariat", secretariat);
        params.put("id", id);
        params.put("address", address);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}

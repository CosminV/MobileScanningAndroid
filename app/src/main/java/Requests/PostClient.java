package Requests;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class PostClient extends StringRequest {

    private static final String clientposturl = "http://192.168.175.44:83/api/Person";
    private Map<String, String> params;

    public PostClient(String firstName, String lastName, String middleName, Response.Listener<String> listener){
        super(Method.POST, clientposturl, listener, null);

        params = new HashMap<>();
        params.put("FirstName", firstName);
        params.put("LastName", lastName);
        params.put("MiddleName", middleName);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

}

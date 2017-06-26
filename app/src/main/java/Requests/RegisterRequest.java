package Requests;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class RegisterRequest extends StringRequest {

    private static final String REGISTER_REQUESTURL = "http://docscanner.ezyro.com/Register.php";
    private Map<String, String> params;

    public RegisterRequest(String email, String password, String name, int age, String location, Response.Listener<String> listener){
        super(Method.POST, REGISTER_REQUESTURL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        params.put("name", name);
        params.put("age", age+ "");
        params.put("location", location);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}

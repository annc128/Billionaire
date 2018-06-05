package finalproject.comp3617.billionaire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MapJsonResponse {
    public Map[] parseJSON(String response) {
        Gson gson = new GsonBuilder().create();
        Map[] responseObject = gson.fromJson(response, Map[].class);
        return responseObject;
    }


}

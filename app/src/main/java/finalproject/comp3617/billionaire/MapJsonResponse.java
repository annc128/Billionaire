package finalproject.comp3617.billionaire;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class MapJsonResponse {
    Context context;

    MapJsonResponse(Context ctx) {
        context = ctx;
    }

    public Map[] parseJSON(String response) {
        Gson gson = new GsonBuilder().create();
        Map[] responseObject = gson.fromJson(response, Map[].class);
        return responseObject;
    }

    public String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName + ".json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

//    private void mapParsedResponse() {
//        String jsonResponse = loadJSONFromAsset();
//        Map[] mapJsonResponse = parseJSON(jsonResponse);
//
//        List<Map> listMaps = Arrays.asList(mapJsonResponse);
//
//        for (Map map : listMaps) {
//            System.out.println("technicaljungle ---- Name -> " + map.getName()
//                    + " -- Latitude -- " + map.getLatitude() + "--- Longitude --" + map.getLongitude());
//        }
//    }


}

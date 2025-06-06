package it.xview.cp.rest4Buyer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestApiCall {
    private String apiToken = "https://fpgcollaudo.4buyer.it/gateway/api/authentication/token";
    private String apiRequest = "https://fpgcollaudo.4buyer.it/gateway/api/it/Services/OracleIsola/Vendors";
    private String jsonRequestToken = "{\"ApplicationSecret\": \"F4EEAB15-E758-4959-BF5A-9131840DE897\", \"ApplicationKey\": \"2C376702-82BB-45C7-ADD4-DC46521B6B54\"}";

    public static void main(String[] args) throws IOException {
/*
    	RestApiCall obj = new RestApiCall();
        String token = obj.getAuthenticationToken();
        if (token != null) {
            obj.getVendors(token);
        }
*/
    }

    public RestApiCall(String apiToken, String apiRequest, String sApplicationSecret, String sApplicationKey) {
		super();
		this.apiToken = apiToken;
		this.apiRequest = apiRequest;
		this.jsonRequestToken = "{\"ApplicationSecret\": \""+sApplicationSecret + "\", \"ApplicationKey\": \""+sApplicationKey+"\"}";
//		this.jsonRequestToken = "{\"ApplicationSecret\": \"F4EEAB15-E758-4959-BF5A-9131840DE897\", \"ApplicationKey\": \"2C376702-82BB-45C7-ADD4-DC46521B6B54\"}";
	}

	public String getAuthenticationToken() throws IOException {
        String url = apiToken;
        String jsonBody = jsonRequestToken;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(jsonBody);
            wr.flush();
        }

        int responseCode = con.getResponseCode();
        System.out.println("Codice di risposta (token): " + responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            String responseString = response.toString();
            JSONObject jsonObject = new JSONObject(responseString);
            
            String accessToken = jsonObject.getString("accessToken");

            System.out.println("Access Token: " + accessToken);
            return accessToken;
        }
    }

    public List<HashMap<String, Object>>  getVendors(String token) throws IOException {
        String url = apiRequest;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + token);
        
        int responseCode = con.getResponseCode();
        System.out.println("Codice di risposta (vendors): " + responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            
            if("{".equals(String.valueOf(response.charAt(0)))) response.insert(0, '[').append(']');
            List<HashMap<String, Object>> jVendors = jsonToListHashMap(response);
//            printHashMapList(jVendors);
            return jVendors ;
            
       }
    }

    public List<HashMap<String, Object>> jsonToListHashMap(StringBuilder response) {
        List<HashMap<String, Object>> resultList = new ArrayList<>();
        String jsonString = response.toString();

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                HashMap<String, Object> map = new HashMap<>();
                Iterator<String> keys = jsonObject.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = String.valueOf( jsonObject.get(key) );
                    map.put(key, value);
                }

                resultList.add(map);
            }
        } catch (org.json.JSONException e) {
            // Gestisci l'eccezione, ad esempio, se la stringa non è un JSON valido.
            System.err.println("Errore nell'analisi del JSON: " + e.getMessage());
        }

        return resultList;
    }

    public void printHashMap(HashMap<String, String> fields) {
            for (Entry<String, String> entry : fields.entrySet()) {
            	System.out.println("*" + entry.getKey() + " = " + entry.getValue());            }
    }
    
    public void printHashMapList(List<HashMap<String, Object>> hashMapList) {
        for (HashMap<String, Object> map : hashMapList) {
            StringBuilder line = new StringBuilder();
            for (Entry<String, Object> entry : map.entrySet()) {
                line.append(entry.getKey()).append(";").append(entry.getValue()).append(";");
            }
            if (line.length() > 0) {
                // Rimuovi l'ultimo ";" in eccesso
                line.deleteCharAt(line.length() - 1);
            }
            System.out.println(line.toString());
        }
    }

    public List<HashMap<String, Object>> postVendor(String token, HashMap<String, String> fields) throws Exception {
        String url = apiRequest;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Content-Type", "application/json"); //importante
        con.setDoOutput(true); // Imposta doOutput su true!

        JSONObject jsonObject = new JSONObject(fields);
        System.out.println(jsonObject);

        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(jsonObject.toString());
            wr.flush();
        }

        int responseCode = con.getResponseCode();
        System.out.println("Codice di risposta: " + responseCode);
        System.out.println("MSG di risposta : " + con.getResponseMessage());

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            if ("{".equals(String.valueOf(response.charAt(0)))) {
                response.insert(0, '[').append(']');
            }
            List<HashMap<String, Object>> jResult = jsonToListHashMap(response);
//            printHashMapList(jResult);
            return jResult;

        }
    }

}
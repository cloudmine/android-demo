package me.cloudmine.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.util.Log;

public class CMAdapter {
	//private static HashMap<String, String> sNotesProjectionMap;
	//private static String API_SERVER = "http://192.168.30.131:3000";
	//private static String API_SERVER = "http://192.168.1.31:3000";
	//private static String API_SERVER = "http://10.37.110.112:3000";
	private static String API_SERVER = "http://api-staging.cloudmine.me";

	//private static String API_SERVER = "http://ubuntu:3000";
	private static String APP_ID = "a6c0afaaa90e188a64d21cef93anotes";
	
	public static String join(Object[] array, char separator) {
        if (array == null) {
            return null;
        }
        int arraySize = array.length;
        int bufSize = (arraySize == 0 ? 0 : ((array[0] == null ? 16 : array[0].toString().length()) + 1) * arraySize);
        StringBuffer buf = new StringBuffer(bufSize);

        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }


	public JSONObject getValues(){
		return getValues(null);	
	}
	
	public JSONObject getValues(String[] keys){
     	// make request to API_SERVER/v1/app/APP_ID/text/
		String uri = API_SERVER + "/v1/app/" + APP_ID + "/text/";
		
		if(keys != null){
			uri += "?keys=" + join(keys, ',');
		}
		
		RawRESTClient client = new RawRESTClient();
		if( client.makeRequest(RESTClient.GET, uri) == null ){
			return null;
		}
		
		if( client.getStatusCode() >= 300 ){
			Log.d("JSON", "GET call returned HTTP code: " + client.getStatusCode());
        	return null;
		}

		String content = client.getBody();
		if(content == null){
			return null;
		}

		JSONObject response; 
		try {
			response = new JSONObject(content);
			JSONObject success = response.getJSONObject("success");

			return success;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public ContentValues jsonToContentValues(JSONObject json){
		ContentValues values = new ContentValues(json.length());
		Iterator<String> keys = json.keys();
		while(keys.hasNext()){
			String key = keys.next();
			try {
				values.put(key, json.getString(key));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return values;
	}
	
	public Map contentValuesToMap(ContentValues value){
		Map m = new HashMap<String, Object>();
		for(Entry<String, Object> e : value.valueSet()){
			m.put(e.getKey(), e.getValue());
		}
		return m;
	}
	
	public String setValue(String key, Map values){
		JSONObject value = new JSONObject(values);
		return setValue(key, value);
	}
	
	public String setValue(String key, ContentValues values){
		JSONObject value = new JSONObject(contentValuesToMap(values));
		return setValue(key, value);
	}
	
	public String setValue(String key, JSONObject value){
		JSONObject dataobj = new JSONObject();
		try {
			dataobj.put(key, value);
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		String data= "";
		try {
			data = dataobj.toString(4);
			System.out.println("Putting value: " + data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String uri = API_SERVER + "/v1/app/" + APP_ID + "/text/";
		
		RawRESTClient client = new RawRESTClient();
		client.setHeader("content-type", "application/json");
		client.makeRequest(RESTClient.PUT, uri, data);
		if(client.getResponse() == null){
			return null;
		}

		Log.d("JSON", "PUT call returned HTTP code: " + client.getStatusCode() + " " + client.getStatusMessage());

		String content = client.getBody();
		if(content == null){
			return null;
		}

		if(client.getStatusCode() >= 300){
			Log.d("JSON", "HTTP content: " + content);
			return null;
		}

		JSONObject response; 
		try {
			response = new JSONObject(content);
			JSONObject success = response.getJSONObject("success");

			String key_ret = success.keys().next().toString();
			return key_ret;

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
        return null;
	}
	
	public void deleteKeys(){
		deleteKeys(null);	
	}
	
	public void deleteKeys(String[] keys){
		String uri = API_SERVER + "/v1/app/" + APP_ID + "/data/";
		
		if(keys != null){
			uri += "?keys=" + join(keys, ',');
		}
		
		RawRESTClient client = new RawRESTClient();
		if( client.makeRequest(RESTClient.DELETE, uri) == null ){
			return;
		}

		Log.d("JSON", "DELETE call returned HTTP code: " + client.getStatusCode());
        	
	}
}

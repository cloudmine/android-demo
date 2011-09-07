package me.cloudmine.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;  
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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
	
	private String readStream(InputStream in){
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuffer buff = new StringBuffer();
		String line;
		
		try {
			while( (line = reader.readLine()) != null ){
				buff.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return buff.toString();
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
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(uri);
		
		HttpResponse httpResponse;
		 
        try {
        	System.out.println("Making GET request to: " + uri);
            httpResponse = client.execute(request);
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            String message = httpResponse.getStatusLine().getReasonPhrase();
            
            if(responseCode - 200 > 100){
            	Log.d("JSON", "GET call returned HTTP code: " + responseCode);
            	return null;
            }
 
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
            	String content = readStream(entity.getContent());
            	
            	JSONObject response; 
            	try {
					response = new JSONObject(content);
					JSONObject success = response.getJSONObject("success");
					
					return success;
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
            }
        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
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
		JSONObject data = new JSONObject();
		try {
			data.put(key, value);
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		String content = "";
		try {
			content = data.toString(4);
			System.out.println("Putting value: " + content);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String uri = API_SERVER + "/v1/app/" + APP_ID + "/text/";

		HttpClient client = new DefaultHttpClient();
		HttpPut request = new HttpPut(uri);
		request.setHeader("content-type", "application/json");
		try {
			request.setEntity(new StringEntity(content, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		HttpResponse httpResponse;
		 
        try {
        	System.out.println("Making PUT request to: " + uri);
            httpResponse = client.execute(request);
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            String message = httpResponse.getStatusLine().getReasonPhrase();

        	Log.d("JSON", "PUT call returned HTTP code: " + responseCode + " " + message);

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
            	String rcontent = readStream(entity.getContent());
            	
                if(responseCode - 200 > 100){
                	Log.d("JSON", "HTTP content: " + rcontent);
                	return null;
                }
                 	
            	JSONObject response; 
            	try {
					response = new JSONObject(rcontent);
					JSONObject success = response.getJSONObject("success");

					String key_ret = success.keys().next().toString();
					
					return key_ret;
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
            }
        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
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
		
		HttpClient client = new DefaultHttpClient();
		HttpDelete request = new HttpDelete(uri);
		
		HttpResponse httpResponse;
		 
        try {
        	System.out.println("Making DELETE request to: " + uri);
            httpResponse = client.execute(request);
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            String message = httpResponse.getStatusLine().getReasonPhrase();

        	Log.d("JSON", "DELETE call returned HTTP code: " + responseCode);
        	
        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }
	}
}

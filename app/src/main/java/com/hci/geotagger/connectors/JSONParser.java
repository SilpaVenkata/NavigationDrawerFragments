package com.hci.geotagger.connectors;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hci.geotagger.common.Constants;

import android.util.Log;
 
public class JSONParser	
{
	private final static String TAG="JSONParser";
	static InputStream is = null;
	static JSONObject jObj = null;
	static JSONArray jArr = null;
	static String json = "";
	static String jjson = "";
	static String code= null;
	static int success;
 
	/**
	 * This method will perform an HTTP POST command, without first adding the
	 * Access Token value. Just the input will be used to perform the operation.
	 * @param url
	 * @param params
	 * @return Return the JSON Object returned from the server
	 */
	public JSONObject getJSONFromUrlRaw(String url, List<NameValuePair> params) {
		try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
    		return getJSONFromUrl(httpClient, httpPost);
		} catch (Exception e) {
            JSONObject jObj = new JSONObject();
            success = 0;
            return jObj;
        }
	}

	
	public JSONObject getJSONFromUrl(String url, List<NameValuePair> params) {
		try {
	        params.add(new BasicNameValuePair("Authentication", "Bearer"));
	        params.add(new BasicNameValuePair("access_token",WebAPIConstants.ACCESS_TOKEN));

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
    		return getJSONFromUrl(httpClient, httpPost);
		} catch (Exception e) {
            JSONObject jObj = new JSONObject();
            success = 0;
            return jObj;
        }
	}
	
	private JSONObject getJSONFromUrl(DefaultHttpClient httpClient, HttpPost httpPost) {
		try {
            
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            Log.d("Response For Header", String.valueOf(httpResponse.getStatusLine().getStatusCode()));
            code = String.valueOf(httpResponse.getStatusLine().getStatusCode());

            is = httpEntity.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (Exception e) {
        	e.printStackTrace();
        	try {
        		jObj = new JSONObject();
        	} catch (Exception e2) {
        		return jObj;
        	}
        }
        
        try {
            if (Integer.parseInt(code) == HttpStatus.SC_OK) {
            	success = 1; 
            	jObj.put("success", success);
            	Log.d("JSON with Success:", jObj.toString());
            } else {
            	success = 0;
            	jObj.put("success", success);
            }
        } catch (JSONException e) {
            Log.e("JSON Parser getJSON", "Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

	/**
	 * This method will perform an HTTP DELETE request. The access_token parameter will
	 * be added in this method.
	 * @param url The URL of the DELETE request, does NOT contain the access_token
	 * @return
	 */
    public ReturnInfo deleteRequest(String url) {
    	ReturnInfo retValue;
    	
    	String code = null;
    	try {
    		// defaultHttpClient
    		DefaultHttpClient httpClient = new DefaultHttpClient();
    		HttpDelete httpDelete=new HttpDelete(url);
    		
    		// Put the Access Token in
    		HttpParams httpParams = new BasicHttpParams();
//    		httpParams.setParameter("Authentication", "Bearer");
    		httpParams.setParameter("access_token",WebAPIConstants.ACCESS_TOKEN);
    		httpDelete.setParams(httpParams);

            
    		HttpResponse httpResponse = httpClient.execute(httpDelete);
             
    		Log.d("Response For Header", String.valueOf(httpResponse.getStatusLine().getStatusCode()));
    		code = String.valueOf(httpResponse.getStatusLine().getStatusCode());

    		if (Integer.parseInt(code) == HttpStatus.SC_NO_CONTENT) {
    			retValue = new ReturnInfo(ReturnInfo.SUCCESS);
    			Log.d(TAG, "JSON with Success:"+jObj.toString());
    		} else {
    			retValue = new ReturnInfo(ReturnInfo.FAIL_GENERAL);
    			Log.d(TAG, "JSON with Failure");
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
			retValue = new ReturnInfo(ReturnInfo.FAIL_JSONERROR);
    	}
  
    	return retValue;
	}


    public JSONObject deleteRequestToREST(String url) {
    	String code = null;
    	try {
    		// defaultHttpClient
    		DefaultHttpClient httpClient = new DefaultHttpClient();

    		/*
			List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("Authentication", "Bearer"));
            params.add(new BasicNameValuePair("access_token",WebAPIConstants.ACCESS_TOKEN));
    		HttpDelete httpDelete=new HttpDelete(url+ '?'+ URLEncodedUtils.format(params,"UTF-8"));
    		 */
    		
    		HttpDelete httpDelete=new HttpDelete(url);
    		
    		// Put the Access Token in
    		HttpParams httpParams = new BasicHttpParams();
//    		httpParams.setParameter("Authentication", "Bearer");
    		httpParams.setParameter("access_token",WebAPIConstants.ACCESS_TOKEN);
    		httpDelete.setParams(httpParams);

    		
    		HttpResponse httpResponse = httpClient.execute(httpDelete);
             
    		// HttpEntity httpEntity = httpResponse.getEntity();

    		Log.d("Response For Header", String.valueOf(httpResponse.getStatusLine().getStatusCode()));
    		code = String.valueOf(httpResponse.getStatusLine().getStatusCode());
         } catch (Exception e) {
             e.printStackTrace();
         }
  
         try {		
        	 jObj = new JSONObject();
             if (Integer.parseInt(code) == HttpStatus.SC_NO_CONTENT) {
             	success = 1; 
             	jObj.put("success", success);
             	jObj.put("statusCode", code);
             	Log.d("JSON with Success:", jObj.toString());
             } else {
             	success = 0;
             	jObj.put("success", success);
             	jObj.put("statusCode",code);
             }
         } catch (JSONException e) {
             Log.e("JSON Parser getJSON", "Error parsing data " + e.toString());
         }
         return jObj;
     }

    /**
     * Perform an HTTP Post operation
     * @param url
     * @param params
     * @return
     */
    public JSONObject postToServer(String url, JSONObject params) {
    	try {
            List<NameValuePair> authParams = new ArrayList<NameValuePair>();       
            authParams.add(new BasicNameValuePair("Authentication", "Bearer"));
            authParams.add(new BasicNameValuePair("access_token",WebAPIConstants.ACCESS_TOKEN));        

    		URI uri = new URI( url + "?" + URLEncodedUtils.format(authParams, "utf-8"));
    		Log.d(TAG, "postToServer: URI=" + uri.toString());

    		DefaultHttpClient httpClient = new DefaultHttpClient();
    		HttpPost httpPost = new HttpPost(uri);
             
    		// Put the Access Token in
    		/*
     		HttpParams httpParams = new BasicHttpParams();
     		httpParams.setParameter("Authentication", "Bearer");
     		httpParams.setParameter("access_token",WebAPIConstants.ACCESS_TOKEN);
     		httpPost.setParams(httpParams);
     		*/
    		
    		/*
    		httpPost.setHeader("Authentication", "Bearer");
    		httpPost.setHeader("access_token",WebAPIConstants.ACCESS_TOKEN);
             */
    		
     		StringEntity se = new StringEntity( params.toString()); 
     		se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
             
     		httpPost.setEntity(se);
     		Log.d(TAG, "postToServer: HTTP Post=" + httpPost.toString());

     		HttpResponse response = httpClient.execute(httpPost);
     		HttpEntity httpEntity = response.getEntity();
     		Log.i(TAG, "postToServer: reason phrase=" + response.getStatusLine().getReasonPhrase());
     		code = String.valueOf(response.getStatusLine().getStatusCode());
     		Log.i(TAG, "postToServer: Status code=" + code.toString());
             
     		is = httpEntity.getContent();
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
         
        String jString; 
     	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
    		StringBuilder sb = new StringBuilder();
    		String line = null;
    		while ((line = reader.readLine()) != null) {
    			sb.append(line + "\n");
    		}
    		is.close();
    		jString = sb.toString();
    	} catch (Exception e) {
    		Log.e("Buffer Error", "Error converting result " + e.toString());
    		return null;
    	}
    	
    	JSONObject jObject;
    	
    	// try parse the string to a JSON object
    	try {
    		jObject = new JSONObject(jString);
    		int codeInt = Integer.parseInt(code);
            if (codeInt == HttpStatus.SC_CREATED || codeInt == HttpStatus.SC_OK) {
            	success = 1; 
            	jObject.put("success", success);
            	jObject.put("status_code", code);
            } else {
            	success = 0;
            	jObject.put("success", success);
            	jObject.put("status_code", code);
            }
    	} catch (JSONException e) {
    		Log.e("JSON Parser getJSON", "Error parsing data " + e.toString());
    		jObject = null;
    	}

         return jObject;
     }
     
     //----------------------------------------------------------------------------
     /**
      * @author Syed M Shah
      * REST PUT call to add and get boolean in return
      * @throws IOException 
      * @throws ClientProtocolException 
      */
    public boolean restPutCall(String url, JSONObject params) throws ClientProtocolException, IOException {
    	boolean success = false;
    	DefaultHttpClient httpClient = new DefaultHttpClient();
    	StringBuilder result = new StringBuilder();
    	Log.d("restPutCall","Inside the method");
    	
        List<NameValuePair> authParams = new ArrayList<NameValuePair>();
//        authParams.add(new BasicNameValuePair("Authentication", "Bearer"));
        authParams.add(new BasicNameValuePair("access_token",WebAPIConstants.ACCESS_TOKEN));        

        URI uri;
        try {
        	uri = new URI( url + "?" + URLEncodedUtils.format(authParams, "utf-8"));
        } catch (Exception e) {
        	return false;
        }

    	
    	
    	HttpPut putRequest = new HttpPut(uri);
             
    	putRequest.addHeader("Content-Type", "application/json");
    	putRequest.addHeader("Accept", "application/json");
             
    	// Put the Access Token in
/*    	HttpParams httpParams = new BasicHttpParams();
//      httpParams.setParameter("Authentication", "Bearer");
    	httpParams.setParameter("access_token",WebAPIConstants.ACCESS_TOKEN);
    	putRequest.setParams(httpParams);
*/
    	
    	StringEntity se = new StringEntity( params.toString());
    	putRequest.setEntity(se);
    	HttpResponse response = httpClient.execute(putRequest);
    	Log.d("Status Code:", String.valueOf(response.getStatusLine().getStatusCode()));
    	if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT || 
    		response.getStatusLine().getStatusCode() == HttpStatus.SC_OK || 
    		response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
    		success = true;
    		Log.d("Status Code 2:", Boolean.toString(success));
    	} else {
    		success = false;
    		Log.e(TAG, "Failed HTTP PUT Error Code:" + response.getStatusLine().getStatusCode());
    		throw new RuntimeException("Failed : HTTP error code : "
                         + response.getStatusLine().getStatusCode());
    	}
    	Log.d("Edit Profile Parser:","success ="+ success);

    	return success;         
    }
    
    
    private JSONObject doHttpGet(String url, List<NameValuePair> params)  {
    	String jString;
    	
    	try {
    		DefaultHttpClient httpclient = new DefaultHttpClient();
    		URI uri = new URI( url + "?" + URLEncodedUtils.format( params, "utf-8"));
    		Log.d("Get URI", uri.toString());
    		HttpGet httpget = new HttpGet(uri);
    		httpget.setHeader("Accept", "application/json");
    		Log.d("Get Items", httpget.toString());
    		httpget.setHeader("Content-type", "application/json");
    		HttpResponse response = httpclient.execute(httpget);
    		HttpEntity httpEntity = response.getEntity();
            code = String.valueOf(response.getStatusLine().getStatusCode());
    		is = httpEntity.getContent();
    	} catch (Exception e) {
        	Log.v("Error adding article",e.getMessage());
    		return null;
    	}

    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
    		StringBuilder sb = new StringBuilder();
    		String line = null;
    		while ((line = reader.readLine()) != null) {
    			sb.append(line + "\n");
    		}
    		is.close();
    		jString = sb.toString();
    	} catch (Exception e) {
    		Log.e("Buffer Error", "Error converting result " + e.toString());
    		return null;
    	}
    	
    	JSONObject jObject;
    	
    	// try parse the string to a JSON object
    	try 
    	{
    		Log.d("Json String", jString);
    		jObject = new JSONObject(jString);
    	} catch (JSONException e) {
    		Log.e("JSON Parser getJSON", "Error parsing data " + e.toString());
    		jObject = null;
    	}
    	return jObject;
    }

    
    /**
     * This method will make a Profile call to the Web Server and will
     * return the "user" account values.
     * @return The JSONOBject that contains the "user" information
     */
    protected JSONObject getUserAccount() {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();       
        String url = WebAPIConstants.BASE_URL_GTDB + WebAPIConstants.ACC_PROFILE;
        params.add(new BasicNameValuePair("Authentication", "Bearer"));
        params.add(new BasicNameValuePair("access_token",WebAPIConstants.ACCESS_TOKEN));        

        JSONObject jObject = doHttpGet(url, params);
        if (jObject != null) {
        	// try parse the string to a JSON object
        	try {
        		if (Integer.parseInt(code) == HttpStatus.SC_OK) {
        			success = 1; 
        			jObject.put("success", success);
        		} else {
        			success = 0;
        			jObject.put("success", success);
        		}
        	} catch (JSONException e) {
        		Log.e("JSON Parser getJSON", "Error parsing data " + e.toString());
        	}
        }
        // return JSON Object
        return jObject;
    }
    
    protected JSONObject getInfoFromGet(String url) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();       
        params.add(new BasicNameValuePair("Authentication", "Bearer"));
        params.add(new BasicNameValuePair("access_token",WebAPIConstants.ACCESS_TOKEN));        

    	JSONObject jObject = doHttpGet(url, params);
    	return jObject;
    }
    
    // TODO: THIS METHOD IS NOT USED
    /*
    private JSONArray getJSONArrayFromUrl(String url, List<NameValuePair> params) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            //get response
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            
            is = httpEntity.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.d("JSONParser::", json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
 
        // try parse the string to a JSON object
        try {
            jArr = new JSONArray(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            jArr = null;
        }
        return jArr;
    }
    */
    
    // TODO: THIS METHOD IS NOT USED
    /*
    private JSONArray getJSONArrayCommentsREST(String url) {
    	JSONObject jObject;
    	JSONArray jArr = null;
    	
    	jObject = getJSONObject(url);
    	if (jObject != null) {
	        // try parse the string to a JSON object
	        try {
	        	JSONObject tag = jObject.getJSONObject("tag");
	        	 Log.d("Comment tag ", tag.toString());
	        	jArr = tag.getJSONArray("comments");
	        	
	        } catch (JSONException e) {
	            Log.e("JSON Parser", "Error parsing data " + e.toString());
	            jArr = null;
	        }
    	}
        // return JSON String
        return jArr;
    }
    */
    
    public JSONArray getJSONArrayForMembersUrlREST(String url) {
    	JSONObject jObject;
    	JSONArray jArr = null;
    	
    	jObject = getJSONObject(url);
    	if (jObject != null) {
	        // try parse the string to a JSON object
	        try {
	        	jArr = jObject.getJSONArray("members");
	        } catch (JSONException e) {
	            Log.e("JSON Parser", "Error parsing data " + e.toString());
	            jArr = null;
	        }
    	}
        // return JSON String
        return jArr;
    }
    
    public JSONArray getJSONArrayForTAGS_REST(String url) {
    	JSONObject jObject;
    	JSONArray jArr = null;
    	
    	jObject = getJSONObject(url);
    	if (jObject != null) {
	        // try parse the string to a JSON object
	        try {
	        	jArr = jObject.getJSONArray("tags");
	        } catch (JSONException e) {
	            Log.e("JSON Parser", "Error parsing data " + e.toString());
	            jArr = null;
	        }
    	}
        // return JSON String
        return jArr;
    }
    
    public JSONArray getJSONArrayForFriends_REST(String url, List<NameValuePair> params) {
    	JSONObject jObject;
    	JSONArray jArr = null;
    	
    	jObject = getJSONObject(url, params);
    	if (jObject != null) {
	        // try parse the string to a JSON object
	        try {
	        	jArr = jObject.getJSONArray("friends");
	        } catch (JSONException e) {
	            Log.e("JSON Parser", "Error parsing data " + e.toString());
	            jArr = null;
	        }
    	}
        return jArr;
    }

    public JSONArray getJSONArrayForGroups_REST(String url) {
    	JSONObject jObject;
    	JSONArray jArr = null;
    	
    	jObject = getJSONObject(url);
    	if (jObject != null) {
	        // try parse the string to a JSON object
	        try {
	        	jArr = jObject.getJSONArray("groups");
	        } catch (JSONException e) {
	            Log.e("JSON Parser", "Error parsing data " + e.toString());
	        }
    	}
        // return JSON String
        return jArr;
    }

    public JSONArray getJSONArrayForOwnerGroups_REST(String url) {
    	JSONObject jObject;
    	JSONArray jArr = null;
    	
    	jObject = getJSONObject(url);
    	if (jObject != null) {
            // try parse the string to a JSON object
            try {
            	JSONObject groups = jObject.getJSONObject("groups");
            	
            	jArr = groups.getJSONArray("owner_of");
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
    	}
        // return JSON String
        return jArr;
    }
    
    public JSONArray getJSONArrayForMemberGroups_REST(String url) {
    	JSONObject jObject;
    	JSONArray jArr = null;
    	
    	jObject = getJSONObject(url);
    	if (jObject != null) {
            // try parse the string to a JSON object
            try {
            	JSONObject groups = jObject.getJSONObject("groups");
            	
            	jArr = groups.getJSONArray("member_of");
            	Log.d("JSONParser:", jArr.toString());
            	
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
    	}
        // return JSON String
        return jArr;
 
    }
    
    // TODO: NOT USED
    /*
    private JSONArray getJSONArrayGroupsFriends_REST(String url) {
    	JSONObject jObject;
    	JSONArray jArr = null;
    	
    	jObject = getJSONObject(url);
    	if (jObject != null) {
    	    // try parse the string to a JSON object
    	    try {
    	    	jArr = jObject.getJSONArray("users");
    	    	Log.d("JSONParser:", jArr.toString());
    	    	
    	    } catch (JSONException e) {
    	        Log.e("JSON Parser", "Error parsing data " + e.toString());
    	    }
    	}
        // return JSON String
        return jArr;
    }
    */


	public JSONObject getJSONObject(String url) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("Authentication", "Bearer"));
        params.add(new BasicNameValuePair("access_token",WebAPIConstants.ACCESS_TOKEN));

        return getJSONObject_private(url, params);
	}

    
	public JSONObject getJSONObject(String url, List<NameValuePair> params) {
        params.add(new BasicNameValuePair("Authentication", "Bearer"));
        params.add(new BasicNameValuePair("access_token",WebAPIConstants.ACCESS_TOKEN));

        return getJSONObject_private(url, params);
	}
    
    
    
    /**
     * This method will perform the input HTTP Request and return the JSON
     * object that is returned.
     * @param url
     * @param params
     * @return
     */
	private JSONObject getJSONObject_private(String url, List<NameValuePair> params) {
	    // Making HTTP request
		try {
	        DefaultHttpClient httpclient = new DefaultHttpClient();
	        URI uri = new URI(url + "?" + URLEncodedUtils.format( params, "utf-8"));
        	Log.d("Get URI", uri.toString());
        	HttpGet httpget = new HttpGet(uri);
	        httpget.setHeader("Accept", "application/json");
	        Log.d("Get Items", httpget.toString());
	        httpget.setHeader("Content-type", "application/json");
	        HttpResponse response = httpclient.execute(httpget);
	        HttpEntity httpEntity = response.getEntity();
	        is = httpEntity.getContent();
		} catch (Exception e) {
			Log.v("Error adding article",e.getMessage());
		}
		try {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
	        StringBuilder sb = new StringBuilder();
	        String line = null;
	        while ((line = reader.readLine()) != null) {
	            sb.append(line + "\n");
	        }
	        is.close();
	        json = sb.toString();
	        //Log.d("JSONParser", json);
	    } catch (Exception e) {
	        Log.e("Buffer Error", "Error converting result " + e.toString());
	    }

		JSONObject jsonObj;
		
	    // try parse the string to a JSON object
	    try {
	    	jsonObj = new JSONObject(json);
	    } catch (JSONException e) {
	        Log.e("JSON Parser", "Error parsing data " + e.toString());
	        jsonObj = null;
	    }
	    // return JSON String
	    return jsonObj;
	}
    
    class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";
        public String getMethod() { return METHOD_NAME; }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }
        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }
        public HttpDeleteWithBody() { super(); }
    }
    
    
    
    
    
	/****************************************************************************************
	 * RELATION METHODS and DEFINITIONS 
	 ****************************************************************************************/

	public boolean sendRelationToServerDB(boolean doDelete, String method, String entityClass, long fromID, long toID) {
		String url = String.format(WebAPIConstants.ACC_FORMAT_RELATION, WebAPIConstants.BASE_URL_GTDB, WebAPIConstants.ACCESS_TOKEN);
		JSONObject param = new JSONObject();
		JSONObject attributes = new JSONObject();
		try {
			attributes.put("entityClass", entityClass);
			attributes.put("method", method);
			attributes.put("toEntity", toID);
			attributes.put("fromEntity", fromID);
			param.put("relation", attributes);
			
			int status;
			if (doDelete) {
				status = sendDeleteToServer(url, param);
				// If the status is created of deleted then return success
				if (status == HttpStatus.SC_NO_CONTENT)
					return true;
			} else {
				status = sendPostToServer(url, param);
				// If the status is created of deleted then return success
				if (status == HttpStatus.SC_CREATED)
					return true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
    
	/**
	 * Perform the POST command for the input URL and Params. Return the code
	 * returned from the server.
	 * @param url
	 * @param params
	 * @return
	 */
    private int sendPostToServer(String url, JSONObject params) {
    	int code;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            
            StringEntity se = new StringEntity( params.toString()); 
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            
            httpPost.setEntity(se);

            HttpResponse response = httpClient.execute(httpPost);
            code = response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            e.printStackTrace();
            return HttpStatus.SC_EXPECTATION_FAILED;
        }
        return code;
    }

	/**
	 * Perform the POST command for the input URL and Params. Return the code
	 * returned from the server.
	 * @param url
	 * @param params
	 * @return
	 */
    private int sendDeleteToServer(String url, JSONObject params) {
    	int code;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpDelete htpDelete = new HttpDelete(url); 
            
            StringEntity se = new StringEntity( params.toString()); 
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            
            HttpDeleteWithBody httpDeleteWithBody = new HttpDeleteWithBody(url);
            httpDeleteWithBody.setEntity(se);

            HttpResponse response = httpClient.execute(httpDeleteWithBody);
            code = response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            e.printStackTrace();
            return HttpStatus.SC_EXPECTATION_FAILED;
        }
        return code;
    }

    
}
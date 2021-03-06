package com.virat.hoteltonight.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Parses JSON that is returned from requests made to the Google Place API.
 * @author Virat
 *
 */
public class PlacesJSONParser {
	private static final String TAG = "PlacesJSONParser";
	
	// Constants for Google Place requests
	private static final String API_KEY = "AIzaSyCnwc1ZryI2LuYzHO7X4uU8acjFcYmUA9E";
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String TYPE_DETAILS_SEARCH = "/details";
	private static final String OUT_JSON = "/json";
	
	
	// This HashMap holds a key/value pair that maps
	// a Google Place's name to its Google Details reference id.
	// The key is the name of the Place
	// The value is the unique reference id of the Place
	private Map<String, String> place_map;
	
	/**
	 * Default ctor initializes an empty Google Place HashMap
	 */
	public PlacesJSONParser() {
		place_map = new HashMap<String, String>();
	}
	
	/**
	 * Populates the suggestions of the destination search AutoCompleteTextView
	 * with Google Places Autocomplete choices.
	 */
	public ArrayList<String> autoComplete(String input) {
		ArrayList<String> resultList = null;

		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();

		try {
			StringBuilder sb = new StringBuilder(PLACES_API_BASE
					+ TYPE_AUTOCOMPLETE + OUT_JSON);
			sb.append("?sensor=false&key=" + API_KEY);
			sb.append("&radius=1000");
			sb.append("&input=" + URLEncoder.encode(input, "utf8"));

			URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());

			// Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error processing Places API URL", e);
			return resultList;
		} catch (IOException e) {
			Log.e(TAG, "Error connecting to Places API", e);
			return resultList;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		try {
			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

			// Extract the Place descriptions from the results
			resultList = new ArrayList<String>(predsJsonArray.length());
			for (int i = 0; i < predsJsonArray.length(); i++) {
				
				// Add the Google Place suggestions to an ArrayList
				resultList.add(predsJsonArray
						.getJSONObject(i)
						.getString(
						"description"));
				
				// Put the Google Place suggestions in a Map
				place_map.put(predsJsonArray.getJSONObject(i)
						.getString("description"), predsJsonArray
						.getJSONObject(i).getString("reference"));
			}
		} catch (JSONException e) {
			Log.e(TAG, "Cannot process JSON results", e);
		}
		Log.d(TAG, "Places resultList is: " + resultList);
		return resultList;
	}
	
	/**
	 * Returns a HashMap that holds a mapping of a 
	 * Google Place's name and reference id
	 */
	public Map<String, String> getPlaceSuggestionsMap() {
		if (place_map == null) {
			place_map = new HashMap<String, String>();
		}
		
		return place_map;
	}
	
	/**
	 * Executes and parses the result of a Google Place Details search request
	 * @param reference
	 * @return the latitude/longitude of a location
	 */
	public Place getPlaceDetails(String reference) {
		Place place = new Place();
		String name = "";
		String formatted_address = "";
		String formatted_phone_number = "";
		double rating = 0;
		
		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		
		try {
			StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_DETAILS_SEARCH + OUT_JSON);
			sb.append("?sensor=false&key=" + API_KEY);
	        sb.append("&reference=" + reference.replaceAll("\\s+",""));
	        
	        URL url = new URL(sb.toString());
	        conn = (HttpURLConnection)url.openConnection();
	        InputStreamReader in = new InputStreamReader(conn.getInputStream());
	        
	        // Load the results into a StringBuilder
	        int read;
	        char[] buff = new char[1024];
	        while ((read = in.read(buff)) != -1) {
	        	jsonResults.append(buff, 0, read);
	        }
		} catch (MalformedURLException e) {
	        Log.e(TAG, "Error processing Places API URL", e);
	        return null;
	    } catch (IOException e) {
	        Log.e(TAG, "Error connecting to Places API", e);
	        return null;
	    } finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }
		
		try {
			// Create a JSON object from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONObject resultJsonObj = jsonObj.getJSONObject("result");
			Log.d(TAG, "getDetailsLatLng resultJsonObj: " + resultJsonObj);
			
			try {
				name = resultJsonObj.getString("name");
				place.setName(name);
			} catch (JSONException e) {
				// No name found
				name = "";
			}
			try {
				formatted_address = resultJsonObj.getString("formatted_address");
				place.setAddress(formatted_address);
			} catch (JSONException e) {
				// No address found
				formatted_address = "";
			}
			
			try {
				formatted_phone_number = resultJsonObj.getString("formatted_phone_number");
				place.setPhoneNumber(formatted_phone_number);
			} catch (JSONException e) {
				// No phone number found
				formatted_phone_number = "";
			}
			
			try {
				rating = resultJsonObj.getDouble("rating");
				place.setRating(rating);
			} catch (JSONException e) {
				// No rating found
				rating = 0;
			}
			
	    } catch (JSONException e) {
	        Log.e(TAG, "Cannot process JSON results", e);
	    }
		
		return place;
	}
}

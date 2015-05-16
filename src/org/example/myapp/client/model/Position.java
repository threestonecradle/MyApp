package org.example.myapp.client.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class Position {
	double l1;
	double l2;
	
	
	public double getL1() {
		return l1;
	}


	public void setL1(double l1) {
		this.l1 = l1;
	}


	public double getL2() {
		return l2;
	}


	public void setL2(double l2) {
		this.l2 = l2;
	}


	public static Position paser_str(String str) {
		JSONTokener jsonParser = new JSONTokener(str);
		try {
			JSONObject ret = (JSONObject)(jsonParser.nextValue());
			JSONObject post_json_obj = ret.getJSONObject("content");
			Position pos = new Position();
			pos.setL1(Double.parseDouble(post_json_obj.getString("lat"))/100);
			pos.setL2(Double.parseDouble(post_json_obj.getString("lon"))/100);
					
			return pos;	
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String get_device_id(String str) {
		try {
			JSONTokener jsonParser = new JSONTokener(str);
			JSONObject ret = (JSONObject)(jsonParser.nextValue());
			JSONArray doc_list = ret.getJSONArray("list");
			int length = doc_list.length();
			JSONObject oj_tmp = doc_list.getJSONObject(0);
			String ss = oj_tmp.getString("imeiid");
			return ss;
		} catch (JSONException  e) {
			return "";
		}
	}
}

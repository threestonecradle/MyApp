package org.example.myapp.client.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.example.myapp.client.model.Doctor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

/**
 * 帖子实体类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class PostList {



	public final static int CATALOG_ASK = 1;
	public final static int CATALOG_SHARE = 2;
	public final static int CATALOG_OTHER = 3;
	public final static int CATALOG_JOB = 4;
	public final static int CATALOG_SITE = 5;
	
	private int pageSize;
	private int postCount;
	private List<Post> postlist = new ArrayList<Post>();
	
	public int getPageSize() {
		return pageSize;
	}
	public int getPostCount() {
		return postCount;
	}
	public List<Post> getPostlist() {
		return postlist;
	}

	public static PostList parse(String str) {
		PostList postlist = new PostList();
		Post post = null;
		try {
			JSONTokener jsonParser = new JSONTokener(str);
			JSONObject ret = (JSONObject)(jsonParser.nextValue());
			JSONArray doc_list = ret.getJSONArray("list");
			int length = doc_list.length();
			for(int i = 0; i < length; i++) {
				JSONObject oj_tmp = doc_list.getJSONObject(i);
				post = Post.jiexi(oj_tmp);

				if (post != null) {
					postlist.getPostlist().add(post);
				}			
			}
		} catch (JSONException  e) {
			postlist.getPostlist().clear();
		}
		return postlist;
	}
}

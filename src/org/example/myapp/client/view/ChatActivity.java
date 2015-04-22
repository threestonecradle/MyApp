package org.example.myapp.client.view;
import java.util.ArrayList;
import java.util.HashMap;

import org.example.myapp.R;
import org.example.myapp.client.network.YQClient;
import org.example.myapp.common.MessageOne;
import org.example.myapp.common.MyTime;
import org.example.myapp.common.ReturnObj;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity {
	EditText et_input;
	
	private String chatContent;//消息内容
	ListView chatListView;
	
	public static ChatAdapter chat_adap;//好友列表的adapter
	private static HashMap chatEntityList = new HashMap<Long,ArrayList<MessageOne>>();
	ArrayList<MessageOne> curr_char_list;
	private static ChatActivity entity;
	
	private static Long chatAccount;
	private static String charName;
	private static int charOnline;
	private String major;
	
	private static YQClient new_http_client = null;
	BroadcastMainChat  broadcastMain;
	
	public static int[] avatar=new int[]{R.drawable.avatar_default,R.drawable.h001,R.drawable.h002,R.drawable.h003,
			R.drawable.h004,R.drawable.h005,R.drawable.h006,R.drawable.group_avatar};
	
	
	public static void jiexi(String str) {
		try {
			JSONTokener jsonParser = new JSONTokener(str);
			JSONObject ret = (JSONObject)(jsonParser.nextValue());
			JSONArray doc_list = ret.getJSONArray("list");
			int length = doc_list.length();
			ArrayList<MessageOne> list_tmp = (ArrayList<MessageOne>)chatEntityList.get(chatAccount);
			for(int i = 0; i < length; i++) {
				JSONObject oj_tmp = doc_list.getJSONObject(i);
				MessageOne one_msg = new MessageOne();
				one_msg.setContent(oj_tmp.getString("content"));
				one_msg.setLeft(true);
				
				one_msg.setReceiver(MainActivity.myUser.getId());
				one_msg.setReceiver_name(MainActivity.myUser.getName());
				
				
				one_msg.setSender(chatAccount);
				one_msg.setSender_name(charName);
				one_msg.setReceiver_role(0);
				one_msg.setSender_role(1);
				one_msg.setTime(oj_tmp.getString("sendtime"));
			
				list_tmp.add(one_msg);
			}
			chatEntityList.put(chatAccount, list_tmp);
		} catch (JSONException  e) {
			Log.i("exception wangbo", "获取医生列表失败! " + e.getMessage()); 
			//Toast.makeText(BuddyActivity.this, "获取医生列表失败! " + e.getMessage(), Toast.LENGTH_SHORT).show();	
		}
	}
	
	public static void updateCharList() {
		if (new_http_client != null) {
			ReturnObj obj = new_http_client.get_user_doc_msg_toread_detail_list(MainActivity.myUser.getId(), chatAccount);
	
			if (obj.getRet_code() == 0) {
				jiexi(obj.getOrg_str());
			}
		}
	}
	
	
	protected void onStop() {
		
		super.onStop();
	}
	
	
	protected void onResume() {
		
		Intent intent = new Intent();
		intent.setClass(this, ChatService.class);
	    startService(intent);
	    
	    broadcastMain = new BroadcastMainChat();
	    IntentFilter filter = new IntentFilter();
	    filter.addAction( ChatService.BROADCASTACTION );
	    registerReceiver( broadcastMain, filter);
	    chat_adap.notifyDataSetChanged();
		super.onResume();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("debug wangbo begin", "onCreate  " + MyTime.geTime());
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_chat);
		
		if (new_http_client == null) {
			new_http_client = new YQClient(true);
		}
		//设置top面板信息
		chatAccount=getIntent().getLongExtra("tel", 0);
		charName=getIntent().getStringExtra("name");
		charOnline = getIntent().getIntExtra("isonline", 0);
		major = getIntent().getStringExtra("major");
	
		
		TextView name_tv=(TextView) findViewById(R.id.chat_top_name);
		name_tv.setText(charName);
		
		TextView tel_tv=(TextView) findViewById(R.id.chat_top_tel);
		tel_tv.setText(Long.toString(chatAccount));
		
		TextView isOnline_tv =(TextView) findViewById(R.id.chat_top_isonline);
		if (charOnline == 1) {
			isOnline_tv.setText("在线");
		} else {
			isOnline_tv.setText("离线");
		}
		
		chatListView=(ListView) findViewById(R.id.lv_chat);
		curr_char_list = (ArrayList<MessageOne>)chatEntityList.get(chatAccount);
		if (curr_char_list == null) {
			curr_char_list = new ArrayList<MessageOne>();
			chatEntityList.put(chatAccount, curr_char_list);
		}
		updateCharList();
		curr_char_list = (ArrayList<MessageOne>)chatEntityList.get(chatAccount);
		chat_adap = new ChatAdapter(this, curr_char_list);
		
		chatListView.setAdapter(chat_adap);
		entity = this;

		findViewById(R.id.ib_send).setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//得到输入的数据，并清空EditText
				et_input=(EditText) findViewById(R.id.et_input);
				chatContent=et_input.getText().toString();
				
				et_input.setText("");
				
				MessageOne one_mes = new MessageOne();
				one_mes.setContent(chatContent);
				one_mes.setLeft(false);
				
				one_mes.setReceiver(chatAccount);
				one_mes.setReceiver_name(charName);
				
				one_mes.setSender(MainActivity.myUser.getId());
				one_mes.setSender_name(MainActivity.myUser.getName());
				one_mes.setReceiver_role(1);
				one_mes.setSender_role(0);
				one_mes.setTime(MyTime.geTime());
				//更新聊天内容
				updateChatView(one_mes);
				
				
				ReturnObj obj = MainActivity.client_in_strict_mode.sendOneMessageToDoc(one_mes);
				if (obj.getRet_code() != 0) {
					Toast.makeText(ChatActivity.this, "发送消息失败 " + obj.getMsg(), Toast.LENGTH_SHORT).show();	
				}
			
			}
		});
	}
	
	protected void onPause() {
		String place = "[" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + Thread.currentThread().getStackTrace()[2].getLineNumber()+"] ";
		Log.i("wangbo debug", place + "  onpause time:" +  MyTime.geTime());
		
		Intent intent = new Intent();
		intent.setClass(this, ChatService.class);
		stopService(intent);
		unregisterReceiver(broadcastMain);  
		super.onStop();
	}
	
	public void updateChatView(MessageOne mes){
		curr_char_list.add(mes);
		chat_adap.notifyDataSetChanged();
	}
	
	
	public class BroadcastMainChat extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("wangbo debug", "handleMessage time:" + MyTime.geTime());
			curr_char_list = (ArrayList<MessageOne>)chatEntityList.get(chatAccount);
			chat_adap.notifyDataSetChanged();
		}
	} 
	
}

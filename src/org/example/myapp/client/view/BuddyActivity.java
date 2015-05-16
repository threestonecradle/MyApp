package org.example.myapp.client.view;

import java.util.ArrayList;
import java.util.List;

import org.example.myapp.R;
import org.example.myapp.client.model.Doctor;
import org.example.myapp.client.network.YQClient;
import org.example.myapp.common.MyTime;
import org.example.myapp.common.ReturnObj;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BuddyActivity extends Activity {
	BroadcastMain  broadcastMain;
	private ListView listview_my_docs = null;
	private ListView listview_all_docs = null;
	private Button frame_btn_my_docs = null;
	private Button frame_btn_all_docs = null;
	public String buddyStr = ""; 
	public static List<Doctor> buddyEntityList = new ArrayList<Doctor>();//�����б�
	public static BuddyAdapter ba = null;//�����б��adapter	
	
	public static YQClient new_http_client = null;

	public final static int CATALOG_ALL = 1;
	public final static int CATALOG_MY = 2;
	private int curDocCatalog = CATALOG_MY;
	
	Doctor temp;

	public void paser_doc_list (final int catalog) {
		if (new_http_client == null) {
			return;
		}
		ReturnObj ret_obj = null;
		if (catalog == CATALOG_ALL)
			ret_obj =  new_http_client.get_all_doc_list(0);
		else
			ret_obj =  new_http_client.get_user_doc_list(MainActivity.myUser.getId());
		
		if (null != ret_obj && ret_obj.getRet_code() == 0) {
			buddyStr = ret_obj.getOrg_str();
			buddyEntityList = Doctor.paser_str_to_objlist(buddyStr);
		} else {
			buddyStr = "";
		}
	}
	

	public static void get_user_doc_msg_toread_read() {
		
		ReturnObj ret_obj =  new_http_client.get_user_doc_msg_toread_list(MainActivity.myUser.getId());
		
		if (ret_obj.getRet_code() == 0) {
			for(int idx=0;idx<buddyEntityList.size();idx++){
				Doctor doc = buddyEntityList.get(idx);
				doc.setMes_to_read(0);
				buddyEntityList.set(idx, doc);
			}
			
			try {
				JSONTokener jsonParser = new JSONTokener(ret_obj.getOrg_str());
				JSONObject ret = (JSONObject)(jsonParser.nextValue());
				JSONArray doc_list = ret.getJSONArray("list");
				int length = doc_list.length();
				
				for(int i = 0; i < length; i++) {
					JSONObject oj_tmp = doc_list.getJSONObject(i);
					
					int count = Integer.parseInt(oj_tmp.getString("unreadcnt"));
					Long tel = Long.parseLong(oj_tmp.getString("sendertel"));
					
			      	String place = "[" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + Thread.currentThread().getStackTrace()[2].getLineNumber()+"] ";
            		Log.i("wangbo debug", place + "doc: "+ Long.toString(tel) + " msg_to_red:" + Integer.toString(count) + " time: " +  MyTime.geTime());
                	
					
					for(int idx=0;idx<buddyEntityList.size();idx++){
						if((buddyEntityList.get(idx).getDoc_id()).equals(tel) == true){
							Doctor doc = buddyEntityList.get(idx);
							doc.setMes_to_read(count);
							buddyEntityList.set(idx, doc);
						}
					}
				}
				//ð������ѡ����Ϣ���ķ�ǰ�棻
				for (int i = 0; i < buddyEntityList.size() - 1; i++) {
					int max = buddyEntityList.get(i).getMes_to_read();
					int max_idx = i;
					for (int j = i + 1; j < buddyEntityList.size(); j ++ ) {
						int cur = buddyEntityList.get(j).getMes_to_read();
						if (cur > max){
							max = cur;
							max_idx = j;
						}
						
					}
					
					if (max == 0) {
						break;
					}
					if (max_idx != i) {
						Doctor doc_tmp = buddyEntityList.get(max_idx);
						buddyEntityList.set(max_idx, buddyEntityList.get(i));
						buddyEntityList.set(i, doc_tmp);	
					}
				}
				
			} catch (Exception e) {
				//Toast.makeText(BuddyActivity.this, "����δ����Ϣʧ��! " + e.getMessage(), Toast.LENGTH_SHORT).show();	
			}
		} else {
			//Toast.makeText(BuddyActivity.this, "��ȡδ��δ����Ϣʧ��! " + ret_obj.getMsg(), Toast.LENGTH_SHORT).show();	
		}
	}
	
	public void updateInfo() {
		TextView tel = (TextView) findViewById(R.id.buddy_top_tel);
		TextView name = (TextView) findViewById(R.id.buddy_top_name);
		
		
		name.setText(MainActivity.myUser.getName());
		tel.setText(Long.toString(MainActivity.myUser.getId()));
	}
	
	
	protected void onResume() {
		updateInfo();
		LoadDocList(curDocCatalog);
		if (new_http_client != null) {
			get_user_doc_msg_toread_read();
		}
		
		String place = "[" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + Thread.currentThread().getStackTrace()[2].getLineNumber()+"] ";
		Log.i("wangbo debug", place + "  onresume time:" +  MyTime.geTime());
		
		Intent intent = new Intent();
		intent.setClass(this, BuddyService.class);
	    startService(intent);
	    
	    broadcastMain = new BroadcastMain();
	    IntentFilter filter = new IntentFilter();
	    filter.addAction( BuddyService.BROADCASTACTION );
	    registerReceiver( broadcastMain, filter);
	    ba.notifyDataSetChanged();
		super.onResume();	
	}
	protected void onPause() {
		String place = "[" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName() + ":" + Thread.currentThread().getStackTrace()[2].getLineNumber()+"] ";
		Log.i("wangbo debug", place + "  onpause time:" +  MyTime.geTime());
		
		Intent intent = new Intent();
		intent.setClass(this, BuddyService.class);
		stopService(intent);
		unregisterReceiver(broadcastMain);  
		super.onPause();
	}
	protected void onStop() {
	
		
		super.onStop();
	}
	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_buddy);
		ManageActivity.addActiviy("buddyActivity", BuddyActivity.this);
		if (new_http_client == null) {
			new_http_client = new YQClient(true);
		}
		
		initFrameButton();
		initFrameListView();
		
		
		LoadDocList(curDocCatalog);
	}
	
	private void LoadDocList(int catalog)
	{
		paser_doc_list(catalog);
		ListView curlv =CATALOG_ALL == curDocCatalog?listview_all_docs:listview_my_docs;
		ba = new BuddyAdapter(this, buddyEntityList);
		curlv.setAdapter(ba);
	}
	
	
	private void initFrameButton()
	{
		frame_btn_my_docs = (Button) findViewById(R.id.frame_btn_my_docs);
        frame_btn_all_docs = (Button) findViewById(R.id.frame_btn_all_docs);
        
        // ������ѡ����
        frame_btn_my_docs.setEnabled(false);
        
        frame_btn_my_docs.setOnClickListener(frameDocBtnClick(
        		frame_btn_my_docs, CATALOG_MY));
        frame_btn_all_docs.setOnClickListener(frameDocBtnClick(
        		frame_btn_all_docs, CATALOG_ALL));
        
	}
	
	private void initFrameListView()
	{
		listview_all_docs = (ListView) findViewById(R.id.listview_all_docs);
		setListViewListener(listview_all_docs);
		listview_my_docs = (ListView) findViewById(R.id.listview_my_docs);
		setListViewListener(listview_my_docs);
	}
	
	private View.OnClickListener frameDocBtnClick(final Button btn,
			final int catalog) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				if (btn == frame_btn_my_docs) {
					frame_btn_my_docs.setEnabled(false);
					frame_btn_all_docs.setEnabled(true);
					listview_my_docs.setVisibility(View.VISIBLE);
					listview_all_docs.setVisibility(View.GONE);
				} else {
					frame_btn_my_docs.setEnabled(true);
					frame_btn_all_docs.setEnabled(false);
					listview_my_docs.setVisibility(View.GONE);
					listview_all_docs.setVisibility(View.VISIBLE);
				}
//				if (btn == frame_btn_all_docs) {
//					frame_btn_all_docs.setEnabled(false);
//				} else {
//					frame_btn_all_docs.setEnabled(true);
//				}

				curDocCatalog = catalog;
				LoadDocList(curDocCatalog);
//				
//				// �������б�
//				if (btn == frame_btn_my_docs) {
//					
//					LoadDocList(listview_my_docs);
//					
//				} else {
//					
//					LoadDocList(listview_all_docs);
//				}
			}
		};
	}
	
	
	private void setListViewListener(final ListView listview) {
		listview.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> a, View v, int position,long l) {
				temp= (Doctor) listview.getItemAtPosition(position);
				//������ҳ��
				Intent intent=new Intent(BuddyActivity.this,DoctorInfoActivity.class);
				intent.putExtra("doccatalog", curDocCatalog);
				intent.putExtra("tel", temp.getDoc_id());
				intent.putExtra("name",temp.getName());
				intent.putExtra("major", temp.getMajor());
				intent.putExtra("isonline", temp.getIsOnline());
				startActivity(intent);
			}
        });
	}
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case 0:
			//������ҳ��
			Intent intent=new Intent(BuddyActivity.this,ChatActivity.class);
			intent.putExtra("tel", temp.getDoc_id());
			intent.putExtra("name",temp.getName());
			intent.putExtra("major", temp.getMajor());
			intent.putExtra("isonline", temp.getIsOnline());
			startActivity(intent);
			break;
		case 1:
			//�����������һ��ɾ�����ѵİ�
			ReturnObj obj = MainActivity.client_in_strict_mode.del_user_doc(MainActivity.myUser.getId(), temp.getDoc_id());
			if (obj.getRet_code() != 0) {
				Toast.makeText(BuddyActivity.this, "ɾ��ҽ��ʧ��! " + obj.getMsg(), Toast.LENGTH_SHORT).show();
			}
			//ɾ�������б��еĸú���
			for(int i=0;i<buddyEntityList.size();i++){
				if((buddyEntityList.get(i).getDoc_id())==temp.getDoc_id()){
					buddyEntityList.remove(i);
				}
			}
			listview_all_docs = (ListView) findViewById(R.id.listview);
			ba=new BuddyAdapter(this,buddyEntityList);
			listview_all_docs.setAdapter(ba);
			break;
		case 2:
			int ret_code = display_doc_info();
			if (ret_code != 0) {
				Toast.makeText(BuddyActivity.this, "��ȡҽ����ϸ��Ϣʧ��", Toast.LENGTH_SHORT).show();
			}
			
			break;
		case 3:
			//�½�һ��ҳ�棻todo
			Intent intent_yuyue = new Intent(BuddyActivity.this,DoctorOrderActivity.class);
			intent_yuyue.putExtra("tel", temp.getDoc_id());
			intent_yuyue.putExtra("name",temp.getName());
			intent_yuyue.putExtra("major", temp.getMajor());
			intent_yuyue.putExtra("isonline", temp.getIsOnline());
			startActivity(intent_yuyue);
	
			break;
		}
		return super.onContextItemSelected(item);
	}

	private int display_doc_info() {
		
		final EditText et = new EditText(BuddyActivity.this);
		
		Doctor new_temp = Doctor.paser_str_to_obj(MainActivity.client_in_strict_mode.get_doc_info(temp.getDoc_id()));
		if (new_temp != null) {
			et.setKeyListener(null);
			et.setText("�绰 : " + Long.toString(new_temp.getDoc_id()) + 
					"\nҽԺ: " + new_temp.getHospital() + 
					"\n����: " + new_temp.getDepartment() + 
					"\nְ��: " + new_temp.getJob() + 
					"\n����: " + new_temp.getMajor() + 
					"\n�Ա�: " + new_temp.getSex() + 
					"\n����: " + new_temp.getAge() + 
					"\nemail: " + new_temp.getMail());
			AlertDialog.Builder dialog = new AlertDialog.Builder(BuddyActivity.this);
			dialog.setTitle("ҽ��" + new_temp.getName()+"��ϸ��Ϣ");
			dialog.setView(et);	
			dialog.setNegativeButton("�ر�", null);
			dialog.show();	
		} else {
			return -1;

		}
		return 0;
	}

	private List<Doctor> jieXi(String str) {
		buddyEntityList.clear();
		try {
			JSONTokener jsonParser = new JSONTokener(str);
			JSONObject ret = (JSONObject)(jsonParser.nextValue());
			JSONArray doc_list = ret.getJSONArray("list");
			int length = doc_list.length();
			for(int i = 0; i < length; i++) {
				JSONObject oj_tmp = doc_list.getJSONObject(i);
				Doctor doc_tmp = new Doctor();
				doc_tmp.setName(oj_tmp.getString("name"));
				Log.i("wangbo debug", "current_item: " + Integer.toString(i) + " name:" + oj_tmp.getString("name"));
				doc_tmp.setSex(oj_tmp.getString("sex"));
				doc_tmp.setDoc_id((Long.parseLong(oj_tmp.getString("tel"))));
				doc_tmp.setHospital(oj_tmp.getString("hospital"));
				doc_tmp.setDepartment(oj_tmp.getString("department"));
				doc_tmp.setJob(oj_tmp.getString("job"));
				doc_tmp.setMajor(oj_tmp.getString("attending"));
				
				doc_tmp.setMail(oj_tmp.getString("email"));
				buddyEntityList.add(doc_tmp);
			}
		   
		} catch (JSONException  e) {
			buddyEntityList.clear();
		}
		return buddyEntityList;
	}
	
	public class BroadcastMain extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("wangbo debug", "handleMessage time:" + MyTime.geTime());
			ba.notifyDataSetChanged();
		}
	}  
}

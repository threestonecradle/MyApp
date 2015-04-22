package org.example.myapp.client.view;

import org.example.myapp.R;
import org.example.myapp.client.model.User;
import org.example.myapp.client.network.YQClient;
import org.example.myapp.common.*;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	public static String userInfo;
	EditText accountEt,passwordEt;
	CheckBox passCb;
	Dialog dialog;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.activity_login);
	    
	    MainActivity.client_in_strict_mode = new YQClient();
	   
	    
	    SharedPreferences mySharedPreferences= getSharedPreferences(MyAppConfig.SHARE_PREFERENCE_FILE, 
	    		Activity.MODE_PRIVATE); 
	    String name =mySharedPreferences.getString("current_login_tel", ""); 
	    String passwd =mySharedPreferences.getString("current_login_pass", ""); 
	    String checkbox_true =mySharedPreferences.getString("current_login_ch", "");
	    
	    accountEt=(EditText) findViewById(R.id.et_account);
	    passwordEt=(EditText) findViewById(R.id.et_password);
	    passCb=(CheckBox) findViewById(R.id.cb_pass);
	    
	    if (checkbox_true.equals("1")) {
	    	passCb.setChecked(true);
	    }
	    
	    if (!name.equals("")) {
	    	accountEt.setText(name);
	    }
	    if (!passwd.equals("")) {
	    	passwordEt.setText(passwd);
	    }
	    Button btnLogin=(Button) findViewById(R.id.btn_login);
	    
	    
	    btnLogin.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				try {
				
					
					if(accountEt.getText().equals("") || passwordEt.getText().equals("")) { 
						Toast.makeText(LoginActivity.this, "账号或密码不能为空！", Toast.LENGTH_SHORT).show();
					} else if (Long.toString(Long.parseLong(accountEt.getText().toString())).length() != MyAppConfig.MOBILE_LEN){  
						Toast.makeText(LoginActivity.this, "账户为手机号，" + MyAppConfig.MOBILE_LEN + "为数字！", Toast.LENGTH_SHORT).show();
					}	else{
						SharedPreferences mySharedPreferences= getSharedPreferences(MyAppConfig.SHARE_PREFERENCE_FILE, 
					    		Activity.MODE_PRIVATE); 
						SharedPreferences.Editor editor = mySharedPreferences.edit(); 
						
					
						
						if (passCb.isChecked()) {
							editor.putString("current_login_ch", "1");
							editor.putString("current_login_tel", accountEt.getText().toString()); 
							editor.putString("current_login_pass", passwordEt.getText().toString()); 
							editor.commit(); 
						} else {
							editor.putString("current_login_tel", ""); 
							editor.putString("current_login_pass", ""); 
							editor.putString("current_login_ch", "0");
							editor.commit(); 
						}
						dialog = ProgressDialog.show(LoginActivity.this, null,  "正在登陆中 …", true, true);
						handler.post(new Runnable(){
							public void run() {
								ReturnObj obj = login(Long.parseLong(accountEt.getText().toString()), passwordEt.getText().toString());
								if(obj.getRet_code() == 0){
									Message m=new Message();  
		                            m.what=1;  
		                            handler.sendMessage(m);
		                            ManageActivity.addActiviy("loginActivity", LoginActivity.this);
		                            //转到主界面
									startActivity(new Intent(LoginActivity.this, MainActivity.class));
									
								} else {
									Toast.makeText(LoginActivity.this, "登陆失败! " + obj.getMsg(), Toast.LENGTH_SHORT).show();
									dialog.dismiss();
								}
							}
						});
					}
				} catch (Exception e) {
					
					Toast.makeText(LoginActivity.this, "登陆失败! ", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
				}
			}
	    });
	    findViewById(R.id.btn_register).setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
			}
	    });
	   
	}
	
	ReturnObj login(Long a, String p){
		User user=new User();
		user.setId(a);
		user.setPassword(p);
		user.setOperation("login");
		ReturnObj b = MainActivity.client_in_strict_mode.sendLoginInfo(user);
		return b;
	}
	
	private Handler handler=new Handler(){  
        public void handleMessage(Message msg){  
            switch(msg.what){  
            case 1:
                dialog.dismiss();  
                break;  
            }  
        }  
    }; 

}

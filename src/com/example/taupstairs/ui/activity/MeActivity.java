package com.example.taupstairs.ui.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import com.example.taupstairs.R;
import com.example.taupstairs.adapter.PersonVariableDataAdapter;
import com.example.taupstairs.bean.Person;
import com.example.taupstairs.bean.Task;
import com.example.taupstairs.logic.ItaActivity;
import com.example.taupstairs.logic.MainService;
import com.example.taupstairs.services.PersonService;
import com.example.taupstairs.string.IntentString;
import com.example.taupstairs.string.JsonString;
import com.example.taupstairs.string.NormalString;
import com.example.taupstairs.util.SdCardUtil;
import com.example.taupstairs.util.SharedPreferencesUtil;

public class MeActivity extends Activity implements ItaActivity {

	private String defaultPersonId;
	private Person defaultPerson;
	private PersonService personService;
	private ListView list_status, list_variable, list_base;
	private String[] my_status = {"我发布的任务", "我报名的任务"};
	private PersonVariableDataAdapter variable_adapter;
	private TextView txt_setting;
	private static final String LIST_LEFT = "left";
	private static final String LIST_RIGHT = "right";
	
	private Bitmap userPhoto;
	private String nickname, signature;
	private static final String IMAGE_FILE_NAME = "userPhoto.png";
	private static String[] items = new String[] { "选择本地图片", "拍照" };
	private String fileName;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hp_me);
		MainService.addActivity(this);
		init();
	}
	
	@Override
	public void init() {
		initData();
		initView();
	}
	
	public void initData() {
		defaultPersonId = SharedPreferencesUtil.getDefaultUser(this).getUserId();
		personService = new PersonService(this);
		defaultPerson = personService.getPersonById(defaultPersonId);
	}
	
	public void initView() {
		list_status = (ListView)findViewById(R.id.list_hp_me_status);
		list_variable = (ListView)findViewById(R.id.list_hp_me_variable);
		list_base = (ListView)findViewById(R.id.list_hp_me_base);
		txt_setting = (TextView)findViewById(R.id.txt_hp_me_setting);
		progressDialog = new ProgressDialog(this);
		
		ArrayAdapter<String> adapter_status = new ArrayAdapter<String>(this, 
				R.layout.common_txt_item, my_status);
		list_status.setAdapter(adapter_status);
		list_status.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case 0:
					Intent intent1 = new Intent(MeActivity.this, MyReleaseStatusActivity.class);
					startActivity(intent1);
					break;
					
				case 1:
					Intent intent2 = new Intent(MeActivity.this, MySignUpStatusActivity.class);
					startActivity(intent2);
					break;

				default:
					break;
				}
			}
		});
		
		if (defaultPerson != null) {		//在initData里面已经从数据库中读数据了
			displayPerson(defaultPerson);	//如果数据库中有数据，就直接显示出来
		} else {
			doGetUserDataTask();			//没有的话，就从服务器获取
		}
		list_variable.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {				//arg2为组件的位置，这个是系统定的，从0开始
				case 0: 
					showDialog();
					break;		
					
				case 1:
					Intent intent_nickname = new Intent(MeActivity.this, UpdataUserdataBaseActivity.class);
					intent_nickname.putExtra(IntentString.Extra.TYPE, Person.PERSON_NICKNAME);
					intent_nickname.putExtra(IntentString.Extra.CONTENT, defaultPerson.getPersonNickname());
					startActivityForResult(intent_nickname, IntentString.RequestCode.MEACTIVITY_UPDATAUSERDATABASE);
					break;	
					
				case 2:
					Intent intent_signature = new Intent(MeActivity.this, UpdataUserdataBaseActivity.class);
					intent_signature.putExtra(IntentString.Extra.TYPE, Person.PERSON_SIGNATURE);
					intent_signature.putExtra(IntentString.Extra.CONTENT, defaultPerson.getPersonSignature());
					startActivityForResult(intent_signature, IntentString.RequestCode.MEACTIVITY_UPDATAUSERDATABASE);
					break;
					
				default:
					break;
				}
			}
		});
		txt_setting.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {		
				Intent intent = new Intent(MeActivity.this, SettingActivity.class);
				startActivity(intent);	
			}
		});
	}
	
	private void showDialog() {
		new AlertDialog.Builder(MeActivity.this)
		.setTitle("设置头像")
		.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					Intent intentFromGallery = new Intent();
					intentFromGallery.setType("image/*"); // 设置文件类型
					intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intentFromGallery, IntentString.RequestCode.IMAGE_REQUEST_CODE);
					break;
				case 1:
					// 判断存储卡是否可以用，可用进行存储
					if (!SdCardUtil.hasSdcard()) {  
						Toast.makeText(MeActivity.this, "未找到存储卡，无法存储照片！", Toast.LENGTH_SHORT).show(); 
		            } else { 
		                File fileName = new File(
		                		Environment.getExternalStorageDirectory().getAbsolutePath(), IMAGE_FILE_NAME);  
		                Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileName));
						startActivityForResult(intentFromCapture, IntentString.RequestCode.CAMERA_REQUEST_CODE);
					}
					break;
				}
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}
	
	private void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 320);
		intent.putExtra("outputY", 320);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, IntentString.RequestCode.PHOTO_REQUEST_CODE);
	}
	
	private void showProgressDialog() {
		progressDialog.setCancelable(false);
		progressDialog.setMessage("    稍等片刻...");
		progressDialog.show();
	}
	
	private void dismissProgressDialog() {
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}
	
	private void doGetUserDataTask() {
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put(Task.TA_GETUSERDATA_ACTIVITY, Task.TA_GETUSERDATA_ACTIVITY_ME);
		taskParams.put(Task.TA_GETUSERDATA_TASKPARAMS, defaultPersonId);
		Task task = new Task(Task.TA_GETUSERDATA, taskParams);
		MainService.addTask(task);
	}
	
	private void doUpdataUserPhotoTask() {
		showProgressDialog();
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put(Task.TA_UPLOADPHOTO_ACTIVITY, Task.TA_UPLOADPHOTO_ACTIVITY_ME);
		taskParams.put(Task.TA_UPLOADPHOTO_BITMAP, userPhoto);
		Task task = new Task(Task.TA_UPLOADPHOTO, taskParams);
		MainService.addTask(task);
	}
	
	private void doUpdataUserDataTask(String url) {
		dismissProgressDialog();
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put(Task.TA_UPDATAUSERDATA_ACTIVITY, Task.TA_UPDATAUSERDATA_FRAGMENT_ME);
		taskParams.put(Task.TA_UPDATAUSERDATA_URL, url);
		Task task = new Task(Task.TA_UPDATAUSERDATA, taskParams);
		MainService.addTask(task);
	}

	@Override
	public void refresh(Object... params) {
		dismissProgressDialog();
		if (params[1] != null) {
			int taskId = (Integer) params[0];
			switch (taskId) {
			case Task.TA_GETUSERDATA:
				defaultPerson = (Person) params[1];
				displayPerson(defaultPerson);		
				personService.insertPerson(defaultPerson);	//更新数据库中的默认Person
				break;
				
			case Task.TA_UPDATAUSERDATA:
				String result = (String) params[1];
				try {
					JSONObject jsonObject = new JSONObject(result);
					String state = jsonObject.getString(JsonString.Return.STATE).trim();
					if (state.equals(JsonString.Return.STATE_OK)) {
						defaultPerson.setPersonPhotoUrl(fileName);
						displayPersonVariable(defaultPerson);
						Toast.makeText(this, "更新头像成功", Toast.LENGTH_SHORT).show();	
						Intent intent = new Intent(NormalString.Receiver.UPDATA_PHOTO);
						intent.putExtra(Person.PERSON_ID, defaultPersonId);
						intent.putExtra(Person.PERSON_PHOTOURL, fileName);
						sendBroadcast(intent);
						personService.updataPersonInfo(defaultPersonId, Person.PERSON_PHOTOURL, fileName);
					} else {
						Toast.makeText(this, "网络竟然出错了", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
				
			case Task.TA_UPLOADPHOTO:
				fileName = (String) params[1];
				String url = "users_id=" + defaultPersonId + "&photo=" + fileName;
				doUpdataUserDataTask(url);
				break;

			default:
				break;
			}
		} else {
			Toast.makeText(this, "网络竟然出错了", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void displayPerson(Person defaultPerson) {
		displayPersonVariable(defaultPerson);
		displayPersonBase(defaultPerson);
		list_status.setVisibility(View.VISIBLE);	//把任务选项列表显示出来
		txt_setting.setVisibility(View.VISIBLE);	//把设置那一行显示出来
	}
	
	/*显示Person资料*/
	private void displayPersonVariable(Person defaultPerson) {
		variable_adapter = new PersonVariableDataAdapter(this, defaultPerson);
		list_variable.setAdapter(variable_adapter);	//把三个可改变资料显示出来
	}
	
	private void displayPersonBase(Person defaultPerson) {
		String[] baseLeft = new String[] {"院系:", "年级:", "专业:", "姓名:", "性别:", };
		String[] baseRight = new String[] {defaultPerson.getPersonFaculty(), defaultPerson.getPersonYear(),
				defaultPerson.getPersonSpecialty(), defaultPerson.getPersonName(), defaultPerson.getPersonSex()};
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
		for (int i = 0; i < baseLeft.length; i++) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put(LIST_LEFT, baseLeft[i]);
			item.put(LIST_RIGHT, baseRight[i]);
			list.add(item);
		}
		SimpleAdapter base_adapter = new SimpleAdapter(this, list, R.layout.person_data_base, 
				new String[] {LIST_LEFT, LIST_RIGHT, }, new int[] {R.id.txt_base_left, R.id.txt_base_right});
		list_base.setAdapter(base_adapter);			//把五个基本资料显示出来
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_CANCELED) {
			switch (requestCode) {
			case IntentString.RequestCode.IMAGE_REQUEST_CODE:
				startPhotoZoom(data.getData());
				break;
			case IntentString.RequestCode.CAMERA_REQUEST_CODE:
				if (SdCardUtil.hasSdcard()) {
					File tempFile = new File(
							Environment.getExternalStorageDirectory().getAbsolutePath(), IMAGE_FILE_NAME);
					startPhotoZoom(Uri.fromFile(tempFile));
				}
				break;
			case IntentString.RequestCode.PHOTO_REQUEST_CODE:
				if (data != null) {
					Bundle extras = data.getExtras();
					if (extras != null) {
						userPhoto = extras.getParcelable("data");
					}
					doUpdataUserPhotoTask();	
				}
				break;
			case IntentString.RequestCode.MEACTIVITY_UPDATAUSERDATABASE:
				if (IntentString.ResultCode.UPDATAUSERDATABASE_MEACTIVITY_NICKNAME == resultCode) {
					nickname = data.getStringExtra(Person.PERSON_NICKNAME);
					defaultPerson.setPersonNickname(nickname);
					displayPersonVariable(defaultPerson);
					Toast.makeText(this, "更新昵称成功", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(NormalString.Receiver.UPDATA_NICKNAME);
					intent.putExtra(Person.PERSON_ID, defaultPersonId);
					intent.putExtra(Person.PERSON_NICKNAME, nickname);
					sendBroadcast(intent);
					personService.updataPersonInfo(defaultPersonId, Person.PERSON_NICKNAME, nickname);
				} else if (IntentString.ResultCode.UPDATAUSERDATABASE_MEACTIVITY_SIGNATURE == resultCode) {
					signature = data.getStringExtra(Person.PERSON_SIGNATURE);
					defaultPerson.setPersonSignature(signature);
					displayPersonVariable(defaultPerson);
					Toast.makeText(this, "更新个性签名成功", Toast.LENGTH_SHORT).show();
					personService.updataPersonInfo(defaultPersonId, Person.PERSON_SIGNATURE, signature);
				}
				break;
			}		
		}
	}
	
	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (personService != null) {
			personService.closeDBHelper();
		}
	}

}

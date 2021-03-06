package com.example.taupstairs.ui.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.example.taupstairs.R;
import com.example.taupstairs.adapter.InfoMessageAdapter;
import com.example.taupstairs.bean.Info;
import com.example.taupstairs.bean.InfoMessage;
import com.example.taupstairs.bean.Message;
import com.example.taupstairs.bean.MessageContent;
import com.example.taupstairs.bean.Person;
import com.example.taupstairs.bean.Status;
import com.example.taupstairs.bean.Task;
import com.example.taupstairs.imageCache.SimpleImageLoader;
import com.example.taupstairs.listener.PersonDataListener;
import com.example.taupstairs.listener.ReplyInfoMessageListener;
import com.example.taupstairs.listener.ReplyListInfoMessageListener;
import com.example.taupstairs.listener.TaskByIdListener;
import com.example.taupstairs.logic.ItaActivity;
import com.example.taupstairs.logic.MainService;
import com.example.taupstairs.logic.TaUpstairsApplication;
import com.example.taupstairs.services.PersonService;
import com.example.taupstairs.string.JsonString;
import com.example.taupstairs.string.NormalString;
import com.example.taupstairs.util.HttpClientUtil;
import com.example.taupstairs.util.KeyBoardUtil;
import com.example.taupstairs.util.SharedPreferencesUtil;
import com.example.taupstairs.util.TimeUtil;

public class InfoMessageActivity extends Activity implements ItaActivity {

	private Button btn_back, btn_message;
	private ImageView img_expression, img_expression_delete;
	private LinearLayout expLayout;
	private GridView grid_expression;
	private Holder holder;
	private Info info;
	private InfoMessageAdapter adapter;
	private EditText edit_message;
	private String edit_text;
	private String replyId, replyNickname;
	private ProgressDialog progressDialog;
	private boolean flag_expression;
	private int[] imageIds = new int[105];
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_message);
		MainService.addActivity(this);
		init();
	}

	@Override
	public void init() {
		initHolder();
		initData();
		initView();
	}
	
	/*头像，昵称，性别，发布时间，来自哪个院系、年级，
	 * 留言内容，任务发布人，任务标题*/
	private class Holder {
		public LinearLayout layout_loading;
		public ImageView img_photo;
		public TextView txt_nickname;
		public ImageView img_sex;
		public TextView txt_releasetime;
		public TextView txt_grade;
		public TextView txt_department;
		
		public TextView txt_message;
		public TextView txt_message_reply;
		public View view;
		public TextView txt_status_nickname;
		public TextView txt_status_title;
		
		public ListView list_message;
	}
	
	private void initHolder() {
		holder = new Holder();
		holder.layout_loading = (LinearLayout)findViewById(R.id.layout_loading);
		
		holder.img_photo = (ImageView)findViewById(R.id.img_photo);
		holder.txt_nickname = (TextView)findViewById(R.id.txt_nickname);
		holder.img_sex = (ImageView)findViewById(R.id.img_sex);
		holder.txt_releasetime = (TextView)findViewById(R.id.txt_releasetime);
		holder.txt_grade = (TextView)findViewById(R.id.txt_grade);
		holder.txt_department = (TextView)findViewById(R.id.txt_department);	
		
		holder.txt_message = (TextView)findViewById(R.id.txt_info_message_message);
		holder.txt_message_reply = (TextView)findViewById(R.id.txt_info_message_reply);
		holder.view = findViewById(R.id.layout_info_message_task);
		holder.txt_status_nickname = (TextView)findViewById(R.id.txt_info_message_nickname);
		holder.txt_status_title = (TextView)findViewById(R.id.txt_info_message_title);
		
		holder.list_message = (ListView)findViewById(R.id.list_info_message_message);
	}
	
	private void initData() {
		TaUpstairsApplication app = (TaUpstairsApplication) getApplication();
		info = app.getInfo();
		if (null == info.getInfoMessage()) {
			showProgressBar();
			doGetInfoMessageTask();
		} else {
			displayMessage();
		}
		replyId = info.getPersonId();	//这两个不初始化的话，直接回复就会出错
		replyNickname = info.getPersonNickname();
		flag_expression = true;
	}
	
	private void initView() {
		btn_back = (Button) findViewById(R.id.btn_back_info_message_detail);
		edit_message = (EditText)findViewById(R.id.edit_task_detail_message);
		btn_message = (Button)findViewById(R.id.btn_task_detail_message);
		img_expression = (ImageView)findViewById(R.id.img_task_detail_expression);
		img_expression_delete = (ImageView)findViewById(R.id.img_expression_delete);
		expLayout = (LinearLayout)findViewById(R.id.layout_expression);
		grid_expression = (GridView)findViewById(R.id.grid_expression);
		progressDialog = new ProgressDialog(this);
		
		SimpleImageLoader.showImage(holder.img_photo, 
				HttpClientUtil.PHOTO_BASE_URL + info.getPersonPhotoUrl());
		PersonDataListener personDataListener = 
				new PersonDataListener(this, info.getPersonId(), Person.PERMISSION_HIDE);
		holder.img_photo.setOnClickListener(personDataListener);	
		holder.txt_nickname.setText(info.getPersonNickname());	
		String personSex = info.getPersonSex().trim();
		if (personSex.equals(Person.MALE)) {
			holder.img_sex.setImageResource(R.drawable.icon_male);
		} else if (personSex.equals(Person.FEMALE)) {
			holder.img_sex.setImageResource(R.drawable.icon_female);
		}
		String displayTime = TimeUtil.getDisplayTime(TimeUtil.getNow(), info.getInfoReleaseTime());
		holder.txt_releasetime.setText(displayTime);
		holder.txt_grade.setText(info.getPersonGrade());
		holder.txt_department.setText(info.getPersonDepartment());
		
		createExpressionGridView();
		MyOnClickListener listener = new MyOnClickListener();
		btn_back.setOnClickListener(listener);
		img_expression.setOnClickListener(listener);
		img_expression_delete.setOnClickListener(listener);
		edit_message.setOnClickListener(listener);
		btn_message.setOnClickListener(listener);
		grid_expression.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageIds[arg2]);
				Bitmap smallBitmap = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
				ImageSpan imageSpan = new ImageSpan(InfoMessageActivity.this, smallBitmap);
				String str;
				if (arg2 < 10){
					str = "[fac00" + arg2;
				} else if (arg2 < 100){
					str = "[fac0" + arg2;
				} else {
					str = "[fac" + arg2;
				}
				SpannableString spannableString = new SpannableString(str);
				spannableString.setSpan(imageSpan, 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				int selectionStart = edit_message.getSelectionStart();
				edit_message.getText().insert(selectionStart, spannableString);
			}
		});
	}
	
	/**
	 * 初始化表情
	 */
	private void createExpressionGridView() {
		List<Map<String,Object>> listItems = new ArrayList<Map<String,Object>>();
		for(int i = 0; i < 105; i++){
			try {
				Field field = R.drawable.class.getDeclaredField("smiley_" + i);
				int resourceId = Integer.parseInt(field.get(null).toString());
				imageIds[i] = resourceId;
			} catch (Exception e) {
				e.printStackTrace();
			}
	        Map<String,Object> listItem = new HashMap<String,Object>();
			listItem.put("image", imageIds[i]);
			listItems.add(listItem);
		}
		
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems, R.layout.expression_cell, 
				new String[]{"image"}, new int[] {R.id.img_expression_cell});
		grid_expression.setAdapter(simpleAdapter);
	}
	
	private void showProgressBar() {
		holder.layout_loading.setVisibility(View.VISIBLE);
	}
	
	private void hideProgressBar() {
		holder.layout_loading.setVisibility(View.GONE);
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
	
	/**
	 * 改变留言edit，并为留言做好参数准备
	 * @param replyId
	 * @param replyNickname
	 */
	public void changeEditHint(String replyId, String replyNickname) {
		this.replyId = replyId;
		this.replyNickname = replyNickname;
		edit_message.setHint("回复  " + replyNickname);
		edit_message.requestFocus();
		KeyBoardUtil.show(this, edit_message);
	}
	
	/**
	 * 获取消息详情
	 */
	private void doGetInfoMessageTask() {
		HashMap<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put(Info.INFO_SOURCE, info.getInfoSource());
		taskParams.put(Info.INFO_TYPE, info.getInfoType());
		taskParams.put(Task.TA_GETINFO_DETAIL_ACTIVITY, Task.TA_GETINFO_DETAIL_MESSAGE);
		Task task = new Task(Task.TA_GETINFO_DETAIL, taskParams);
		MainService.addTask(task);
	}
	
	/**
	 * 进行回复
	 */
	private void doMessageTask() {
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put(Task.TA_MESSAGE_ACTIVITY, Task.TA_MESSAGE_ACTIVITY_INFO);
		taskParams.put(Status.STATUS_ID, info.getInfoMessage().getStatusId());
		taskParams.put(MessageContent.CONTENT, edit_message.getText().toString().trim());
		taskParams.put(Task.TA_MESSAGE_MODE, Task.TA_MESSAGE_MODE_CHILD);
		taskParams.put(Message.MESSAGE_ID, info.getInfoMessage().getMessageId());
		taskParams.put(MessageContent.REPLY_ID, replyId);
		Task task = new Task(Task.TA_MESSAGE, taskParams);
		MainService.addTask(task);
	}

	@Override
	public void refresh(Object... params) {
		dismissProgressDialog();
		if (params[1] != null) {
			int taskId = (Integer) params[0];
			switch (taskId) {
			case Task.TA_GETINFO_DETAIL:
				hideProgressBar();
				InfoMessage infoMessage = (InfoMessage) params[1];
				info.setInfoMessage(infoMessage);
				displayMessage();
				break;
				
			case Task.TA_MESSAGE:
				String message = (String) params[1];
				try {
					JSONObject jsonObject = new JSONObject(message);
					String state = jsonObject.getString(JsonString.Return.STATE).trim();
					if (state.equals(JsonString.Return.STATE_OK)) {
						edit_text = edit_message.getText().toString().trim();
						edit_message.setText("");
						postMessage();	
						KeyBoardUtil.dismiss(this, edit_message);
						expLayout.setVisibility(View.GONE);
					} else {		
						Toast.makeText(this, "网络竟然出错了", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}
	}
	
	/**
	 * 显示留言消息基本部分
	 */
	private void displayMessage() {
		String content = info.getInfoMessage().getCurrentMessage();
		SpannableString spannableString = new SpannableString(content);
		spanExp(content, spannableString);
		holder.txt_message.setText(spannableString);
		holder.view.setOnClickListener(new TaskByIdListener(this, info.getInfoMessage().getStatusId()));
		holder.txt_status_nickname.setText(info.getInfoMessage().getStatusPersonNickname());
		holder.txt_status_title.setText("  :  " + info.getInfoMessage().getStatusTitle());
		displayMessageList();
	}
	
	/**
	 * 表情转换
	 * @param content 留言内容
	 */
	private void spanExp(String content, SpannableString spannableString) {
		Pattern pattern = Pattern.compile(NormalString.Pattern.EXPRESSION);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			String tempString = content.substring(start + 4, end);
			int number = Integer.parseInt(tempString);
			if (number >= 0 && number < 105) {
				try {
					Field field = R.drawable.class.getDeclaredField("smiley_" + number);
					int resourceId = Integer.parseInt(field.get(null).toString());
					Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
					Bitmap smallBitmap = Bitmap.createScaledBitmap(bitmap, 35, 35, true);
					ImageSpan imageSpan = new ImageSpan(InfoMessageActivity.this, smallBitmap);
					spannableString.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 显示留言列表，注册监听器
	 */
	private void displayMessageList() {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		List<MessageContent> contents = info.getInfoMessage().getContents();
		for (int i = 0; i < contents.size(); i++) {
			MessageContent content = contents.get(i);		
			String t = content.getContent();				
			Map<String, Object> item = new HashMap<String, Object>();
			String replyNickname = content.getReplyNickname();
			String receiveNickname = content.getReceiveNickname();
			String text = replyNickname + MessageContent.REPLY_TEXT + 
					receiveNickname + MessageContent.REPLY_TEMP + t;
			item.put(MessageContent.CONTENT, text);			
			item.put(MessageContent.REPLY_START, 0);
			item.put(MessageContent.REPLY_END, replyNickname.length());
			item.put(MessageContent.RECEIVE_START, 
					replyNickname.length() + MessageContent.REPLY_TEXT_LENNTH);
			item.put(MessageContent.RECEIVEY_END, 
					replyNickname.length() + MessageContent.REPLY_TEXT_LENNTH + receiveNickname.length());
			list.add(item);
		}
		adapter = new InfoMessageAdapter(this, list);
		holder.list_message.setAdapter(adapter);
		
		/*这两个在消息详情里面没传过来，要从外面拿
		 * 在点击回复按钮的时候，要回复这个人，需要用到*/
		String replyId = info.getPersonId();
		String replyNickname = info.getPersonNickname();
		ReplyInfoMessageListener replyInfoMessageListener = 
				new ReplyInfoMessageListener(this, replyId, replyNickname);
		holder.txt_message_reply.setOnClickListener(replyInfoMessageListener);
		
		ReplyListInfoMessageListener replyListInfoMessageListener = 
				new ReplyListInfoMessageListener(this, contents);
		holder.list_message.setOnItemClickListener(replyListInfoMessageListener);
	}
	
	/**
	 * 留言成功后改变列表
	 */
	private void postMessage() {
		MessageContent content = new MessageContent();
		String personId = SharedPreferencesUtil.getDefaultUser(this).getUserId();
		content.setReplyId(personId);
		PersonService service = new PersonService(this);
		Person person = service.getPersonById(personId);	//这里读出来可能是空的。但基本上不会，先不管了
		content.setReplyNickname(person.getPersonNickname());
		content.setReceiveNickname(replyNickname);
		content.setContent(edit_text);
		info.getInfoMessage().getContents().add(content);
		displayMessageList();	//在这里用notify没用，这跟他内部机制有些关系。只好重新setAdapter了
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		MainService.removeActivity(this);
	}
	
	class MyOnClickListener implements OnClickListener {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back_info_message_detail:
				finish();
				break;
				
			case R.id.img_task_detail_expression:
				if (flag_expression) {
					img_expression.setImageResource(R.drawable.expression_p);
					KeyBoardUtil.dismiss(InfoMessageActivity.this, edit_message);
					expLayout.setVisibility(View.VISIBLE);
				} else {
					img_expression.setImageResource(R.drawable.expression_n);
					KeyBoardUtil.show(InfoMessageActivity.this, edit_message);
					expLayout.setVisibility(View.GONE);
				}
				flag_expression = !flag_expression;
				break;
				
			case R.id.edit_task_detail_message:
				img_expression.setImageResource(R.drawable.expression_n);
				expLayout.setVisibility(View.GONE);
				break;
				
			case R.id.btn_task_detail_message:
				if (!edit_message.getText().toString().trim().equals("")) {
					showProgressDialog();
					doMessageTask();
				}
				break;
				
			case R.id.img_expression_delete:
				int selectionStart = edit_message.getSelectionStart();// 获取光标的位置
				if (selectionStart > 0) {
				    String body = edit_message.getText().toString();
				    if (!TextUtils.isEmpty(body)) {
				    	String tempStr = body.substring(0, selectionStart);
				    	int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
				    	if (i != -1) {
				    		CharSequence cs = tempStr.subSequence(i, selectionStart - 3);
				    		if (cs.equals("[fac")) {// 判断是否是一个表情
				    			edit_message.getEditableText().delete(i, selectionStart);
				    			return;
				    		}
				    	}
				    	edit_message.getEditableText().delete(selectionStart - 1, selectionStart);
				    }
				}
				break;

			default:
				break;
			}
		}
	}
	
}

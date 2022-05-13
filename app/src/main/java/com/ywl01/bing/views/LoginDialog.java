package com.ywl01.bing.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.orhanobut.hawk.Hawk;
import com.ywl01.bing.R;
import com.ywl01.bing.activities.BaseActivity;
import com.ywl01.bing.beans.SPKey;
import com.ywl01.bing.events.TypeEvent;
import com.ywl01.bing.net.HttpMethods;
import com.ywl01.bing.net.SqlAction;
import com.ywl01.bing.net.SqlFactory;
import com.ywl01.bing.net.TableName;
import com.ywl01.bing.net.User;
import com.ywl01.bing.observers.BaseObserver;
import com.ywl01.bing.observers.InsertObserver;
import com.ywl01.bing.observers.LoginObserver;
import com.ywl01.bing.utils.AppUtils;
import com.ywl01.bing.utils.DialogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import rx.Observer;


/**
 * Created by ywl01 on 2017/3/17.
 */

public class LoginDialog extends Dialog implements View.OnClickListener, BaseObserver.OnNextListener {
    private Context context;

    private EditText etUserName;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnRegister;

    private EditText etUserName_register;
    private EditText etPassword_register;
    private EditText etRePassword;
    private EditText etRealName;
    private EditText etUnit;
    private Button btnSubmit;
    private Button btnCancel;
    private LoginObserver loginObserver;
    private LoginObserver checkUserObserver;
    private InsertObserver insertUserObserver;

    public LoginDialog(Context context) {
        super(context);
        this.context = context;
    }

    public LoginDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.login, null);
        etUserName = (EditText) view.findViewById(R.id.et_userName);
        etPassword = (EditText) view.findViewById(R.id.et_password);
        btnLogin = (Button) view.findViewById(R.id.btn_login);
        btnRegister = (Button) view.findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        setContentView(view);
    }

    @Override
    public void onClick(View view) {
        if (view == btnLogin) {
            onLogin();
        } else if (view == btnRegister) {
            showRegisterView();
        } else if (view == btnSubmit) {
            checkUserName();
        } else if (view == btnCancel) {
            dismiss();
        }
    }

    private void checkUserName() {
        if (validate()) {
            String userName = etUserName.getText().toString().trim();
            checkUserObserver = new LoginObserver();
            String sql = SqlFactory.checkUser(userName);
            HttpMethods.getInstance().getSqlResult(checkUserObserver, SqlAction.SELECT, sql);
            checkUserObserver.setOnNextListener(this);
        }
    }

    private boolean validate() {
        String userName = etUserName_register.getText().toString().trim();
        String password = etPassword_register.getText().toString().trim();
        String rePassword = etRePassword.getText().toString().trim();
        String realName = etRealName.getText().toString().trim();

        if ("".equals(userName)) {
            AppUtils.showToast("用户名不能为空");
            return false;
        } else if ("".equals(password)) {
            AppUtils.showToast("密码不能为空");
            return false;
        } else if (!password.equals(rePassword)) {
            AppUtils.showToast("两次输入密码不一致");
            return false;
        } else if ("".equals(realName)) {
            AppUtils.showToast("真实姓名不能为空");
            return false;
        }
        return true;
    }

    private void onLogin() {
        loginObserver = new LoginObserver();
        String userName = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String sql = SqlFactory.selectUser(userName, password);
        HttpMethods.getInstance().getSqlResult(loginObserver, SqlAction.SELECT, sql);
        loginObserver.setOnNextListener(this);
        dismiss();
    }

    private void showRegisterView() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.register, null);

        etUserName_register = (EditText) view.findViewById(R.id.et_userName);
        etPassword_register = (EditText) view.findViewById(R.id.et_password);
        etRePassword = (EditText) view.findViewById(R.id.et_repass);
        etRealName = (EditText) view.findViewById(R.id.et_realName);
        etUnit = (EditText) view.findViewById(R.id.et_unit);
        btnSubmit = (Button) view.findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(this);
        btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
        setContentView(view);
    }

    @Override
    public void onNext(Object data, Observer observer) {
        if (observer == loginObserver) {
            String json = (String) data;
            try {
                JSONArray arr = new JSONArray(json);
                if (arr.length() > 0) {
                    JSONObject object = arr.getJSONObject(0);
                    User.id = object.getLong("id");
                    User.realName = object.getString("realName");
                    User.userName = object.getString("userName");
                    User.userType = object.getString("userType");

                    Hawk.put( SPKey.USER_ID,User.id);
                    Hawk.put(SPKey.REAL_NAME,User.realName);
                    Hawk.put(SPKey.USER_NAME,User.userName);
                    Hawk.put(SPKey.USER_TYPE,User.userType);

                    System.out.println("保存登陆信息");

                    TypeEvent.send(TypeEvent.LOGIN);

                } else {
                    DialogUtils.showAlert(BaseActivity.currentActivity, "用户名或密码错误", "确定", null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (observer == checkUserObserver) {
            String json = (String) data;
            try {
                JSONArray arr = new JSONArray(json);
                if (arr.length() > 0) {
                    DialogUtils.showAlert(BaseActivity.currentActivity, "该用户名已经被注册", "确定", null);
                } else {
                    insertNewUser();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (observer == insertUserObserver) {
            long returnId = (long) data;
            if (returnId > 0) {
                AppUtils.showToast("已经注册成功，请用注册账号登陆");
                dismiss();
            }
        }
    }

    private void insertNewUser() {
        insertUserObserver = new InsertObserver();
        Map<String, String> tableData = new HashMap<>();
        tableData.put("userName", etUserName_register.getText().toString().trim());
        tableData.put("password", etPassword_register.getText().toString().trim());
        tableData.put("unit", etUnit.getText().toString().trim());
        tableData.put("realName", etRealName.getText().toString().trim());

        String sql = SqlFactory.insert(TableName.USER, tableData);
        HttpMethods.getInstance().getSqlResult(insertUserObserver, SqlAction.INSERT, sql);
        insertUserObserver.setOnNextListener(this);
    }
}

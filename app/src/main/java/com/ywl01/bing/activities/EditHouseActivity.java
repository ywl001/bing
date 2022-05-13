package com.ywl01.bing.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.orhanobut.hawk.Hawk;
import com.ywl01.bing.R;
import com.ywl01.bing.beans.HouseBean;
import com.ywl01.bing.beans.PeopleBean;
import com.ywl01.bing.beans.SPKey;
import com.ywl01.bing.events.GetAngleEvent;
import com.ywl01.bing.events.TypeEvent;
import com.ywl01.bing.net.HttpMethods;
import com.ywl01.bing.net.SqlAction;
import com.ywl01.bing.net.SqlFactory;
import com.ywl01.bing.net.TableName;
import com.ywl01.bing.observers.BaseObserver;
import com.ywl01.bing.observers.DelObserver;
import com.ywl01.bing.observers.PeopleObserver;
import com.ywl01.bing.observers.UpdateObserver;
import com.ywl01.bing.utils.AppUtils;
import com.ywl01.bing.utils.DialogUtils;
import com.ywl01.bing.views.CompassDialog;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observer;


public class EditHouseActivity extends BaseActivity implements BaseObserver.OnNextListener, AdapterView.OnItemClickListener {
    private Context context;
    private HouseBean house;

    @BindView(R.id.et_name)
    AutoCompleteTextView etHouseName;

    @BindView(R.id.et_hoser_number)
    EditText etHouseNumber;

    @BindView(R.id.et_angle)
    EditText etAngle;

    private boolean isAngleChange;

    private DelObserver delMarkerObserver;
    private UpdateObserver updateObserver;
    private PeopleObserver peopleObserver;
    private ArrayAdapter adapter;
    private ArrayList<PeopleBean> peoples = new ArrayList<>();

    @Override
    protected void initView() {
        setContentView(R.layout.activity_edit_house);
        ButterKnife.bind(this);

        adapter = new ArrayAdapter<PeopleBean>(this, android.R.layout.simple_dropdown_item_1line, peoples);
        etHouseName.setAdapter(adapter);
        etHouseName.setOnItemClickListener(this);
    }

    @Override
    protected void initData() {
        house = (HouseBean) data.get("data");
        peopleObserver = new PeopleObserver();
        //给控件赋值
        etHouseName.setText(checkStr(house.houseName));
        etHouseNumber.setText(checkStr(house.houseNumber));
        etAngle.setText(house.angle + "");
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().hide();
    }

    private CharSequence hname;
    @OnTextChanged(R.id.et_name)
    public void onPeopleNameChange(CharSequence name){
        if(isAllChinese(name) && name.length() >= 2){
            hname = name;
            String sql = SqlFactory.selectPeople(name.toString());

            HttpMethods.getInstance().getSqlResult(peopleObserver,SqlAction.SELECT,sql);
            peopleObserver.setOnNextListener(this);
        }
    }


    @OnClick(R.id.btn_cancel)
    public void onCancel() {
        finish();
    }

    @OnClick(R.id.btn_submit)
    public void onSubmit() {
        String houseName = etHouseName.getText().toString().trim();
        String houseNumber = etHouseNumber.getText().toString().trim();
        String angle = etAngle.getText().toString().trim();

        Hawk.put(SPKey.HOUSE_ANGLE, angle);

        isAngleChange = !(angle.equals(house.angle));
        boolean isHouseNameChange = !(houseName.equals(house.houseName));
        boolean isHouseNumberChange = !(houseNumber.equals(house.houseNumber));

        Map<String, String> tableData = new HashMap<>();
        if (isHouseNameChange) {
            tableData.put("houseName", houseName);
            house.houseName = houseName;
        }
        if (isHouseNumberChange) {
            tableData.put("houseNumber", houseNumber);
            house.houseNumber = houseNumber;
        }
        if (isAngleChange) {
            tableData.put(SPKey.HOUSE_ANGLE, angle);
            house.angle = Float.parseFloat(angle);
            Hawk.put(SPKey.HOUSE_ANGLE, angle);
        }

        if (tableData.isEmpty()) {
            finish();
            return;
        }

        Hawk.put(SPKey.MARK_LAT, house.y);
        Hawk.put(SPKey.MARK_LNG, house.x);

        updateObserver = new UpdateObserver();
        String sql = SqlFactory.update("house", tableData, house.id);
        HttpMethods.getInstance().getSqlResult(updateObserver, SqlAction.UPDATE, sql);
        updateObserver.setOnNextListener(this);
        finish();
    }

    @OnClick(R.id.btn_del)
    public void delHouse() {
        DialogUtils.showAlert(BaseActivity.currentActivity,
                "删除提示",
                "确定要删除该监控点吗？",
                "确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("aaaaaaaa");
                        confirmDel();
                        dialogInterface.dismiss();
                    }
                },
                "取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("bbbbbbbb");
                        dialogInterface.dismiss();
                    }
                });
    }

    private void confirmDel() {
        System.out.println("house id:" + house.id);
        delMarkerObserver = new DelObserver();
        String sql = SqlFactory.delete(TableName.HOUSE, house.id);
        System.out.println(sql);
        HttpMethods.getInstance().getSqlResult(delMarkerObserver, SqlAction.DELETE, sql);
        delMarkerObserver.setOnNextListener(this);
        finish();
    }

    @Override
    public void onNext(Object data, Observer observer) {
        if(observer == delMarkerObserver || observer == updateObserver){
            int rows = (int) data;
            if (rows > 0) {
                TypeEvent.send(TypeEvent.REFRESH_MARKERS);
                String m = observer == updateObserver ? "更新成功" : "删除成功";
                AppUtils.showToast(m);
            }
            TypeEvent.send(TypeEvent.GET_COUNT_INFO);
        }else if(observer == peopleObserver){
            System.out.println(data);
            ArrayList<PeopleBean> p = (ArrayList) data;
            adapter.clear();
            adapter.addAll(p);
            adapter.getFilter().filter(hname);
        }
    }

    private String checkStr(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        } else if (str.toLowerCase().equals("null")) {
            return "";
        } else
            return str;
    }

    private void showSelectAngleDialog() {
        String strAngle = etAngle.getText().toString();
        float a = 0;
        try {
            a = Float.parseFloat(strAngle);
        } catch (NumberFormatException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        CompassDialog dialog = new CompassDialog(BaseActivity.currentActivity, R.style.dialog);
        dialog.show();
        System.out.println("set init angle:" + a);
        if (a != 0) {
            dialog.setInitAngle(a);
        }
    }


    @OnClick(R.id.et_angle)
    public void onClickEtAngle() {
        showSelectAngleDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void getAngle(GetAngleEvent event) {
        int angle = event.angle;
        if (angle >= 360) {
            angle = angle - 360;
        }
        etAngle.setText(angle + "");
    }

    private boolean isAllChinese(CharSequence str){
        for (int i = 0; i < str.length(); i++) {
            if(!isChinese(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    private boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        PeopleBean p = (PeopleBean) adapterView.getAdapter().getItem(i);
        etHouseName.setText(p.name);
        etHouseNumber.setText(p.pNumber);
    }
}

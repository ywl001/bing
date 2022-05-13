package com.ywl01.bing.activities;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import com.orhanobut.hawk.Hawk;
import com.ywl01.bing.R;
import com.ywl01.bing.beans.BuildingBean;
import com.ywl01.bing.beans.SPKey;
import com.ywl01.bing.events.GetAngleEvent;
import com.ywl01.bing.events.TypeEvent;
import com.ywl01.bing.net.HttpMethods;
import com.ywl01.bing.net.SqlAction;
import com.ywl01.bing.net.SqlFactory;
import com.ywl01.bing.net.TableName;
import com.ywl01.bing.observers.BaseObserver;
import com.ywl01.bing.observers.DelObserver;
import com.ywl01.bing.observers.UpdateObserver;
import com.ywl01.bing.utils.AppUtils;
import com.ywl01.bing.utils.DialogUtils;
import com.ywl01.bing.views.CompassDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.Observer;


public class EditBuildingActivity extends BaseActivity implements BaseObserver.OnNextListener {
    private BuildingBean building;

    @BindView(R.id.et_housing)
    EditText etHousing;

    @BindView(R.id.et_building_number)
    EditText etBuildingNumber;

    @BindView(R.id.et_count_floor)
    EditText etCountFloor;

    @BindView(R.id.et_count_unit)
    EditText etCountUnit;

    @BindView(R.id.et_unit_houses)
    EditText etUnitHouses;

    @BindView(R.id.et_building_angle)
    EditText etBuildingAngle;

    @BindView(R.id.radio_sort_a)
    RadioButton sortA;

    @BindView(R.id.radio_sort_b)
    RadioButton sortB;

    private String sortType;

    private DelObserver delMarkerObserver;
    private UpdateObserver updateObserver;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_edit_building);
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        building = (BuildingBean) data.get("data");

        //给控件赋值
        etHousing.setText(checkStr(building.housingName));
        etBuildingNumber.setText(checkStr(building.buildingNumber));
        etBuildingAngle.setText(checkStr(building.angle + ""));
        etCountFloor.setText(checkStr(building.countFloor + ""));
        etCountUnit.setText(checkStr(building.countUnit + ""));
        etUnitHouses.setText(checkStr(building.countHomesInUnit + ""));
        if (building.sortType.equals("a")) {
            sortA.setChecked(true);
        } else if (building.sortType.equals("b")) {
            sortB.setChecked(true);
        }
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().hide();
    }

    @OnCheckedChanged({R.id.radio_sort_a, R.id.radio_sort_b})
    public void onSortTypeChange(CompoundButton button, boolean checked) {
        if (checked) {
            switch (button.getId()) {
                case R.id.radio_sort_a:
                    sortType = "a";
                    break;
                case R.id.radio_sort_b:
                    System.out.println("building");
                    sortType = "b";
                    break;
            }
        }
    }

    @OnClick(R.id.btn_cancel)
    public void onCancel() {
        finish();
    }

    @OnClick(R.id.btn_submit)
    public void onSubmit() {

        Hawk.put(SPKey.HOUSING_NAME, getETValue(etHousing));
        Hawk.put(SPKey.MARK_LAT, building.y);
        Hawk.put(SPKey.MARK_LNG, building.x);

        Map<String, String> tableData = new HashMap<>();
        tableData.put("housingName", getETValue(etHousing));
        tableData.put("buildingNumber", getETValue(etBuildingNumber));
        tableData.put("countFloor", getETValue(etCountFloor));
        tableData.put("countUnit", getETValue(etCountUnit));
        tableData.put("countHomesInUnit", getETValue(etUnitHouses));
        tableData.put("angle", getETValue(etBuildingAngle));
        tableData.put("sortType", sortType);

        updateObserver = new UpdateObserver();
        String sql = SqlFactory.update(TableName.BUILDING, tableData, building.id);
        HttpMethods.getInstance().getSqlResult(updateObserver, SqlAction.UPDATE, sql);
        updateObserver.setOnNextListener(this);
        finish();
    }

    private String getETValue(EditText et) {
        return et.getText().toString().trim();
    }

    @OnClick(R.id.btn_del)
    public void del() {
        DialogUtils.showAlert(BaseActivity.currentActivity,
                "删除提示",
                "确定要删除该监控点吗？",
                "确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        confirmDel();
                        dialogInterface.dismiss();
                    }
                },
                "取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
    }

    private void confirmDel() {
        delMarkerObserver = new DelObserver();
        String sql = SqlFactory.delete(TableName.BUILDING, building.id);
        HttpMethods.getInstance().getSqlResult(delMarkerObserver, SqlAction.DELETE, sql);
        delMarkerObserver.setOnNextListener(this);
        finish();
    }

    @Override
    public void onNext(Object data, Observer observer) {
        int rows = (int) data;
        if (rows > 0) {
            TypeEvent.send(TypeEvent.REFRESH_MARKERS);
            String m = observer == updateObserver ? "更新成功" : "删除成功";
            AppUtils.showToast(m);
        }
        TypeEvent.send(TypeEvent.GET_COUNT_INFO);
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
        String strAngle = etBuildingAngle.getText().toString();
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


    @OnClick(R.id.et_building_angle)
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
        etBuildingAngle.setText(angle + "");
    }
}

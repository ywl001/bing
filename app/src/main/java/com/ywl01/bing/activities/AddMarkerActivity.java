package com.ywl01.bing.activities;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.orhanobut.hawk.Hawk;
import com.ywl01.bing.R;
import com.ywl01.bing.beans.PeopleBean;
import com.ywl01.bing.beans.SPKey;
import com.ywl01.bing.events.GetAngleEvent;
import com.ywl01.bing.events.TypeEvent;
import com.ywl01.bing.net.HttpMethods;
import com.ywl01.bing.net.SqlAction;
import com.ywl01.bing.net.SqlFactory;
import com.ywl01.bing.net.TableName;
import com.ywl01.bing.net.User;
import com.ywl01.bing.observers.BaseObserver;
import com.ywl01.bing.observers.DelObserver;
import com.ywl01.bing.observers.InsertObserver;
import com.ywl01.bing.observers.PeopleObserver;
import com.ywl01.bing.observers.UpdateObserver;
import com.ywl01.bing.utils.AppUtils;
import com.ywl01.bing.utils.PeopleNumbleUtils;
import com.ywl01.bing.views.CompassDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observer;


/**
 * Created by ywl01 on 2017/3/12.
 */

public class AddMarkerActivity extends BaseActivity implements BaseObserver.OnNextListener, AdapterView.OnItemClickListener {

    @BindView(R.id.et_town)
    EditText etTown;

    //民房
    @BindView(R.id.et_village)
    EditText etVillage;

    @BindView(R.id.et_name)
    AutoCompleteTextView etHouseName;

    @BindView(R.id.et_hoser_number)
    EditText etHouseNumber;

    @BindView(R.id.et_angle)
    EditText etAngle;

    //楼房
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


    //    场所
    @BindView(R.id.et_manager_name)
    EditText etManagerName;

    @BindView(R.id.et_marker_name)
    EditText etMarkerName;

    @BindView(R.id.et_telephone)
    EditText etTelephone;

    @BindView(R.id.et_display_level)
    EditText etDisplayLevel;

    //容器
    @BindView(R.id.container_house)
    LinearLayout houseContainer;

    @BindView(R.id.container_building)
    LinearLayout buildingContainer;

    @BindView(R.id.container_marker)
    LinearLayout markerContainer;

    @BindView(R.id.radio_type)
    RadioGroup type;

    String state = TableName.HOUSE;
    private String sortType = "a";

    private PeopleObserver peopleObserver;
    private ArrayAdapter adapter;
    private ArrayList<PeopleBean> peoples = new ArrayList<>();
    private InsertObserver insertObserver;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_add_marker);
        ButterKnife.bind(this);

        adapter = new ArrayAdapter<PeopleBean>(this, android.R.layout.simple_dropdown_item_1line, peoples);
        peopleObserver = new PeopleObserver();
        etHouseName.setAdapter(adapter);
        etHouseName.setOnItemClickListener(this);

//        初始化设置为民房
        String town = Hawk.get(SPKey.TOWN, "");
        etTown.setText(town);
        String village = Hawk.get(SPKey.VILLAGE, "");
        String angle = Hawk.get(SPKey.HOUSE_ANGLE, "");
        etAngle.setText(angle);
        etVillage.setText(village);
        etHouseName.requestFocus();
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().hide();
    }

    @OnCheckedChanged({R.id.radio_house, R.id.radio_building, R.id.radio_marker})
    public void onTypechange(CompoundButton button, boolean checked) {
        if (checked) {
            switch (button.getId()) {
                case R.id.radio_house:
                    System.out.println("house");
                    state = TableName.HOUSE;
                    setUIVisible(houseContainer, buildingContainer, markerContainer);
                    String village = Hawk.get(SPKey.VILLAGE, "");
                    double angle = Hawk.get(SPKey.HOUSE_ANGLE, 0.0);
                    etAngle.setText(angle + "");
                    etVillage.setText(village);
                    etHouseName.requestFocus();
                    break;
                case R.id.radio_building:
                    System.out.println("building");
                    state = TableName.BUILDING;
                    setUIVisible(buildingContainer, houseContainer, markerContainer);
                    String housingName = Hawk.get(SPKey.HOUSING_NAME, "");
                    etHousing.setText(housingName);
                    etBuildingNumber.requestFocus();
                    break;
                case R.id.radio_marker:
                    state = TableName.MARKER;
                    setUIVisible(markerContainer, houseContainer, buildingContainer);
                    etMarkerName.requestFocus();
                    System.out.println("marker");
                    break;
            }
        }
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

    private CharSequence hname;

    @OnTextChanged(R.id.et_name)
    public void onPeopleNameChange(CharSequence name) {
        if (isAllChinese(name) && name.length() >= 2) {
            hname = name;
            String sql = SqlFactory.selectPeople(name.toString());
            System.out.println(sql);
            HttpMethods.getInstance().getSqlResult(peopleObserver, SqlAction.SELECT, sql);
            peopleObserver.setOnNextListener(this);
        }
    }

    private void setUIVisible(LinearLayout container1, LinearLayout container2, LinearLayout container3) {
        container1.setVisibility(View.VISIBLE);
        container2.setVisibility(View.GONE);
        container3.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_cancel)
    public void onCancel() {
        finish();
    }

    @OnClick(R.id.et_angle)
    public void onClickEtAngle() {
        showSelectAngleDialog();
    }

    @OnClick(R.id.btn_submit)
    public void onSubmit() {
        if (!validate()) return;

        Hawk.put(SPKey.MARK_LAT, data.getDouble("y"));
        Hawk.put(SPKey.MARK_LNG, data.getDouble("x"));

        String sql = getSql(state);
        insertObserver = new InsertObserver();
        HttpMethods.getInstance().getSqlResult(insertObserver, SqlAction.INSERT, sql);
        insertObserver.setOnNextListener(this);
    }

    private String getSql(String state) {
        HashMap<String, String> tableData = new HashMap<>();
        String town = getETValue(etTown);
        tableData.put("insertUser", User.id + "");
        tableData.put("x", data.getDouble("x") + "");
        tableData.put("y", data.getDouble("y") + "");
        tableData.put("town", town);
        Hawk.put(SPKey.TOWN, town);

        if (state.equals(TableName.HOUSE)) {
            tableData.put("village", getETValue(etVillage));
            tableData.put("houseName", getETValue(etHouseName));
            tableData.put("houseNumber", getETValue(etHouseNumber));
            tableData.put("angle", getETValue(etAngle));

            Hawk.put(SPKey.HOUSE_ANGLE, getETValue(etAngle));
            Hawk.put(SPKey.VILLAGE, getETValue(etVillage));
        } else if (state.equals(TableName.BUILDING)) {
            tableData.put("housingName", getETValue(etHousing));
            tableData.put("buildingNumber", getETValue(etBuildingNumber));
            tableData.put("countFloor", getETValue(etCountFloor));
            tableData.put("countUnit", getETValue(etCountUnit));
            tableData.put("countHomesInUnit", getETValue(etUnitHouses));
            tableData.put("angle", getETValue(etBuildingAngle));
            tableData.put("sortType", sortType);

            Hawk.put(SPKey.HOUSING_NAME, getETValue(etHousing));

        } else if (state.equals(TableName.MARKER)) {
            tableData.put("name", getETValue(etMarkerName));
            tableData.put("managerName", getETValue(etManagerName));
            tableData.put("telephone", getETValue(etTelephone));
            tableData.put("displayLevel", getETValue(etDisplayLevel));
        }

        String sql = SqlFactory.insert(state, tableData);
        return sql;
    }

    private boolean validate() {
        if (state.equals(TableName.HOUSE)) {
            if (getETValue(etHouseName).equals("")) {
                AppUtils.showToast("房屋名字必须填写");
                return false;
            }
            if (getETValue(etHouseNumber).equals("")) {
                AppUtils.showToast("身份证号必须填写");
                return false;
            }
            boolean res = PeopleNumbleUtils.validate(etHouseNumber.getText().toString());
            if (!res) {
                AppUtils.showToast(PeopleNumbleUtils.errorMessage);
                return false;
            }
            return true;
        } else if (state.equals(TableName.BUILDING)) {
            if (getETValue(etBuildingNumber).equals("") || getETValue(etBuildingNumber).equals("")) {
                AppUtils.showToast("请填写楼栋号或小区名称");
                return false;
            }
            if (getETValue(etCountUnit).equals("") || getETValue(etCountFloor).equals("") || getETValue(etUnitHouses).equals("")) {
                AppUtils.showToast("请填写楼栋参数");
                return false;
            }
            return true;
        } else if (state.equals(TableName.MARKER)) {
            if (getETValue(etMarkerName).equals("")) {
                AppUtils.showToast("名字必须填写");
                return false;
            }
            return true;
        }
        return true;
    }

    private String getETValue(EditText et) {
        return et.getText().toString().trim();
    }

    private void showSelectAngleDialog() {
        String strAngle = etAngle.getText().toString();
        int a = 0;
        try {
            a = Integer.parseInt(strAngle);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        CompassDialog dialog = new CompassDialog(BaseActivity.currentActivity, R.style.dialog);
        dialog.show();
        System.out.println("set init angle:" + a);
        if (a != 0) {
            dialog.setInitAngle(a);
        }
    }

    @Override
    public void onNext(Object data, Observer observer) {
        if (observer == insertObserver) {
            finish();
            long returnID = (long) data;
            if (returnID > 0) {
                AppUtils.showToast("插入成功");
                TypeEvent.send(TypeEvent.REFRESH_MARKERS);
                TypeEvent.send(TypeEvent.GET_COUNT_INFO);
            }
        } else if (observer == peopleObserver) {
            System.out.println(data);
            ArrayList<PeopleBean> p = (ArrayList) data;
            adapter.clear();
            adapter.addAll(p);
            adapter.getFilter().filter(hname);
        }
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

    @SuppressLint("SetTextI18n")
    @Subscribe
    public void getAngle(GetAngleEvent event) {
        int angle = event.angle;
        if (state.equals(TableName.HOUSE)) {
            etAngle.setText(angle + "");
        } else {
            etBuildingAngle.setText(angle + "");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        PeopleBean p = (PeopleBean) adapterView.getAdapter().getItem(i);
        etHouseName.setText(p.name);
        etHouseNumber.setText(p.pNumber);
    }

    private boolean isAllChinese(CharSequence str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isChinese(str.charAt(i))) {
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

}

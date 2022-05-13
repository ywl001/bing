package com.ywl01.bing.activities;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.EditText;

import com.orhanobut.hawk.Hawk;
import com.ywl01.bing.R;
import com.ywl01.bing.beans.MarkerBean;
import com.ywl01.bing.beans.SPKey;
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

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;


public class EditMarkerActivity extends BaseActivity implements BaseObserver.OnNextListener {
    private MarkerBean marker;

    @BindView(R.id.et_manager_name)
    EditText etManagerName;

    @BindView(R.id.et_marker_name)
    EditText etMarkerName;

    @BindView(R.id.et_telephone)
    EditText etTelephone;

    @BindView(R.id.et_display_level)
    EditText etDisplayLevel;


    private DelObserver delMarkerObserver;
    private UpdateObserver updateObserver;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_edit_mark);
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        marker = (MarkerBean) data.get("data");
        //给控件赋值
        etMarkerName.setText(checkStr(marker.name));
        etManagerName.setText(checkStr(marker.managerName));
        etTelephone.setText(checkStr(marker.telephone));
        etDisplayLevel.setText(checkStr(marker.displayLevel + ""));
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().hide();
    }

    @OnClick(R.id.btn_cancel)
    public void onCancel() {
        finish();
    }

    @OnClick(R.id.btn_submit)
    public void onSubmit() {
        Map<String, String> tableData = new HashMap<>();
        tableData.put("name", getETValue(etMarkerName));
        tableData.put("managerName", getETValue(etManagerName));
        tableData.put("telephone", getETValue(etTelephone));
        tableData.put("displayLevel", getETValue(etDisplayLevel));

       Hawk.put(SPKey.MARK_LAT, marker.y);
       Hawk.put(SPKey.MARK_LNG, marker.x);

        updateObserver = new UpdateObserver();
        String sql = SqlFactory.update(TableName.MARKER, tableData, marker.id);
        HttpMethods.getInstance().getSqlResult(updateObserver, SqlAction.UPDATE, sql);
        updateObserver.setOnNextListener(this);
        finish();
    }

    private String getETValue(EditText et) {
        return et.getText().toString().trim();
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
        delMarkerObserver = new DelObserver();
        String sql = SqlFactory.delete(TableName.MARKER, marker.id);
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

package com.ywl01.bing.events;

/**
 * Created by ywl01 on 2018/3/7.
 * 对于不需要携带参数的event的合集
 */

public class TypeEvent extends Event {

    public static final int CHANGE_PANORAMA_ICON = 1;
    public static final int DEL_MARKER = 2;
    public static final int LOGIN = 3;

    public static final int MOVE_MARKER = 4;
    public static final int REFRESH_MARKERS = 5;
    public static final int GET_COUNT_INFO = 6;
    public static final int UPLOAD_COMPLETE = 7;
    public static final int SHOW_BTN_CONTAINER = 8;

    public int type;

    public TypeEvent(int type) {
        this.type = type;
    }

    public static void send(int type) {
        TypeEvent event = new TypeEvent(type);
        event.dispatch();
    }

}

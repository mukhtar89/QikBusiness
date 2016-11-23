package com.equinox.qikbusiness.Enums;

import com.equinox.qikbusiness.R;

/**
 * Created by mukht on 11/23/2016.
 */

public enum EmployeeStatus {

    IDLE(android.R.color.holo_green_dark, R.drawable.ic_event_available_black_48dp),
    BUSY (R.color.colorPrimary, R.drawable.ic_event_note_black_48dp),
    OUT (android.R.color.holo_red_dark, R.drawable.ic_event_busy_black_48dp);

    private int color;
    private int icon;

    EmployeeStatus(int color, int icon) {
        this.color = color;
        this.icon = icon;
    }

    public int getColor() {
        return color;
    }
    public int getIcon() {
        return icon;
    }
}

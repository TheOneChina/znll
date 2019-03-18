package com.tnsoft.web.model;

public interface Constants {

    interface IsAble {
        int DISABLE = 0;
        int ABLE = 1;
    }

    interface State {
        int STATE_ACTIVE = 1;
        int STATE_FINISHED = 2;
        int STATE_DISABLED = 3;
        int STATE_DELETED = 4;
    }

    interface Version {
        int EXPRESS = 1;
        int MEDICINE = 2;
        int STANDARD = 3;
    }

    interface HardwareType {
        //无屏幕、外置探头
        int NO_SCREEN_SENSOR_OUTSIDE = 0;

        //有屏幕、内置探头
        int SCREEN_SENSOR_INSIDE = 1;
    }

    interface Calibrate {
        int TM20 = 1;
        int TM20E = 2;
        int THM20 = 3;
        int THM20E = 4;
        int PERMISSIBLE_ERROR_HUMIDITY = 5;
        float PERMISSIBLE_ERROR_TEMP = 0.5f;
        int STATUS_OFF = 0;
        int STATUS_ON = 1;
        int SLEEP_TIME = 3;

        int TASK_STATUS_CREATION = 0;
        int TASK_STATUS_LOW = 1;
        int TASK_STATUS_MEDIUM = 2;
        int TASK_STATUS_HIGH = 3;
        int TASK_STATUS_END = 4;
    }


    interface TM20 {
        float HIGH_TEMP = 30;
        float MEDIUM_TEMP = 20;
        float LOW_TEMP = 10;
        float HUMIDITY = 45;
    }

    interface TM20E {
        float HIGH_TEMP = 30;
        float MEDIUM_TEMP = 20;
        float LOW_TEMP = 10;
        float HUMIDITY = 45;
    }

    interface THM20 {
        float HIGH_TEMP = 30;
        float MEDIUM_TEMP = 20;
        float LOW_TEMP = 10;
        float HUMIDITY = 45;
    }

    interface THM20E {
        float HIGH_TEMP = 30;
        float MEDIUM_TEMP = 20;
        float LOW_TEMP = 10;
        float HUMIDITY = 45;
    }

    interface Electricity {
        //高于等于此值时为正常 3.25V
        int NORMAL = 3328;
        //低于此值时为电量过低
//        int LOW = 3000;
    }

    interface SMSAlertType {
        int TYPE_TEMP_LOW = 1;
        int TYPE_TEMP_HIGH = 2;
        int TYPE_TEMP_ELECTRICITY = 3;
        int TYPE_TEMP_LOSS = 4;
    }

    interface UserState {
        int STATE_NORMAL = 1;
        int STATE_CANCLE = 0;
    }

    interface UserRoleState {
        int STATE_NORMAL = 1;
        int STATE_CANCLE = 0;
    }

    interface ExpressState {

        int STATE_ACTIVE = 1;
        int STATE_FINISHED = 2;
    }

    interface UserExpressState {
        int STATE_ACTIVE = 1;
        int STATE_FINISHED = 2;
    }

    interface TagExpressState {
        int STATE_ACTIVE = 1;
        int STATE_DELETE = 2;
    }

    interface BindState {
        int STATE_PENDING = 0;
        int STATE_ACTIVE = 1;
        int STATE_DELETE = 2;
    }

    interface TagState {
        //不可用
        int STATE_DELETE = 0;
        //可用
        int STATE_ACTIVE = 1;
        //工作中
        int STATE_WORKING = 2;
    }

    interface TagBuzzerState {
        int STATE_OFF = 0;
        int STATE_ON = 1;
    }

    interface DomainState {
        int STATE_ACTIVE = 1;
        int STATE_DISABLE = 2;
    }

    interface Role {
        int SUPER_ADMIN = 1;
        int ADMIN = 2;
        int MAINTAINER = 3;
        int COURIER = 4;
        int EXCHANGE_USER = 5;
        int ADMIN_MEDICINE = 6;
        int ADMIN_STANDARD = 7;
        int MAINTAINER_MEDICINE = 8;
        int MAINTAINER_STANDARD = 9;
        int SUB_ADMIN_MEDICINE = 10;
    }

    interface AlertState {
        int STATE_ACTIVE = 1;
        int STATE_FINISHED = 2;
    }

    interface AlertLevel {
        int STATE_SERIOUS = 1;
        int STATE_NORAML_HIGH = 2;
        int STATE_NORAML_LOW = 3;
    }

    interface AlertLevelType {
        int TEMP_SERIOUS = 1;
        int NO_RESPONSE = 2;
    }

    interface AlertType {
        int STATE_TEMPHISALERT = 1;
        //2 - 电力报警
        int STATE_ELECTRICITY = 2;
        int STATE_NOT_RESPONSE = 3;
    }
}

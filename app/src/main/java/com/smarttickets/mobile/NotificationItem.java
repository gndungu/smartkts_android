package com.smarttickets.mobile;

/**
 * Created by gndungu on 2/15/2018.
 */

public class NotificationItem {
    private String notifiation;
    private String notification_date;
    private String notification_type;

    public NotificationItem(String notifiation, String notification_date, String notification_type){
        super();
        this.setNotifiation(notifiation);
        this.setNotificationDate(notification_date);
        this.setNotificationType(notification_type);

    }

    public String getNotifiation() {
        return notifiation;
    }

    public void setNotifiation(String notifiation) {
        this.notifiation = notifiation;
    }

    public String getNotificationDate() {
        return notification_date;
    }

    public void setNotificationDate(String notification_date) {
        this.notification_date = notification_date;
    }

    public String getNotificationType() {
        return notification_type;
    }

    public void setNotificationType(String notification_type) {
        this.notification_type = notification_type;
    }


}

package com.smarttickets.mobile;

/**
 * Created by gndungu on 7/1/2017.
 */
public class OfflineItem {
    private String event_name;
    private String sync_date;
    private String total_tickets;
    private String instance_code;

    public OfflineItem(String event_name, String sync_date, String total_tickets, String instance_code){
        super();
        this.setEventName(event_name);
        this.setSyncDate(sync_date);
        this.setTotalTickets(total_tickets);
        this.setInstanceCode(instance_code);
    }

    public String getEventName() {
        return event_name;
    }

    public void setEventName(String event_name) {
        this.event_name = event_name;
    }

    public String getSyncDate() {
        return sync_date;
    }

    public void setSyncDate(String sync_date) {
        this.sync_date = sync_date;
    }

    public String getTotalTickets() {
        return total_tickets;
    }

    public void setTotalTickets(String total_tickets) {
        this.total_tickets = total_tickets;
    }

    public String getInstanceCode() {
        return instance_code;
    }

    public void setInstanceCode(String instance_code) {
        this.instance_code = instance_code;
    }


}

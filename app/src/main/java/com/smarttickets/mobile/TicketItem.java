package com.smarttickets.mobile;

/**
 * Created by gndungu on 2/15/2018.
 */

public class TicketItem {
    private String event;
    private String amount;
    private String category;
    private String event_date;
    private String payclass;
    private String ticket_code;

    public TicketItem(String event, String amount, String category, String event_date, String payclass, String ticket_code){
        super();
        this.setEvent(event);
        this.setAmount(amount);
        this.setCategory(category);
        this.setEventDate(event_date);
        this.setPayClass(payclass);
        this.setTicketCode(ticket_code);
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEventDate() {
        return event_date;
    }

    public void setEventDate(String event_date) {
        this.event_date = event_date;
    }

    public String getPayClass() {
        return payclass;
    }

    public void setPayClass(String payclass) {
        this.payclass = payclass;
    }

    public String getTicketCode() {
        return ticket_code;
    }

    public void setTicketCode(String ticket_code) {
        this.ticket_code = ticket_code;
    }

}

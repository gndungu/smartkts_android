package com.smarttickets.mobile.model.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "ticket")
public class Ticket {
    @PrimaryKey
    @NonNull
    private Integer id;

    private String provider_id, event_name, instance_code, ticket_code, phone_number, usage_limit, date_bought,
            status, used, scan_count, admits, scan_date, sync_status, sync_date, pay_class, ticket_number, ticket_user, ticket_request, create_date;

    @NonNull
    public Integer getId() {
        return id;
    }

    public void setId(@NonNull Integer id) {
        this.id = id;
    }

    public String getProvider_id() {
        return provider_id;
    }

    public void setProvider_id(String provider_id) {
        this.provider_id = provider_id;
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public String getInstance_code() {
        return instance_code;
    }

    public void setInstance_code(String instance_code) {
        this.instance_code = instance_code;
    }

    public String getTicket_code() {
        return ticket_code;
    }

    public void setTicket_code(String ticket_code) {
        this.ticket_code = ticket_code;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getUsage_limit() {
        return usage_limit;
    }

    public void setUsage_limit(String usage_limit) {
        this.usage_limit = usage_limit;
    }

    public String getDate_bought() {
        return date_bought;
    }

    public void setDate_bought(String date_bought) {
        this.date_bought = date_bought;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getScan_count() {
        return scan_count;
    }

    public void setScan_count(String scan_count) {
        this.scan_count = scan_count;
    }

    public String getAdmits() {
        return admits;
    }

    public void setAdmits(String admits) {
        this.admits = admits;
    }

    public String getScan_date() {
        return scan_date;
    }

    public void setScan_date(String scan_date) {
        this.scan_date = scan_date;
    }

    public String getSync_status() {
        return sync_status;
    }

    public void setSync_status(String sync_status) {
        this.sync_status = sync_status;
    }

    public String getSync_date() {
        return sync_date;
    }

    public void setSync_date(String sync_date) {
        this.sync_date = sync_date;
    }

    public String getPay_class() {
        return pay_class;
    }

    public void setPay_class(String pay_class) {
        this.pay_class = pay_class;
    }

    public String getTicket_number() {
        return ticket_number;
    }

    public void setTicket_number(String ticket_number) {
        this.ticket_number = ticket_number;
    }

    public String getTicket_user() {
        return ticket_user;
    }

    public void setTicket_user(String ticket_user) {
        this.ticket_user = ticket_user;
    }

    public String getTicket_request() {
        return ticket_request;
    }

    public void setTicket_request(String ticket_request) {
        this.ticket_request = ticket_request;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }
}

package com.smarttickets.mobile;

/**
 * Created by gndungu on 7/1/2017.
 */
public class Item {
    private String label_view;
    private String value_view;

    public Item(String label_view, String value_view){
        super();
        this.setLabel_view(label_view);
        this.setValue_view(value_view);
    }

    public String getLabel_view() {
        return label_view;
    }

    public void setLabel_view(String label_view) {
        this.label_view = label_view;
    }

    public String getValue_view() {
        return value_view;
    }

    public void setValue_view(String value_view) {
        this.value_view = value_view;
    }


}

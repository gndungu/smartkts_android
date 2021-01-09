package com.smarttickets.mobile;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gndungu on 7/1/2017.
 */
public class NotificarionsAdapter extends ArrayAdapter<NotificationItem>  implements Filterable {

    private final Context context;
    private  ArrayList<NotificationItem> itemsArrayList;
    private  ArrayList<NotificationItem> filterItemsArrayList;

    CustomFilter filter;

    public NotificarionsAdapter(Context context, ArrayList<NotificationItem> itemsArrayList) {

        super(context, R.layout.notifiation_layout, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
        this.filterItemsArrayList = itemsArrayList;
    }

    @Override
    public int getCount() {
        return itemsArrayList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.notifiation_layout, parent, false);
        rowView.setBackgroundColor(position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5F5F5"));

        // 3. Get the two text view from the rowView
        TextView mNotification = (TextView) rowView.findViewById(R.id.txtMessage);
        TextView mNDate = (TextView) rowView.findViewById(R.id.txtNDate);
        TextView mNType = (TextView) rowView.findViewById(R.id.txtNType);

        // 4. Set the text for textView
        System.out.print(position);
        mNotification.setText(itemsArrayList.get(position).getNotifiation());
        mNDate.setText(itemsArrayList.get(position).getNotificationDate());
        mNType.setText(itemsArrayList.get(position).getNotificationType());


        // 5. return rowView
        return rowView;
    }

    @Override
    public Filter getFilter() {
        // TODO Auto-generated method stub
        if(filter == null)
        {
            filter=new CustomFilter();
        }
        return filter;
    }

    class CustomFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // TODO Auto-generated method stub
            FilterResults results=new FilterResults();
            if(constraint != null && constraint.length()>0)
            {
                //CONSTARINT TO UPPER
                constraint=constraint.toString().toUpperCase();
                List<NotificationItem> filters = new ArrayList<NotificationItem>();
                //get specific items

                for (final NotificationItem ite : filterItemsArrayList) {
                    if (ite.getNotifiation().toUpperCase().contains(constraint)) {
                        filters.add(ite);
                    }
                }

                results.count=filters.size();
                results.values=filters;
            }else
            {
                results.count=filterItemsArrayList.size();
                results.values=filterItemsArrayList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // TODO Auto-generated method stub
            System.out.println("Search Result "+ results.values);
            itemsArrayList = (ArrayList<NotificationItem>) results.values;
            notifyDataSetChanged();
        }
    }
}

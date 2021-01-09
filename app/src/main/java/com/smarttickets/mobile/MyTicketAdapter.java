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
public class MyTicketAdapter extends ArrayAdapter<TicketItem>  implements Filterable {

    private final Context context;
    private  ArrayList<TicketItem> itemsArrayList;
    private  ArrayList<TicketItem> filterItemsArrayList;

    CustomFilter filter;

    public MyTicketAdapter(Context context, ArrayList<TicketItem> itemsArrayList) {

        super(context, R.layout.my_ticket_layout, itemsArrayList);

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
        View rowView = inflater.inflate(R.layout.my_ticket_layout, parent, false);
        rowView.setBackgroundColor(position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5F5F5"));

        // 3. Get the two text view from the rowView
        TextView mEvent = (TextView) rowView.findViewById(R.id.txtEvent);
        TextView mAmount = (TextView) rowView.findViewById(R.id.txtAmount);
        TextView mEventDate = (TextView) rowView.findViewById(R.id.txtDate);
        TextView mPayClass = (TextView) rowView.findViewById(R.id.txtPayClass);
        TextView mTicketCode = (TextView) rowView.findViewById(R.id.txtTicketCode);

        // 4. Set the text for textView
        System.out.print(position);
        mEvent.setText(itemsArrayList.get(position).getEvent());
        mAmount.setText(itemsArrayList.get(position).getAmount());
        mEventDate.setText(itemsArrayList.get(position).getCategory());
        mEventDate.setText(itemsArrayList.get(position).getEventDate());
        mPayClass.setText(itemsArrayList.get(position).getPayClass());
        mTicketCode.setText(itemsArrayList.get(position).getTicketCode());

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
                List<TicketItem> filters = new ArrayList<TicketItem>();
                //get specific items

                for (final TicketItem ite : filterItemsArrayList) {
                    if (ite.getEvent().toUpperCase().contains(constraint)) {
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
            itemsArrayList = (ArrayList<TicketItem>) results.values;
            notifyDataSetChanged();
        }
    }
}

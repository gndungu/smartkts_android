package com.smarttickets.mobile;

import android.content.Context;
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
public class MyAdapter  extends ArrayAdapter<Item>   implements Filterable {

    private final Context context;
    private  ArrayList<Item> itemsArrayList;
    private  ArrayList<Item> filterItemsArrayList;

    CustomFilter filter;

    public MyAdapter(Context context, ArrayList<Item> itemsArrayList) {

        super(context, R.layout.data_row, itemsArrayList);

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
        View rowView = inflater.inflate(R.layout.data_row, parent, false);

        // 3. Get the two text view from the rowView
        TextView labelView = (TextView) rowView.findViewById(R.id.labelTextView);
        TextView valueView = (TextView) rowView.findViewById(R.id.valueTextView);

        // 4. Set the text for textView
        System.out.print(position);
        labelView.setText(itemsArrayList.get(position).getLabel_view());
        valueView.setText(itemsArrayList.get(position).getValue_view());

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
                List<Item> filters = new ArrayList<Item>();
                //get specific items

                for (final Item ite : filterItemsArrayList) {
                    if (ite.getLabel_view().toUpperCase().contains(constraint)) {
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
            itemsArrayList = (ArrayList<Item>) results.values;
            notifyDataSetChanged();
        }
    }
}

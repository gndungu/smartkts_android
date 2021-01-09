package com.smarttickets.mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gndungu on 7/1/2017.
 */
public class MyOfflineAdapter extends ArrayAdapter<OfflineItem>   implements Filterable {

    private final Context context;
    private  ArrayList<OfflineItem> itemsArrayList;
    private  ArrayList<OfflineItem> filterItemsArrayList;

    CustomFilter filter;

    public MyOfflineAdapter(Context context, ArrayList<OfflineItem> itemsArrayList) {

        super(context, R.layout.ticket_layout_o, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
        this.filterItemsArrayList = itemsArrayList;
    }

    @Override
    public int getCount() {
        return itemsArrayList.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.ticket_layout_o, parent, false);
        rowView.setBackgroundColor(position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5F5F5"));

        // 3. Get the two text view from the rowView
        TextView event = (TextView) rowView.findViewById(R.id.txtOEvent);
        TextView sync_date = (TextView) rowView.findViewById(R.id.txtOSyncDate);
        TextView total_ticket = (TextView) rowView.findViewById(R.id.txtTicketCount);
        TextView instance_code = (TextView) rowView.findViewById(R.id.txtOInstanceCode);
        ImageView myDeleteImg = (ImageView) rowView.findViewById(R.id.imgVDelete);

        // 4. Set the text for textView
        System.out.print(position);
        event.setText(itemsArrayList.get(position).getEventName());
        sync_date.setText(itemsArrayList.get(position).getSyncDate());
        total_ticket.setText(itemsArrayList.get(position).getTotalTickets());
        instance_code.setText(itemsArrayList.get(position).getInstanceCode());

        myDeleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("");
                alertDialogBuilder.setMessage("Are you sure you want to delete " + itemsArrayList.get(position).getEventName() + " ticket data?");
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int sumthin) {
                        Toast.makeText(context, "Deleted cached ticket data for "+itemsArrayList.get(position).getEventName(), Toast.LENGTH_LONG).show();
                        DatabaseHelper dbHelper;
                        SQLiteDatabase db;
                        dbHelper = new DatabaseHelper(context);
                        db = dbHelper.getReadableDatabase();

                        dbHelper = new DatabaseHelper(context);
                        db = dbHelper.getWritableDatabase();
                        db.delete(DatabaseHelper.TABLE_TICKETS, DatabaseHelper.INSTANCECODE + "= \"" + itemsArrayList.get(position).getInstanceCode()+"\"", null);
                        db.delete(DatabaseHelper.TABLE_SCANTICKET, DatabaseHelper.INSTANCECODE + "= \"" + itemsArrayList.get(position).getInstanceCode()+"\"", null);
                        db.close();
                        notifyDataSetChanged();
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();



            }
        });

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
                List<OfflineItem> filters = new ArrayList<OfflineItem>();
                //get specific items

                for (final OfflineItem ite : filterItemsArrayList) {
                    if (ite.getEventName().toUpperCase().contains(constraint)) {
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
            itemsArrayList = (ArrayList<OfflineItem>) results.values;
            notifyDataSetChanged();
        }
    }
}

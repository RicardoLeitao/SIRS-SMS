package ulisboa.tecnico.SIRSsms;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class SMSListAdapter extends ArrayAdapter<SMS> {

   // List context
    private final Context context;
    // List values
    private final List<SMS> smsList;

   public SMSListAdapter(Context context, List<SMS> smsList) {
       super(context, R.layout.activity_main, smsList);
       this.context = context;
       this.smsList = smsList;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
       LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

       View rowView = inflater.inflate(R.layout.inbox_list_row, parent, false);

       TextView senderNumber = (TextView) rowView.findViewById(R.id.numberLbl);
       TextView smsBody = (TextView) rowView.findViewById(R.id.bodyLbl);
       senderNumber.setText(smsList.get(position).getNumber());
       smsBody.setText(smsList.get(position).getBody());
       return rowView;
   }

}
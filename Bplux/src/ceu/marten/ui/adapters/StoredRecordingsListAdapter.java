package ceu.marten.ui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ceu.marten.bplux.R;
import ceu.marten.model.Recording;

import com.haarman.listviewanimations.ArrayAdapter;

public class StoredRecordingsListAdapter extends ArrayAdapter<Recording> {

	private final Context context;

	public StoredRecordingsListAdapter(Context context,
			ArrayList<Recording> recordings) {
		super(recordings);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewGroup rowView = (ViewGroup) convertView;
		if (rowView == null) {
			rowView = (ViewGroup) LayoutInflater.from(context).inflate(
					R.layout.li_recording, parent, false);
		}

		TextView name = (TextView) rowView.findViewById(R.id.dli_name);
		TextView date = (TextView) rowView.findViewById(R.id.dli_date);
		TextView duration = (TextView) rowView.findViewById(R.id.dli_duration);

		Recording recording = getItem(position);
		name.setText(recording.getName());
		date.setText(recording.getSavedDate());
		duration.setText("duration " + recording.getDuration());

		return rowView;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

}
package ceu.marten.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import ceu.marten.bplux.R;
import ceu.marten.model.Configuration;
import ceu.marten.ui.adapters.ActiveChannelsListAdapter;
import ceu.marten.ui.adapters.ChannelsToDisplayListAdapter;

public class NewConfigurationActivity extends Activity {

	private static final int FREQUENCY_MAX = 1000;
	private static final int FREQUENCY_MIN = 36;
	private static final int DEFAULT_FREQUENCY = 500;
	private static final int DEFAULT_NUMBER_OF_BITS = 8;

	private SeekBar frequencySeekbar;
	private EditText frequencyEditor, configurationName, macAddress;
	private TextView activeChannels, channelsToDisplay;

	String[] channelsActivated = null;
	boolean[] channelsSelected = null;

	Configuration newConfiguration;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_new_configuration);

		new InitActivity().execute("");
	}

	private void initializeVariables() {
		newConfiguration = new Configuration();
		newConfiguration.setFrequency(DEFAULT_FREQUENCY);
		newConfiguration.setNumberOfBits(DEFAULT_NUMBER_OF_BITS);
	}

	private void findViews() {
		frequencySeekbar = (SeekBar) findViewById(R.id.freq_seekbar);
		frequencyEditor = (EditText) findViewById(R.id.freq_view);
		configurationName = (EditText) findViewById(R.id.dev_name);
		macAddress = (EditText) findViewById(R.id.nc_mac_address);
		activeChannels = (TextView) findViewById(R.id.nc_txt_active_channels);
		channelsToDisplay = (TextView) findViewById(R.id.nc_txt_channels_to_show);
	}

	private void initializeFrequencyComponents() {
		frequencySeekbar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean changedByUser) {
						if (changedByUser) {
							frequencyEditor.setText(String.valueOf(progress
									+ FREQUENCY_MIN));
							newConfiguration.setFrequency(progress
									+ FREQUENCY_MIN);
						}
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
					}
				});
		frequencyEditor.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView currentView, int actionId,
					KeyEvent event) {
				boolean handled = false;

				if (actionId == EditorInfo.IME_ACTION_DONE) {
					int newFrequency = Integer.parseInt(currentView.getText()
							.toString());

					setFrequency(newFrequency);
					closeKeyboardAndClearFocus();
					handled = true;
				}
				return handled;
			}

			private void setFrequency(int newFrequency) {
				if (newFrequency >= FREQUENCY_MIN
						&& newFrequency <= FREQUENCY_MAX) {
					frequencySeekbar
							.setProgress((newFrequency - FREQUENCY_MIN));
					newConfiguration.setFrequency(newFrequency);
				} else if (newFrequency > FREQUENCY_MAX) {
					frequencySeekbar.setProgress(FREQUENCY_MAX);
					frequencyEditor.setText(String.valueOf(FREQUENCY_MAX));
					newConfiguration.setFrequency(FREQUENCY_MAX);
					displayErrorToast("max frequency is " + FREQUENCY_MAX
							+ "Hz");
				} else {
					frequencySeekbar.setProgress(0);
					frequencyEditor.setText(String.valueOf(FREQUENCY_MIN));
					newConfiguration.setFrequency(FREQUENCY_MIN);
					displayErrorToast("min frequency is " + FREQUENCY_MIN
							+ " Hz");
				}
			}

			private void closeKeyboardAndClearFocus() {
				frequencyEditor.clearFocus();
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

				inputManager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

			}
		});
	}

	private void setupActiveChannelsDialog() {

		// INIT CHANNELS ARRAY
		final ArrayList<String> channels = new ArrayList<String>();
		channels.add(getResources().getString(R.string.channel1));
		channels.add(getResources().getString(R.string.channel2));
		channels.add(getResources().getString(R.string.channel3));
		channels.add(getResources().getString(R.string.channel4));
		channels.add(getResources().getString(R.string.channel5));
		channels.add(getResources().getString(R.string.channel6));
		channels.add(getResources().getString(R.string.channel7));
		channels.add(getResources().getString(R.string.channel8));

		final ActiveChannelsListAdapter activeChannelsListAdapter = new ActiveChannelsListAdapter(
				this, channels);

		AlertDialog.Builder activeChannelsBuilder;
		AlertDialog activeChannelsDialog;
		ListView activeChannelsListView;

		// ACTIVE CHANNELS BUILDER
		activeChannelsBuilder = new AlertDialog.Builder(this);
		activeChannelsBuilder
				.setIcon(R.drawable.select_dialog)
				.setTitle("  Select channels to activate")
				.setView(
						getLayoutInflater().inflate(
								R.layout.dialog_channels_listview, null));

		activeChannelsBuilder.setPositiveButton("accept",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						channelsActivated = activeChannelsListAdapter
								.getChecked();

						newConfiguration.setActiveChannels(channelsActivated);

						if (noChannelsActivated(channelsActivated))
							activeChannels.setText("no channels selected");
						else
							printActivatedChannels(channelsActivated);
					}

					private void printActivatedChannels(
							String[] channelsActivated) {
						activeChannels.setText("channels to activate: ");
						String si = "";
						for (int i = 0; i < channelsActivated.length; i++) {
							if (channelsActivated[i] != null)
								si += (" " + String.valueOf(i + 1) + ",");
						}
						si = (si.substring(0, si.length() - 1));
						activeChannels.append(si);

					}

				});
		activeChannelsBuilder.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		activeChannelsDialog = activeChannelsBuilder.create();
		activeChannelsDialog.show();

		activeChannelsListView = (ListView) activeChannelsDialog
				.findViewById(R.id.lv_channelsSelection);
		activeChannelsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		activeChannelsListView.setItemsCanFocus(false);
		activeChannelsListView.setAdapter(activeChannelsListAdapter);
	}

	private boolean noChannelsActivated(String[] channelsActivated) {
		int counter = 0;
		for (int i = 0; i < channelsActivated.length; i++) {
			if (channelsActivated[i] != null)
				counter++;
		}
		if (counter == 0)
			return true;
		else
			return false;
	}

	private void setupChannelsToDisplay() {

		final ArrayList<String> channels = new ArrayList<String>();
		final ArrayList<String> sensors = new ArrayList<String>();

		// FILL THE TWO ARRAYS
		for (int i = 0; i < newConfiguration.getActiveChannelsWithNullFill().length; i++) {
			if (newConfiguration.getActiveChannelsWithNullFill()[i]
					.compareTo("null") != 0) {
				channels.add("channel " + (i + 1));
				sensors.add(newConfiguration.getActiveChannelsWithNullFill()[i]);
			}
		}

		final ChannelsToDisplayListAdapter channelsToDisplayListAdapter = new ChannelsToDisplayListAdapter(
				this, channels, sensors);
		AlertDialog.Builder channelsToDisplayBuilder;
		AlertDialog channelsToDisplayDialog;

		// BUILDER
		channelsToDisplayBuilder = new AlertDialog.Builder(this);
		channelsToDisplayBuilder.setIcon(R.drawable.select_dialog);
		channelsToDisplayBuilder.setTitle("  Select channels to display");
		channelsToDisplayBuilder.setView(getLayoutInflater().inflate(
				R.layout.dialog_channels_listview, null));

		channelsToDisplayBuilder.setPositiveButton("accept",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						channelsSelected = channelsToDisplayListAdapter
								.getChecked();
						boolean[] channelsToDisplayArray = new boolean[8];

						if (numberOfChannelsSelected(channelsSelected) > 2)
							displayErrorToast("channels to display have to be less than 3");
						else {
							channelsToDisplay.setText("channels to display: ");
							printChannelsToDisplay(channelsToDisplayArray,
									channelsSelected);
							newConfiguration
									.setChannelsToDisplay(channelsToDisplayArray);
						}

					}

					private void printChannelsToDisplay(
							boolean[] channelsToDisplayArray,
							boolean[] channelsSelected) {
						String si = "";
						for (int i = 0; i < channelsSelected.length; i++) {
							if (channelsSelected[i]) {
								int in = Character.getNumericValue((channels
										.get(i).toString().charAt(channels
										.get(i).toString().length() - 1)) - 1);
								channelsToDisplayArray[in] = true;

								si = si + "\n\t" + channels.get(i).toString()
										+ " with sensor "
										+ sensors.get(i).toString();
							}
						}
						channelsToDisplay.append(si);

					}

				});
		channelsToDisplayBuilder.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		// CREATE DIALOG
		channelsToDisplayDialog = channelsToDisplayBuilder.create();
		channelsToDisplayDialog.show();

		// LIST VIEW CONFIGURATION
		ListView channelsToDisplayListView;
		channelsToDisplayListView = (ListView) channelsToDisplayDialog
				.findViewById(R.id.lv_channelsSelection);
		channelsToDisplayListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		channelsToDisplayListView.setItemsCanFocus(false);
		channelsToDisplayListView.setAdapter(channelsToDisplayListAdapter);

	}

	private int numberOfChannelsSelected(boolean[] channelsSelected) {
		int counter = 0;
		for (int i = 0; i < channelsSelected.length; i++) {
			if (channelsSelected[i]) {
				counter++;
			}
		}
		return counter;
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		newConfiguration.setName(configurationName.getText().toString());
		newConfiguration.setMacAddress(macAddress.getText().toString());

		savedInstanceState.putSerializable("configuration", newConfiguration);
		savedInstanceState.putString("activeChannels", activeChannels.getText()
				.toString());
		savedInstanceState.putString("channelsToDisplay", channelsToDisplay
				.getText().toString());

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		newConfiguration = (Configuration) savedInstanceState
				.getSerializable("configuration");
		activeChannels.setText(savedInstanceState.getString("activeChannels"));
		channelsToDisplay.setText(savedInstanceState
				.getString("channelsToDisplay"));
	}

	private void displayErrorToast(String messageToDisplay) {
		Toast errorToast = new Toast(getApplicationContext());

		LayoutInflater inflater = getLayoutInflater();
		View toastView = inflater.inflate(R.layout.toast_error, null);
		errorToast.setView(toastView);
		((TextView) toastView.findViewById(R.id.display_text))
				.setText(messageToDisplay);

		errorToast.show();
	}

	private void displayInfoToast(String messageToDisplay) {
		Toast infoToast = new Toast(getApplicationContext());

		LayoutInflater inflater = getLayoutInflater();
		View toastView = inflater.inflate(R.layout.toast_info, null);
		infoToast.setView(toastView);
		((TextView) toastView.findViewById(R.id.display_text))
				.setText(messageToDisplay);

		infoToast.show();
	}

	public void onClickedSubmit(View view) {

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();

		newConfiguration.setCreateDate(dateFormat.format(date));
		newConfiguration.setName(configurationName.getText().toString());
		newConfiguration.setMacAddress(macAddress.getText().toString());
		if (validateFields()) {
			Intent returnIntent = new Intent();
			returnIntent.putExtra("configuration", newConfiguration);
			setResult(RESULT_OK, returnIntent);
			finish();

			displayInfoToast("configuration successfully created");
		}

	}

	public void onClickedCancel(View view) {
		displayInfoToast("configuration canceled");
		finish();
	}

	public void onRadioButtonClicked(View radioButtonView) {
		boolean checked = ((RadioButton) radioButtonView).isChecked();

		switch (radioButtonView.getId()) {
		case R.id.radioBttn8:
			if (checked)
				newConfiguration.setNumberOfBits(8);
			break;
		case R.id.radioBttn12:
			if (checked)
				newConfiguration.setNumberOfBits(12);
			break;
		}
	}

	public void onClickedChannelPickDialog(View view) {
		setupActiveChannelsDialog();
	}

	public void onClickedChannelDisplayPickDialog(View view) {
		String[] channelsActivated = newConfiguration
				.getActiveChannelsWithNullFill();
		if (channelsActivated != null) {
			int counter = 0;
			for (int i = 0; i < channelsActivated.length; i++) {
				if (channelsActivated[i].compareTo("null") != 0) {
					counter++;
				}
			}

			if (counter != 0)
				setupChannelsToDisplay();
			else
				displayErrorToast("please select active channels first");
		} else
			displayErrorToast("please select active channels first");
	}

	private boolean validateFields() {
		boolean validated = true;
		String errorMessage = "";
		// VALIDATE NAME FIELD
		if (configurationName.getText().toString() == null
				|| configurationName.getText().toString().compareTo("") == 0) {
			errorMessage += (" *invalid configuration name\n");
			validated = false;
		}

		String regex = "^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$";
		// VALIDATE MAC FIELD
		if (macAddress.getText().toString() == null
				|| macAddress.getText().toString().compareTo("") == 0
				|| !macAddress.getText().toString().matches(regex)
				&& macAddress.getText().toString().compareTo("test") != 0) {
			errorMessage += (" *invalid mac address\n");
			validated = false;
		}

		// VALIDATE ACTIVE CHANNELS
		if (channelsActivated == null || noChannelsActivated(channelsActivated)) {
			errorMessage += (" *active channels not selected \n");
			validated = false;
		}

		// VALIDATE CHANNELS TO DISPLAY
		if (channelsSelected == null
				|| numberOfChannelsSelected(channelsSelected) == 0) {
			errorMessage += (" *channels to display not selected\n");
			validated = false;
		}

		if (!validated) {
			errorMessage = errorMessage.substring(0, errorMessage.length() - 1);
			displayErrorToast(errorMessage);
		}

		return validated;
	}

	private class InitActivity extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			initializeVariables();
			findViews();
			initializeFrequencyComponents();
			return "execuded";
		}

		@Override
		protected void onPostExecute(String result) {

		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
}

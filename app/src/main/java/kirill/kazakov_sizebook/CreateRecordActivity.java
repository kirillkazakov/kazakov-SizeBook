package kirill.kazakov_sizebook;

/**
 * Created by Kirill Kazakov on 2017-02-04.
 */


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by kazakov on 2017-02-04.
 */


/**
 * This class creates the values for the various app records as specified by the user. It returns doubles
 * for the numerical values. For the date field there is a calendar implementation, where the user
 * picks a date from an Android calendar widget. It also implements the ability for the user to edit
 * previous text fields. It saves the edits to a file name "file.sav".
 */

public class CreateRecordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String FILENAME = "file.sav";
    private ArrayList<Record> recordList = new ArrayList<>();
    private Record newRecord;
    private Calendar newRecordDate;
    private EditText personNameEditText;
    private EditText recordDateEditText;
    private EditText neckSizeEditText;
    private EditText bustSizeEditText;
    private EditText chestSizeEditText;
    private EditText waistSizeEditText;
    private EditText hipSizeEditText;
    private EditText inseamSizeEditText;
    private EditText commentEditText;
    private int index = -1;

    private Button completeRecordButton;

    /**
     * This overrides the initial onCreate from MainActivity as we would like to update the app
     * with the values that the user requires. saveInstanceState is called so that we may edit the previous
     * edits updates on the app so that the user does not lose his data.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_record);

        // setup each EditText and their onClickListner
        personNameEditText = (EditText) findViewById(R.id.person_name);

        recordDateEditText = (EditText) findViewById(R.id.record_date);
        recordDateEditText.setFocusable(false);
        recordDateEditText.setOnClickListener(this);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");

        neckSizeEditText = (EditText) findViewById(R.id.neck_size);
        bustSizeEditText = (EditText) findViewById(R.id.bust_size);
        chestSizeEditText = (EditText) findViewById(R.id.chest_size);
        waistSizeEditText = (EditText) findViewById(R.id.waist_size);
        hipSizeEditText = (EditText) findViewById(R.id.hip_size);
        inseamSizeEditText = (EditText) findViewById(R.id.inseam_size);
        commentEditText = (EditText) findViewById(R.id.comment_size);

        completeRecordButton = (Button) findViewById(R.id.complete);
        completeRecordButton.setOnClickListener(this);

        //This is where the widget DatePicker is implemented and where it gets the date
        recordDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newRecordDate = Calendar.getInstance();
                new DatePickerDialog(CreateRecordActivity.this, onDateSetListener,
                        newRecordDate.get(Calendar.YEAR), newRecordDate.get(Calendar.MONTH),
                        newRecordDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        loadAllRecord();
        //Receive the associated data with the user's inputs to display them
        Gson gson = new Gson();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String jsonStr = extras.getString("EDIT");
            newRecord = gson.fromJson(jsonStr, Record.class);
            index = getIndex(newRecord);
            newRecordDate = newRecord.getDate();
            personNameEditText.setText(newRecord.getPersonName());
            if (newRecord.getDate() != null) {
                recordDateEditText.setText(sdf.format(newRecordDate.getTime()));
            }
            if (newRecord.getNeckSize() != 0) {
                neckSizeEditText.setText(Double.toString(newRecord.getNeckSize()));
            }
            if (newRecord.getBustSize() != 0) {
                bustSizeEditText.setText(Double.toString(newRecord.getBustSize()));
            }
            if (newRecord.getChestSize() != 0) {
                chestSizeEditText.setText(Double.toString(newRecord.getChestSize()));
            }
            if (newRecord.getWaistSize() != 0) {
                waistSizeEditText.setText(Double.toString(newRecord.getWaistSize()));
            }
            if (newRecord.getHipSize() != 0) {
                hipSizeEditText.setText(Double.toString(newRecord.getHipSize()));
            }
            if (newRecord.getInseamSize() != 0) {
                inseamSizeEditText.setText(Double.toString(newRecord.getInseamSize()));
            }
            if (newRecord.getComment() != null) {
                commentEditText.setText(newRecord.getComment());
            }
        }
    }

    /**
     * Creates the record when the user presses the complete button
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == completeRecordButton) {
            createRecord();

        }
    }


    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        /**
         *Will override if the user wants to edit the date
         * Also sets the chosen date with what the user picked
         * @param view
         * @param year
         * @param monthOfYear
         * @param dayOfMonth
         */
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            newRecordDate.set(Calendar.YEAR, year);
            newRecordDate.set(Calendar.MONTH, monthOfYear);
            newRecordDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            recordDateEditText.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
        }
    };

    /**
     *Creates the Record as intended by the user input
     */
    protected void createRecord() {
        Intent intent = new Intent(this, MainActivity.class);
        // Get record name from user input
        String personName = personNameEditText.getText().toString();
        Record newRecord = new Record();
        // if record name is not given
        if (personName.isEmpty()) {
            personNameEditText.setError("Person's name is empty!");
            return;
        }

        if (checkPersonName(personName)) {
            personNameEditText.setError("The person already exits!");
            return;
        }
        if (!neckSizeEditText.getText().toString().isEmpty()) {
            // if neckSize isn't positive
            double neckSize =parseDouble(neckSizeEditText.getText().toString());
            if (!checkSize(neckSize, neckSizeEditText)) return;
            newRecord.setNeckSize(neckSize);
        }
        if (!bustSizeEditText.getText().toString().isEmpty()) {
            // if bustSize isn't positive
            double bustSize = parseDouble(bustSizeEditText.getText().toString());
            if (!checkSize(bustSize, bustSizeEditText)) return;
            newRecord.setBustSize(bustSize);
        }
        if (!chestSizeEditText.getText().toString().isEmpty()) {
            // if chestSize isn't positive
            double chestSize = parseDouble(chestSizeEditText.getText().toString());
            if (!checkSize(chestSize, chestSizeEditText)) return;
            newRecord.setChestSize(chestSize);
        }
        if (!waistSizeEditText.getText().toString().isEmpty()) {
            // if WaistSize isn't positive
            double waistSize = parseDouble(waistSizeEditText.getText().toString());
            if (!checkSize(waistSize, waistSizeEditText)) return;
            newRecord.setWaistSize(waistSize);
        }
        if (!hipSizeEditText.getText().toString().isEmpty()) {
            // if HipSize isn't positive
            double hipSize = parseDouble(hipSizeEditText.getText().toString());
            if (!checkSize(hipSize, hipSizeEditText)) return;
            newRecord.setHipSize(hipSize);
        }
        if (!inseamSizeEditText.getText().toString().isEmpty()) {
            // if InseamSize isn't positive
            double inseamSize = parseDouble(inseamSizeEditText.getText().toString());
            if (!checkSize(inseamSize, inseamSizeEditText)) return;
            newRecord.setInseamSize(inseamSize);
        }
        if (!commentEditText.getText().toString().isEmpty()) {
            // if InseamSize isn't positive
            String comment = commentEditText.getText().toString();
            newRecord.setComment(comment);
        }
        if (!recordDateEditText.getText().toString().isEmpty()) {
            newRecord.setDate(newRecordDate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
        }
        try {
            newRecord.setPersonName(personName);
            if (index != -1) {
                recordList.set(index, newRecord);
            } else {
                recordList.add(newRecord);
            }
            saveInFile();
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

    /**
     * Returns a true or false based on if the user inputs a positive number or
     * a negative one.
     * @param checkNumber
     * @param editText
     * @return
     */
    private Boolean checkSize(double checkNumber, EditText editText) {
        if (checkNumber > 0 ){
            editText.setText(Double.toString(checkNumber));
            return true;
        }
        editText.setError("Input Number must be positive number!");
        return false;
    }

    /**
     * Saves the data on file and throws an exception
     * if the file is not found
     */
    private void saveInFile() {
        try {
            FileOutputStream fos = openFileOutput(FILENAME,
                    Context.MODE_PRIVATE);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));

            Gson gson = new Gson();

            gson.toJson(recordList, out);

            out.flush();

            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
        }

    }

    /**
     * Checks if the user input is a number or a decimal number
     * @param str
     * @return
     */
    private double parseDouble(String str) {
        double num = 0;
        try {
            num = Double.parseDouble(str);
            DecimalFormat df = new DecimalFormat("#,##0.0");
            num = Double.valueOf(df.format(num));
            return num;
        } catch (NumberFormatException e) {
            Toast toast = Toast.makeText(this, "Expect a number", Toast.LENGTH_SHORT);
            toast.show();
        }
        return num;
    }

    /**
     * Loads the records from the file.
     * Code follows the steps as shown from the Labs using the LonelyTwitter activity app.
     */
    private void loadAllRecord() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            Gson gson = new Gson();
            // Taken from http://stackoverflow.com/questions/12384064/gson-convert-from-json-to-a-typed-arraylistt
            // 2017-01-26

            recordList = gson.fromJson(in, new TypeToken<ArrayList<Record>>(){}.getType());
            fis.close();
        } catch (FileNotFoundException e) {
            recordList = new ArrayList<Record>();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        }
    }

    /**
     * Returns the index of the person.
     * @param record
     * @return
     */
    private int getIndex(Record record) {
        int index = 0;
        for (Record r : recordList) {
            if (r.getPersonName().equals(record.getPersonName())) {
                break;
            }
            ++index;
        }
        return index;
    }

    /**
     * Checks for person name so that we know if we are editing the right person.
     * @param name
     * @return
     */
    private Boolean checkPersonName(String name){
        for (Record r : recordList) {
            if (r.getPersonName().equals(name)) {
                if (index != -1) {
                    if (newRecord.getPersonName().equals(name)) continue;
                }
                return true;
            }
        }
        return false;
    }
}

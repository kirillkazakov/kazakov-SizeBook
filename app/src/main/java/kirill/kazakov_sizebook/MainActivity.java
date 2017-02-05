package kirill.kazakov_sizebook;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * This MainActivity class implements the views that the user will see when he opens the app.
 * It displays the record list along with the total count of people. It also gives the user options
 * if he wants to either delete a record or edit one.
 * It then saves the changes to a "file.sav".
 */
public class MainActivity extends AppCompatActivity {

    private static final String FILENAME = "file.sav";
    private ArrayList<Record> recordList = new ArrayList<>();
    private ArrayAdapter<Record> adapter;
    private ListView oldRecordList;
    private TextView countTextView;

    /**
     * Displays the view of the last state of which the app had before it closed.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button createButton = (Button) findViewById(R.id.create);

        oldRecordList = (ListView) findViewById(R.id.oldRecordList);
        countTextView = (TextView) findViewById(R.id.count_of_people);


        oldRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Opens the correct record which the user wants to examine
             * @param parent
             * @param view
             * @param position
             * @param id
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openTheRecord(position);

            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateRecordActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Display the count on start along with any records that were stored by the user
     * since we also want persistence
     */
    @Override
    protected void onStart() {
        super.onStart();
        adapter = new ArrayAdapter<Record>(this,
                R.layout.list_item, recordList);
        oldRecordList.setAdapter(adapter);
        loadAllRecord();
        countTextView.setText(String.format("Count: %s", recordList.size()));
    }

    /**
     *Load the records when the user reopen the app
     */
    @Override
    protected void onResume() {
        super.onResume();
        recordList.clear();
        loadAllRecord();

        adapter.notifyDataSetChanged();
    }


    /**
     * Open the selected record and give the user options to either delete the record or
     * edit it.
     */
    private void openTheRecord(final int position) {
        Record editRecord = recordList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Edit record")
                .setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.remove(recordList.get(position));
                        recordList.remove(position);
                        adapter.notifyDataSetChanged();
                        saveInFile();
                        countTextView.setText(String.format("Count: %s", recordList.size()));

                    }
                })
                .setPositiveButton("VIEW DETAIL/EDIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, CreateRecordActivity.class);
                        Gson gson = new Gson();
                        String recordStr = gson.toJson(recordList.get(position));
                        intent.putExtra("EDIT",recordStr);
                        startActivity(intent);
                    }
                });


        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * Load all the records for displaying as inspired the the LonelyTwitter app
     */
    private void loadAllRecord() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            Gson gson = new Gson();
            // Taken from http://stackoverflow.com/questions/12384064/gson-convert-from-json-to-a-typed-arraylistt
            // 2017-01-26

            recordList = gson.fromJson(in, new TypeToken<ArrayList<Record>>(){}.getType());
            adapter.clear();
            adapter.addAll(recordList);
            adapter.notifyDataSetChanged();
            fis.close();
        } catch (FileNotFoundException e) {
            recordList = new ArrayList<Record>();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Save the records to file as inspired by the LonelyTwitter app
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

}
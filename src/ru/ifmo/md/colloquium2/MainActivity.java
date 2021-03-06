package ru.ifmo.md.colloquium2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    class NewCandidateListener implements DialogInterface.OnClickListener {
        EditText name;
        Context mainContext;

        public NewCandidateListener(EditText name, Context c) {
            this.name = name;
            mainContext = c;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Cursor c = getContentResolver().query(CandidatesProvider.CONTENT_URI, null,
                               CandidatesProvider.NAME + "=?", new String[]{name.getText().toString()}, null);
            if (c.getCount() != 0) {
                Toast t = Toast.makeText(mainContext, "Candidate with this name exists", Toast.LENGTH_LONG);
                t.show();
                return;
            }
            if (name.getText().toString().length() == 0) {
                Toast t = Toast.makeText(mainContext, "Empty name", Toast.LENGTH_LONG);
                t.show();
                return;
            }
            c.close();
            ContentValues cv = new ContentValues();
            cv.put(CandidatesProvider.NAME, name.getText().toString());
            cv.put(CandidatesProvider.VOTES, 0);
            getContentResolver().insert(CandidatesProvider.CONTENT_URI, cv);
        }
    }

    class RenameCandidateListener implements DialogInterface.OnClickListener {
        EditText name;
        Context mainContext;
        int id;

        public RenameCandidateListener(EditText name, Context c, int id) {
            this.name = name;
            mainContext = c;
            this.id = id;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Cursor c = getContentResolver().query(CandidatesProvider.CONTENT_URI, null,
                    CandidatesProvider.NAME + "=?", new String[]{name.getText().toString()}, null);
            if (c.getCount() > 1) {
                Toast t = Toast.makeText(mainContext, "Candidate with this name exists", Toast.LENGTH_LONG);
                t.show();
                return;
            }
            if (name.getText().toString().length() == 0) {
                Toast t = Toast.makeText(mainContext, "Empty name", Toast.LENGTH_LONG);
                t.show();
                return;
            }
            c.close();
            ContentValues cv = new ContentValues();
            cv.put(CandidatesProvider.NAME, name.getText().toString());
            cv.put(CandidatesProvider.VOTES, 0);
            getContentResolver().update(CandidatesProvider.CONTENT_URI, cv, CandidatesProvider._ID + "=?",
                    new String[]{((Integer)id).toString()});
        }
    }

    class MyListener implements AdapterView.OnItemLongClickListener {
        Context context;

        public MyListener(Context c) {
            context = c;
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (!voteInProgress) {
                LinearLayout ll = (LinearLayout) view;
                String oldName = ((TextView) ll.findViewById(R.id.text1)).getText().toString();
                Cursor c = getContentResolver().query(CandidatesProvider.CONTENT_URI, null,
                        CandidatesProvider.NAME + "=?", new String[]{oldName}, null);
                c.moveToNext();
                int id = c.getInt(c.getColumnIndex(CandidatesProvider._ID));
                c.close();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Rename candidate");
                final TextView nameLabel = new TextView(context);
                nameLabel.setText("New name:");
                final EditText nameField = new EditText(context);
                nameField.setText("");
                builder.setView(nameLabel);
                builder.setView(nameField);
                LinearLayout ll1 = new LinearLayout(context);
                ll1.setOrientation(LinearLayout.VERTICAL);
                ll1.addView(nameLabel);
                ll1.addView(nameField);
                builder.setView(ll1);
                builder.setPositiveButton("OK", new RenameCandidateListener(nameField, context, id));
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
            return true;
        }
    }

    boolean voteInProgress;
    Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        voteInProgress = false;
        try {
            if (!savedInstanceState.getBoolean("cleared"))
                getContentResolver().delete(CandidatesProvider.CONTENT_URI, null, null);
            savedInstanceState.putBoolean("cleared", true);
        } catch (Exception e) {
            getContentResolver().delete(CandidatesProvider.CONTENT_URI, null, null);
        }

        ListView lv = (ListView) findViewById(R.id.list);
        Cursor c = getContentResolver().query(CandidatesProvider.CONTENT_URI, null, null, null, CandidatesProvider._ID);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_item, c,
                 new String[]{CandidatesProvider.NAME, CandidatesProvider.VOTES},
                new int[]{R.id.text1, R.id.text2});
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (voteInProgress) {
                    Cursor c = getContentResolver().query(CandidatesProvider.CONTENT_URI, null,
                            CandidatesProvider._ID + "=" + Long.toString(id), null, null);
                    c.moveToNext();
                    ContentValues cv = new ContentValues();
                    cv.put(CandidatesProvider.NAME, c.getString(c.getColumnIndex(CandidatesProvider.NAME)));
                    cv.put(CandidatesProvider.VOTES, c.getInt(c.getColumnIndex(CandidatesProvider.VOTES)) + 1);
                    getContentResolver().update(CandidatesProvider.CONTENT_URI, cv,
                            CandidatesProvider._ID + "=" + Long.toString(id), null);
                    c.close();
                } else {
                    LinearLayout ll = (LinearLayout) view;
                    TextView nameView = (TextView) ll.findViewById(R.id.text1);
                    getContentResolver().delete(CandidatesProvider.CONTENT_URI,
                            CandidatesProvider.NAME + "=?", new String[]{nameView.getText().toString()});
                }
            }
        });
        lv.setOnItemLongClickListener(new MyListener(this));
    }

    protected void onSaveInstanceState(Bundle onOrientChange) {
        super.onSaveInstanceState(onOrientChange);
        onOrientChange.putBoolean("cleared", true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                if (!voteInProgress) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Add new candidate");
                    final TextView nameLabel = new TextView(this);
                    nameLabel.setText("Name:");
                    final EditText nameField = new EditText(this);
                    nameField.setText("Unnamed");
                    builder.setView(nameLabel);
                    builder.setView(nameField);
                    LinearLayout ll = new LinearLayout(this);
                    ll.setOrientation(LinearLayout.VERTICAL);
                    ll.addView(nameLabel);
                    ll.addView(nameField);
                    builder.setView(ll);
                    builder.setPositiveButton("OK", new NewCandidateListener(nameField, this));
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
                return true;
            case R.id.ready:
                if (!voteInProgress) {
                    MenuItem add = menu.findItem(R.id.add);
                    add.setVisible(false);
                    voteInProgress = true;
                }
                else {
                    startActivity(new Intent(this, ResultActivity.class));

                    MenuItem add = menu.findItem(R.id.add);
                    add.setVisible(true);
                    voteInProgress = false;
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

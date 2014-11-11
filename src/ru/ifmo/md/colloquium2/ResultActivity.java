package ru.ifmo.md.colloquium2;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

public class ResultActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ListView lv = (ListView) findViewById(R.id.list);
        Cursor c = getContentResolver().query(CandidatesProvider.CONTENT_URI, null, null, null, CandidatesProvider.VOTES);
        int n = 0, maxN = 0;
        while (c.moveToNext()) {
            n += c.getInt(c.getColumnIndex(CandidatesProvider.VOTES));
            maxN = Math.max(c.getInt(c.getColumnIndex(CandidatesProvider.VOTES)), maxN);
        }
        c.moveToFirst();
        lv.setAdapter(new ResultAdapter(this, R.layout.list_item_result, c,
                new String[]{CandidatesProvider.NAME, CandidatesProvider.VOTES},
                new int[]{R.id.text1, R.id.text2}, n, maxN));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

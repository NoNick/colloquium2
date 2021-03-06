package ru.ifmo.md.colloquium2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ResultAdapter extends SimpleCursorAdapter {
    private int votesN, maxVotes;

    public ResultAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int N, int mx) {
        super(context, layout, c, from, to);
        votesN = N;
        maxVotes = mx;
    }

    @Override
    public void setViewText (TextView view, String text) {
        try {
            int votes = Integer.parseInt(text);
            view.setText("Votes: " + text + "(" + votes * 100 / votesN + "%)");
            if (votes == maxVotes)
                view.setTextColor(Color.RED);
        }
        catch (Exception e) {
            view.setText(text);
        }
    }
}
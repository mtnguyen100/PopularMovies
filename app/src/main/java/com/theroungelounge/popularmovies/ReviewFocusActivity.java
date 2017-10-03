package com.theroungelounge.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ReviewFocusActivity extends AppCompatActivity {

    private String[] reviewData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_focus);
        Intent reviewFocusIntent = getIntent();

        reviewData = reviewFocusIntent.getStringArrayExtra(Intent.EXTRA_TEXT);

        TextView authorTextView = (TextView) findViewById(R.id.author_textview);
        TextView contentTextView = (TextView) findViewById(R.id.content_textview);

        authorTextView.setText(reviewData[0]);
        contentTextView.setText(reviewData[1]);
    }
}

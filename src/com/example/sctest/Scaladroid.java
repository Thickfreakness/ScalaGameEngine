package com.example.sctest;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Scaladroid extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scaladroid);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scaladroid, menu);
		return true;
	}

}

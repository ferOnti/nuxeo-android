package org.nuxeo.android.testsfrag;

import org.nuxeo.android.automationsample.R;
import org.nuxeo.android.automationsample.R.id;
import org.nuxeo.android.automationsample.R.layout;
import org.nuxeo.android.documentprovider.DocumentProviderSampleFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class BasicFragActivity extends FragmentActivity {

	public final static int CONNECT = 0;
	public final static int FETCH_DOCUMENT = 1;
	public final static int CONTENT_PROVIDER = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_basic_frag);
		

		FragmentTransaction listTransaction = getSupportFragmentManager().beginTransaction();
		Intent callingIntent = getIntent();
		if(callingIntent.getIntExtra("frag", 0) == CONNECT) {
			listTransaction.replace(R.id.basic_fragment_container, new ConnnectSampleFragment());
		} else if(callingIntent.getIntExtra("frag", 0) == FETCH_DOCUMENT) {
			listTransaction.replace(R.id.basic_fragment_container, new SimpleFetchSampleFragment());
		}
		listTransaction.commit();
		
	}

}
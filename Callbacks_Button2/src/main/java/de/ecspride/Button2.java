package de.ecspride;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @testcase_name Button2
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail siegfried.rasthofer@cased.de
 * 
 * @description Sources and sinks are called in button callbacks. There is only one data leak iff first button3 and then button1 is pressed!
 * @dataflow clickOnButton3: source -> imei; onClick (button1): imei -> sinks 
 * @number_of_leaks 3
 * @challenges the analysis must be able to analyze listeners, know that callback of button3 is ClickOnButton3 (defined in xml file)
 *  and has to handle the arbitrary order of the listener callbacks.
 */
public class Button2 extends Activity {
	private String creditCardNumber = null;
	SQLiteDatabase db;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button2);
        
        Button button1= (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				Log.i("TAG", "Button3: " + creditCardNumber); //sink, potential leak
				String query= "select * from USERS where CARD_NO = '"+ creditCardNumber;
				db.execSQL(query); //sink, potential leak
		    }
		});
		
		
		Button button2= (Button) findViewById(R.id.button2);
		button2.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        String[] projection = {"*"};
		        String[] arguments ={creditCardNumber};
				Cursor c = db.query("USERS", projection, "CARD_NO = ?",arguments,creditCardNumber,null,"desc");//sink, no leak
		    }
		});
    }

    public void clickOnButton3(View view){
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		EditText editText = (EditText) findViewById(R.id.creditCardInput);
		creditCardNumber = editText.getText().toString();	 //source
		Log.i("TAG", "Button3: " + creditCardNumber); //sink, leak
	}
}

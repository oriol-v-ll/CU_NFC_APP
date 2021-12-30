package cat.urv.deim.cu.cu_nfc_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    public static final String ERROR_DETECTED = "No s'ha detectat cap TAG";
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;
    private String text_NFC;
    TextView tvNFCContent;
    TextView message;
    Button btnWrite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        tvNFCContent = (TextView) findViewById(R.id.nfc_contents);
        message = (TextView) findViewById(R.id.edit_message);
        btnWrite = (Button) findViewById(R.id.button);

        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (myTag == null) {
                        Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                    } else {
                        //Nomes per les opcions de desenvolupador
                        write(message.getText().toString(), myTag);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FormatException e) {
                    e.printStackTrace();
                }
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Aquest dispositiu no soporta NFC", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }


    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;
        try {
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        tvNFCContent.setText("NFC Content: " + text);
        //Aqui es on aniria les dades al realtime database
        text_NFC=text;


    }

    private void enviarFirebaseSortida(){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://computacioubiquoa-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference myRef = database.getReference("entrades");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss ");
        String currentDateandTime = sdf.format(new Date());
        /* Ho deixo comentat
            1r creem un mapa clau valor en el que guardarem el temps actual com a clau
            i després el nom de la "sala" a la que accedim
            2n agafem la referencia a la base de dades i accedim al "child" que
            correspon al mail de l'usuari
            3r li fem un push per afegir sense sobrescriure
         */
        EditText text = (EditText)findViewById(R.id.editTextTextEmailAddress);
        String email = text.getText().toString();
        EditText txt = (EditText)findViewById(R.id.editTextTextPersonName);
        String name = txt.getText().toString();
        Map<String, String> entrada = new HashMap<>();
        entrada.put("Sortida", currentDateandTime);
        String Sortida = "Sortida";
        myRef.child(name).child(Sortida).setValue(currentDateandTime);

    }

    private void enviarFirebaseEntrada(){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://computacioubiquoa-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference myRef = database.getReference("entrades");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        String currentDateandTime = sdf.format(new Date());
        /* Ho deixo comentat
            1r creem un mapa clau valor en el que guardarem el temps actual com a clau
            i després el nom de la "sala" a la que accedim
            2n agafem la referencia a la base de dades i accedim al "child" que
            correspon al mail de l'usuari
            3r li fem un push per afegir sense sobrescriure
         */
        EditText text = (EditText)findViewById(R.id.editTextTextEmailAddress);
        String email = text.getText().toString();
        EditText txt = (EditText)findViewById(R.id.editTextTextPersonName);
        String name = txt.getText().toString();
        Map<String, String> entrada = new HashMap<>();
        entrada.put("Sala", text_NFC);
        entrada.put("email", email);
        entrada.put("Entrada", currentDateandTime);
        entrada.put("Sortida", "");
        myRef.child(name).setValue(entrada);


    }

    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }



    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();

        findViewById(R.id.Entrada).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarFirebaseEntrada();
            }
        });
        findViewById(R.id.Sortida).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarFirebaseSortida();
            }
        });
    }

    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }
}
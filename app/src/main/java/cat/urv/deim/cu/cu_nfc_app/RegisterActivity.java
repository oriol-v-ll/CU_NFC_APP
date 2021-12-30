package cat.urv.deim.cu.cu_nfc_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }


    @Override
    public void onResume(){
        super.onResume();


        findViewById(R.id.Registrar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent firestore = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(firestore);
                finish();
            }
        });

    }
}
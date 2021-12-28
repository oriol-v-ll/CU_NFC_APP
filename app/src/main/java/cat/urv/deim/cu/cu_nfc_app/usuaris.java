package cat.urv.deim.cu.cu_nfc_app;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class usuaris {

    public String username;
    public String email;
    public String aula;
    public String time;


    public usuaris(){

    }

    public usuaris(String username, String email, String aula, String time) {
        this.username = username;
        this.email = email;
        this.aula = aula;
        this.time = time;
    }

}

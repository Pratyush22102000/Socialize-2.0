package com.example.dummychatapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private ListView listview;

    private Button button;

    private DatabaseReference databaseReference;

    private String stringMessage;

    private final byte[] encryptionKey = {9,115,51,86,105,4,-31,-23,-68,-88,17,20,3,-105,119,-53};
    private Cipher cipher;
    private Cipher decipher;
    private SecretKeySpec secretKeySpec;

//    public MainActivity(DatabaseReference databaseReference, Cipher decipher) {
//        this.databaseReference = databaseReference;
//        this.decipher = decipher;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editTextTextMultiLine);
        listview = findViewById(R.id.listView);
        button = findViewById(R.id.button2);

        databaseReference = FirebaseDatabase.getInstance().getReference("Message");

        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        try {
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        secretKeySpec = new SecretKeySpec(encryptionKey,"AES");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.getValue() != null) {
                    stringMessage = Objects.requireNonNull(snapshot.getValue()).toString();
                    stringMessage = stringMessage.substring(1, stringMessage.length() - 1);


                    String[] stringMessageArray = stringMessage.split(", ");

                    String[] stringFinal = new String[stringMessageArray.length * 2];
                    for (int i = 0; i < stringMessageArray.length; i++) {

                        String[] stringKeyValue = stringMessageArray[i].split("=", 2);
                        try {
                            stringFinal[2 * i] = (String) android.text.format.DateFormat.format("dd-MM-YYYY hh:mm:ss", Long.parseLong(stringKeyValue[0]));

                            stringFinal[2 * i + 1] = AESDecryptionMethod(stringKeyValue[1]);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        listview.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, stringFinal));
                    }
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void sendButton(View view){
            Date date = new Date();
            databaseReference.child(Long.toString(date.getTime())).setValue(AESEncryptionMethod(editText.getText().toString()));
            editText.setText("");
    }
    private String AESEncryptionMethod(String string){
        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
        String returnString=null;
        returnString = new String(encryptedByte, StandardCharsets.ISO_8859_1);
        return returnString;
    }
    private String AESDecryptionMethod(String string) throws UnsupportedEncodingException {
        byte[] EncryptedByte = string.getBytes(StandardCharsets.ISO_8859_1);
        String decryptedString="";
//        decryptedString = string;
        byte[] decryption;
        try {
//            decipher.getIV();
            decipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return decryptedString;
    }
}
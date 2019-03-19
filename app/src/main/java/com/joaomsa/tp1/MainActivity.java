package com.joaomsa.tp1;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public Person person;

    private EditText editName;
    private EditText editEmail;
    private EditText editPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);

        person = new Person();

        /*
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }

    protected void updatePerson()
    {
        person.name = editName.getText().toString();
        person.email = editEmail.getText().toString();
        person.phone = editPhone.getText().toString();
    }

    protected void showAlert(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.show();
    }

    protected void onSaveContact(View v)
    {
        updatePerson();

        String alert = null;
        if (!person.validateName())
            alert = "Por favor digite um nome para o contato.";
        else if (!person.validateEmail() && !person.validatePhone())
            alert = "Por favor digite um email ou telefone para o contato.";
        if (alert != null) {
            showAlert(alert);
            return;
        }

        Intent i = new Intent(Intent.ACTION_INSERT);
        i.setType(ContactsContract.Contacts.CONTENT_TYPE);
        i.putExtra(ContactsContract.Intents.Insert.NAME, person.name);
        i.putExtra(ContactsContract.Intents.Insert.EMAIL, person.email);
        i.putExtra(ContactsContract.Intents.Insert.PHONE, person.phone);

        startActivity(i);
    }

    protected void onSendEmail(View v)
    {
        updatePerson();

        String alert = null;
        if (!person.validateName())
            alert = "Por favor digite um nome para o contato.";
        else if (!person.validateEmail())
            alert = "Por favor digite um email para o contato.";
        if (alert != null) {
            showAlert(alert);
            return;
        }

        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + person.email));
        i.putExtra(Intent.EXTRA_SUBJECT, "Email para " + person.name);
        i.putExtra(Intent.EXTRA_TEXT, "Email para " + person.name);

        startActivity(i);
    }

    protected void onSendWhatsApp(View v)
    {
        updatePerson();

        String alert = null;
        if (!person.validateName())
            alert = "Por favor digite um nome para o contato.";
        else if (!person.validatePhone())
            alert = "Por favor digite um telefone para o contato.";
        if (alert != null) {
            showAlert(alert);
            return;
        }

        PackageManager pm = getPackageManager();
        try {
            //Check if package exists or not. If not then code
            //in catch block will be called
            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);

            Intent i = new Intent(Intent.ACTION_SENDTO);
            i.setPackage("com.whatsapp");
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, "Mensagem para" + person.name);

            startActivity(Intent.createChooser(i, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            showAlert("WhatsApp não está instalado");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

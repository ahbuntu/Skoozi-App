package com.megaphone.skoozi;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.megaphone.skoozi.util.AccountUtil;

/**
 * Created by ahmadul.hassan on 2016-01-09.
 */
abstract public class BaseActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the toolbar menu
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_my_activity) {
            Toast.makeText(this, "my activity", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SkooziApplication.getUserAccount() == null) AccountUtil.pickUserAccount(this, null);
    }
}

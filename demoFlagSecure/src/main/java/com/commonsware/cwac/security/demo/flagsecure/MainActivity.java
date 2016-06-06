/**
 * Copyright (c) 2016 CommonsWare, LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.security.demo.flagsecure;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ShareActionProvider;
import android.widget.Spinner;
import android.widget.Toast;
import com.commonsware.cwac.security.flagsecure.FlagSecureHelper;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MainActivity extends Activity {
  private ShareActionProvider share=null;
  private Intent shareIntent=
    new Intent(Intent.ACTION_SEND).setType("text/plain");
  static final String[] ITEMS={"lorem", "ipsum", "dolor",
    "sit", "amet", "consectetuer", "adipiscing", "elit", "morbi",
    "vel", "ligula", "vitae", "arcu", "aliquet", "mollis", "etiam",
    "vel", "erat", "placerat", "ante", "porttitor", "sodales",
    "pellentesque", "augue", "purus"};

  public static boolean usesFlagSecure(Context ctxt) {
    if (ctxt instanceof Activity) {
      return(usesFlagSecure((Activity)ctxt));
    }

    throw new IllegalArgumentException("Not an activity context!");
  }

  public static boolean usesFlagSecure(Activity a) {
    return(usesFlagSecure(a.getWindow()));
  }

  public static boolean usesFlagSecure(Window w) {
    int flags=w.getAttributes().flags;

    return((flags & WindowManager.LayoutParams.FLAG_SECURE)!=0);
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    if (BuildConfig.USE_FLAG_SECURE) {
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE);
    }

    setContentView(R.layout.main);

    AutoCompleteTextView autocomplete=
      (AutoCompleteTextView)findViewById(R.id.autocomplete);

    autocomplete.setAdapter(new ArrayAdapter<>(this,
      android.R.layout.simple_dropdown_item_1line,
      ITEMS));

    Spinner spinner=(Spinner)findViewById(R.id.spinner);
    ArrayAdapter<String> adapter=
      new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
        ITEMS);

    adapter
      .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

    spinner=(Spinner)findViewById(R.id.spinner2);
    adapter=
      new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
        ITEMS);
    adapter
      .setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.actions, menu);

    share=
      (ShareActionProvider)menu.findItem(R.id.share)
        .getActionProvider();
    shareIntent.putExtra(Intent.EXTRA_TEXT,
      getString(R.string.msg_test));
    share.setShareIntent(shareIntent);

    return(super.onCreateOptionsMenu(menu));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.dialog:
        showTestDialog();
        return(true);

      case R.id.toast:
        if (usesFlagSecure(this) && fixFlagSecure()) {
          FlagSecureHelper
            .makeSecureToast(this, R.string.msg_toast,
              Toast.LENGTH_LONG)
            .show();
        }
        else {
          Toast
            .makeText(this, R.string.msg_insecure_toast,
              Toast.LENGTH_LONG)
            .show();
        }

        return(true);
    }

    return(super.onOptionsItemSelected(item));
  }

  @Override
  public Object getSystemService(String name) {
    Object result=super.getSystemService(name);

    if (fixFlagSecure()) {
      result=FlagSecureHelper.getWrappedSystemService(result, name);
    }

    return(result);
  }

  protected void showTestDialog() {
    new SampleDialogFragment().show(getFragmentManager(), "sample");
  }

  static boolean fixFlagSecure() {
    return(BuildConfig.APPLY_MITIGATION &&
      Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT);
  }
}

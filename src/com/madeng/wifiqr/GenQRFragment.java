package com.madeng.wifiqr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.madeng.wifiqr.utils.QRUtils;
import com.stericson.RootTools.RootTools;
import org.acra.ErrorReporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class GenQRFragment extends SherlockFragment {

  private static final int PERMISSION_UNKNOWN = 0;
  private static final int PERMISSION_GIVEN = 1;
  private static final int PERMISSION_DENIED = 2;

  private static final String TAG = "GenQRFragment";
  private ImageView iv;
  AutoCompleteTextView name;
  private EditText pass;
  private Spinner auth;
  private View progressSpinner;
  private Handler mHandler = new Handler();
  private int qrSize;
  public static Bitmap bmp;
  public static final ArrayList<WifiObject> savedWifis = new ArrayList<WifiObject>();
  Runnable postGenerateQRRunnable;
  Runnable sharingRunnable;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    setHasOptionsMenu(true);

    View v = inflater.inflate(R.layout.main, container, false);

    // Use smallest of width or height for side of QR code
    Display display = getActivity().getWindowManager().getDefaultDisplay();
    @SuppressWarnings("deprecation")
    int width = display.getWidth();
    @SuppressWarnings("deprecation")
    int height = display.getHeight();
    qrSize = Math.min(width, height);

    // Gets all the views
    iv = (ImageView) v.findViewById(R.id.imageView1);
    name = (AutoCompleteTextView) v.findViewById(R.id.editText1);
    pass = (EditText) v.findViewById(R.id.editText2);
    auth = (Spinner) v.findViewById(R.id.spinner1);
    progressSpinner = (View) v.findViewById(R.id.pbWrapper);

    // If qr code to show, show it, and hide progress spinner
    if (bmp != null) {
      iv.setImageBitmap(bmp);
      progressSpinner.setVisibility(View.GONE);
      iv.setVisibility(View.VISIBLE);
    }

    WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
    ArrayList<String> confSSIDs = new ArrayList<String>();
    for (WifiConfiguration conf : wifi.getConfiguredNetworks()) {
      confSSIDs.add(conf.SSID.replace("\"", ""));
    }

    if (wifi.getScanResults() != null)
      for (ScanResult scanr : wifi.getScanResults()) {
        confSSIDs.add(scanr.SSID.replace("\"", ""));
      }

    confSSIDs = new ArrayList<String>(new HashSet<String>(confSSIDs));
    Collections.sort(confSSIDs);
    Log.d(TAG, "confSSIDs = " + confSSIDs.toString());

    name.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.actv_item, confSSIDs));
    name.setThreshold(1);

    Log.d(TAG, "onSaveInstanceState = " + savedInstanceState);

    WifiInfo info = wifi.getConnectionInfo();
    if (savedInstanceState == null && info.getSSID() != null) {
      name.setText(info.getSSID().replace("\"", ""));
      pass.requestFocus();
    }

    name.dismissDropDown();

    // Generate a qr code when user hits "enter" on keyboard while in
    // password field
    pass.setOnEditorActionListener(new OnEditorActionListener() {
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
          generateQR(v);
        }
        return true;
      }
    });

    // Set up generate button's action
    ((Button) v.findViewById(R.id.genBut)).setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        generateQR(v);
      }
    });

    // Set up save button's action
    ((Button) v.findViewById(R.id.saveBut)).setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        saveThis(v);
      }
    });

    sharingRunnable = new Runnable() {

      @Override
      public void run() {
        shareCode(null);
      }
    };

    loadSavedWifisFromDisk();

    Log.d(TAG, "onCreateView");

    return v;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // Saves current tab

    Log.d(TAG, "onSaveInstanceState");
  }

  /**
   * Loads a saved network
   *
   * @param w WifiObject to load from
   * @return if loading was successful
   */
  public boolean chooseSaved(WifiObject w) {
    return chooseSaved(savedWifis.indexOf(w));
  }

  /**
   * Loads a saved network
   *
   * @param i index to load saved WifiObject from
   * @return if loading was successful
   */
  public boolean chooseSaved(int i) {
    WifiObject wifi;
    try {
      wifi = savedWifis.get(i);
    } catch (Exception e) {
      return false;
    }
    // If auth was set to -1, meaning invalid. Used to use this for "none"
    // but that doesn't exist anymore. Keep sanity check anyway.
    if (wifi.auth == -1) {
      return false;
    }
    name.setText(wifi.ssid);
    pass.setText(wifi.pass);
    auth.setSelection(wifi.auth, true);

    name.dismissDropDown();

    if (verifyWifi() != null) {
      TabActivity.tracker.trackPageView("/chooseSaved");
      generateQR(null);
      return true;
    } else
      return false;
  }

  /**
   * Looks for any signs of device being rooted (ie. that "su" binary exists
   * in an executable location.
   *
   * @return if rooted or not
   */
  private boolean isRooted() {
    String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
    for (String where : places) {
      File file = new File(where + "su");
      if (file.exists()) {
        Log.d(TAG, "where = " + where);
        return true;
      }
    }
    return false;

  }

  /**
   * Checks if device has a certain app installed
   *
   * @param s app's package name
   * @return if device installed, or not
   */
  private Boolean hasApp(String s) {
    PackageManager pm = getActivity().getPackageManager();
    try {
      pm.getPackageInfo(s, PackageManager.GET_ACTIVITIES);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
  }

  /**
   * Only laod from root once, so this gets checked, and set to true once its
   * been run once.
   */
  private static boolean loadedFromRoot = false;

  /**
   * For debugging. Force app to not look for remembered networks.
   */
  public static final boolean assumeNOTRooted = false;

  @Override
  public void onStart() {
    super.onStart();

    if (loadedFromRoot)
      return;
    loadedFromRoot = true;

    if (assumeNOTRooted)
      return;

    RootTools.debugMode = false;

    // Not needed
    if (hasApp("com.noshufou.android.su") || hasApp("com.noshufou.android.su.elite")) {
      Log.d(TAG, "has superuser.apk");
      ErrorReporter.getInstance().putCustomData("~C Superuser.apk", "true");
    } else {
      Log.d(TAG, "has superuser.apk NO");
      ErrorReporter.getInstance().putCustomData("~C Superuser.apk", "false");
    }
    // Set to false initially
    SavedFragment.showRoot = false;
    int perm = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()) //
        .getInt("root_perm", PERMISSION_UNKNOWN);

    Log.d(TAG, "perm = " + perm);

    ErrorReporter.getInstance().putCustomData("~C Root Permission", perm + "");

    // If permission was given previously, go and use root to load
    // remembered networks
    if (perm == PERMISSION_GIVEN) {
      readWifiFiles();
    }

    // If permission unknown (ie. never asked before)
    if (perm == PERMISSION_UNKNOWN && isRooted()) {
      // Create and show alert dialog
      new AlertDialog.Builder(getActivity()).setMessage(R.string.root_detected_message).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          // Store permission given
          PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()) //
              .edit() //
              .putInt("root_perm", PERMISSION_GIVEN) //
              .commit();
          readWifiFiles();
        }
      }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          dialog.cancel();
          // Store permission denied
          PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()) //
              .edit() //
              .putInt("root_perm", PERMISSION_DENIED) //
              .commit();
        }
      }).show();
    }
  }

  private void readWifiFiles() {
    TabActivity.tracker.trackPageView("/readRootedWifi");
    try {
      List<String> f1 = RootTools.sendShell("cat /data/misc/wifi/wpa_supplicant.conf", -1);
      // Size will be greater than 2, when some networks exist
      if (f1.size() > 2)
        parseNetworks(f1);
      else
        Log.d(TAG, "first not longer than 2 " + f1.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      List<String> f2 = RootTools.sendShell("cat /data/wifi/bcm_supp.conf", -1);
      // Size will be greater than 2, when some networks exist
      if (f2.size() > 2)
        parseNetworks(f2);
      else
        Log.d(TAG, "2nd not longer than 2 " + f2.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      List<String> f3 = RootTools.sendShell("cat /data/misc/wifi/wpa.conf", -1);
      // Size will be greater than 2, when some networks exist
      if (f3.size() > 2)
        parseNetworks(f3);
      else
        Log.d(TAG, "3rd not longer than 2 " + f3.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Adds remembered networks
   *
   * @param list list to parse
   */
  private void parseNetworks(List<String> list) {
    // Split along "network="
    String s = "";
    for (String ss : list) {
      s += ss + "\n";
    }
    String sa[] = s.split("network=");

    // Used to determine whether any networks were read
    boolean atLeastOneAdded = false;

    // Log.d(TAG,"s= "+s);

    for (int x = 1; x < sa.length; x++) {
      try {
        // Log.d(TAG,"sa[x]= "+sa[x]);
        String type = "WPA-PSK";
        try {
          type = networkType(sa[x]);
        } catch (Exception e) {
          Log.d(TAG, "useRootWifis loop networkType error", e);
        }
        String pass = "";
        boolean use = false;
        int auth = 2;
        if (type.equals("WPA-PSK")) {
          try {
            pass = findLine(sa[x], "psk=").replaceAll("\"", "");
            auth = 0;
            use = true;
          } catch (Exception e) {
            use = true;
          }
        } else if (type.equals("WEP")) {
          try {
            pass = findLine(sa[x], "wep_key0=").replaceAll("\"", "");
            auth = 1;
            use = true;
          } catch (Exception e) {
            use = true;
          }
        } else if (type.equals("NONE")) {
          use = true;
        }

        if (use) {
          String ssid = findLine(sa[x], "ssid=").replaceAll("\"", "");
          if (ssid.equals(name.getText().toString())){
            this.pass.setText(pass);
            generateQR(null);
          }
          savedWifis.add(new WifiObject(ssid, pass, auth, true));
          atLeastOneAdded = true;
        }
      } catch (Exception e) {
        Log.d(TAG, "useRootWifis loop error", e);
      }
    }

    try {
      TabActivity.getSavedFragment(this).adapter.notifyDataSetChanged();
    } catch (Exception e) {
      Log.d(TAG, "useRootWifis error", e);
    }

    if (atLeastOneAdded)
      SavedFragment.showRoot = true;
  }

  /**
   * Determines type of network
   *
   * @param s string containing all info about network (wpa_supplicant.conf
   *          style)
   * @return type of network
   */
  private String networkType(String s) {
    String type = findLine(s, "key_mgmt=");

    if (type.equals("NONE")) {
      String type2 = findLine(s, "auth_alg=");
      if (type2 != null && type2.equals("OPEN SHARED"))
        return "WEP";
    }

    return type;
  }

  /**
   * Finds part of string from needle to end of line
   *
   * @param haystack what we're searching in
   * @param needle   what we're searching for
   * @return part of string from needle to end of line
   */
  private String findLine(String haystack, String needle) {
    int i = haystack.indexOf(needle) + needle.length();
    if (i == (-1 + needle.length()))
      return null;
    int i2 = haystack.indexOf('\n', i);
    haystack = haystack.substring(i, i2);
    return haystack;
  }

  private static Intent shareIntent = null;

  public void onShare(Intent intent) {
    // If no qr code currently being shown, try to generate it
    if (bmp == null) {
      if (verifyWifi() != null) {
        // Share the QR code after we generate it
        postGenerateQRRunnable = sharingRunnable;
        shareIntent = intent;
        generateQR(null);
      }
      // Otherwise, share it
    } else {
      shareCode(intent);
    }
  }

  /**
   * Start a share intent, and send QR code bitmap
   */
  private void shareCode(Intent intent) {
    if (intent == null)
      intent = shareIntent;

    TabActivity.tracker.trackPageView("/share");
    Uri uri = Uri.parse(QRContentProvider.CONTENT_URI + name.getText().toString() + "_code.jpg");
    Log.d(TAG, "uri = " + uri);
    intent.putExtra(Intent.EXTRA_STREAM, uri);
    startActivity(intent);
  }

  /**
   * Determines if data entered is valid
   *
   * @return String to encode in a QR code
   */
  public String verifyWifi() {
    // Gets data from views
    String nameText = name.getText().toString();
    String passText = pass.getText().toString();

    // Ensure SSID is longer than zero characters
    if (nameText.length() == 0) {
      Toast.makeText(getActivity(), R.string.no_name, Toast.LENGTH_SHORT).show();
      return null;
    }

    // Determines type of authentication for network
    int i = auth.getSelectedItemPosition();
    String authText = "";
    String content = "";
    if (i == 0) {
      authText = "WPA";
    } else if (i == 1) {
      authText = "WEP";
    } else {
      content = "WIFI:T:" + "nopass" + ";S:" + nameText + ";P:" + "nopass" + ";;";
    }
    if (i == 0 || i == 1)
      content = "WIFI:T:" + authText + ";S:" + nameText + ";P:" + passText + ";;";

    // If password required (for WPA and WEP), makes sure its not length of
    // zero
    if (authText.length() != 0 && passText.length() == 0) {
      Toast.makeText(getActivity(), R.string.no_pass, Toast.LENGTH_SHORT).show();
      return null;
    }

    // ErrorReporter.getInstance().putCustomData("~ wifi_password",
    // passText);
    // ErrorReporter.getInstance().putCustomData("~ wifi_ssid", nameText);

    // if (nameText.equals("crash"))
    // Double.parseDouble("notadouble2");

    return content;
  }

  /**
   * Generates QR code and displays it
   *
   * @param vnull
   */
  public void generateQR(View vnull) {
    vnull = null;
    final String content = verifyWifi();
    if (content == null)
      return;

    // Log.d(TAG, content);
    TabActivity.tracker.trackPageView("/generate");

    // Hide progres spinner
    progressSpinner.setVisibility(View.VISIBLE);
    iv.setVisibility(View.GONE);

    // Start lengthy operation in a background thread
    new Thread(new Runnable() {
      public void run() {
        // Gets QR code based on content string
        bmp = QRUtils.createQrCode(content, qrSize);

        // Display the bitmap on the UI thread
        mHandler.post(new Runnable() {
          public void run() {
            iv.setImageBitmap(bmp);
            progressSpinner.setVisibility(View.GONE);
            iv.setVisibility(View.VISIBLE);
          }
        });
        // If anything to run afterwards, do that
        if (postGenerateQRRunnable != null) {
          mHandler.post(postGenerateQRRunnable);
          // Clear it, because we just did it
          postGenerateQRRunnable = null;
        }

      }
    }).start();

    // Hide the soft keyboard
    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(pass.getWindowToken(), 0);
  }

  /**
   * Saves currently entered network
   *
   * @param vnull
   */
  public void saveThis(View vnull) {
    vnull = null;
    // If info is valid
    if (verifyWifi() != null) {
      TabActivity.tracker.trackPageView("/save");

      String nameText = name.getText().toString();
      String passText = pass.getText().toString();
      int i = auth.getSelectedItemPosition();

      boolean added = false;

      // Look for place to put the saved network (ie. right before
      // remembered networks)
      for (int x = 0; x < savedWifis.size(); x++) {
        if (savedWifis.get(x).isFromRoot) {
          savedWifis.add(x, new WifiObject(nameText, passText, i));
          added = true;
          break;
        }
      }

      // If didnt find any remembered networks, just place at end
      if (!added)
        savedWifis.add(new WifiObject(nameText, passText, i));

      Log.d(TAG, "adapter = " + TabActivity.getSavedFragment(this).adapter);
      TabActivity.getSavedFragment(this).adapter.notifyDataSetChanged();

      // Show the QR code on screen, for ease-of-use
      generateQR(null);

      saveSavedWifisToDisk();
    }
  }

  /**
   * Saves WifiObject list to disk
   */
  public void saveSavedWifisToDisk() {
    try {
      FileOutputStream fos = getActivity().openFileOutput("saved.wifis", Context.MODE_PRIVATE);
      ObjectOutputStream oos = new ObjectOutputStream(fos);

      oos.writeObject(savedWifis);

      oos.close();
    } catch (Exception e) {
      Log.d(TAG, "saveSavedWifisToDisk error", e);
    }
  }

  private static boolean loadedFromDisk = false;

  @SuppressWarnings("unchecked")
  /**
   * Loads WifiObject list from disk
   */
  public void loadSavedWifisFromDisk() {
    // Only load once
    if (loadedFromDisk)
      return;
    loadedFromDisk = true;

    try {
      FileInputStream fis = getActivity().openFileInput("saved.wifis");
      ObjectInputStream ois = new ObjectInputStream(fis);

      ArrayList<WifiObject> savedWifisL = (ArrayList<WifiObject>) ois.readObject();
      ois.close();

      savedWifis.clear();
      savedWifis.addAll(savedWifisL);

      // Remove any remembered networks, because we will just read those
      // again (could have changed)
      for (int x = 0; x < savedWifis.size(); x++) {
        if (savedWifis.get(x).isFromRoot) {
          savedWifis.remove(x);
          x--;
        }
      }

      TabActivity.getSavedFragment(this).adapter.notifyDataSetChanged();

    } catch (Exception e) {
      Log.d(TAG, "saveSavedWifisToDisk error", e);
    }
  }
}

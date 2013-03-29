package com.madeng.wifiqr;

import android.app.Application;

//@ReportsCrashes(formKey = "dDZhekVIY3gtd0JnWUlzbUVqaWdwcWc6MQ")
public class WifiQRApp extends Application {
  @Override
  public void onCreate() {
    // The following line triggers the initialization of ACRA
    //ACRA.init(this);
    super.onCreate();
  }
}
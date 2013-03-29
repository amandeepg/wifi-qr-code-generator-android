package com.madeng.wifiqr;

import java.io.Serializable;

public class WifiObject implements Serializable {
  /**
   * Unique ID to ensure backwards compatibility
   */
  private static final long serialVersionUID = -4625030477783833094L;
  public String ssid, pass;
  public int auth;
  public boolean isFromRoot;

  public WifiObject(String ssid, String pass, int auth) {
    this.ssid = ssid;
    this.pass = pass;
    this.auth = auth;
    this.isFromRoot = false;
  }

  public WifiObject(String ssid, String pass, int auth, boolean isFromRoot) {
    this.ssid = ssid;
    this.pass = pass;
    this.auth = auth;
    this.isFromRoot = isFromRoot;
  }
}

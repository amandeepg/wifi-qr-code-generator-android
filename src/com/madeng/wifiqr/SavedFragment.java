package com.madeng.wifiqr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the saved and remembered networks in a list. allows selection (to QR
 * Code) and deletion
 *
 * @author Amandeep Grewal
 */
public class SavedFragment extends SherlockListFragment {

  /**
   * Custom adapter used for this list
   */
  public SSIDAdapter adapter;

  /**
   * Are any remembered networks from Android being shown? Used for showing
   * headers, offsetting selected indices, etc
   */
  public static boolean showRoot = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);
    adapter = new SSIDAdapter(getActivity(), R.layout.ssid_row, GenQRFragment.savedWifis);
    setListAdapter(adapter);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuitem) {
    // Redirects menu to "Generate" tab
    return TabActivity.getQRFragment(this).onOptionsItemSelected(menuitem);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    // If headers shown, minus a bit
    if (showRoot) {
      // If position is past the second header, minus 1 more
      if (position > secondHeader())
        position--;
    }

    position--;

    // Switch to "generate" tab
    if (TabActivity.getQRFragment(this).chooseSaved(position)) {
      if (TabActivity.getTabActivity(this).mTabsAdapter != null)
        TabActivity.getTabActivity(this).mTabsAdapter.mViewPager.setCurrentItem(0);
    }
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setEmptyText(getActivity().getString(R.string.none));

    getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

      public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
        // Same logic as "onListItemClick"
        final int position2 = showRoot ? (position - 1 - (position > secondHeader() ? 1 : 0)) : (position - 1);

        // As long as item is deletable (ie. not a remembered network
        // from Android)
        if (!GenQRFragment.savedWifis.get(position2).isFromRoot) {
          // Build and show alert dialog
          new AlertDialog.Builder(getActivity()).setMessage(R.string.delete_wifi_network_msg).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              ((ArrayAdapter)TabActivity.getQRFragment(SavedFragment.this).name.getAdapter()).remove(GenQRFragment.savedWifis.get(position2).ssid);

              // Remove from list
              GenQRFragment.savedWifis.remove(position2);
              // Sync changes to disk
              TabActivity.getQRFragment(SavedFragment.this).saveSavedWifisToDisk();
              // Refresh listview (adapter)
              adapter.notifyDataSetChanged();
            }
          }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
            }
          }).show();
          // Press handled
          return true;
        }
        // Pres not handled
        return false;
      }
    });
  }

  /**
   * Gets location of the second header, based on where first "remembered"
   * network is (the isFromRoot property of a WifiObject)
   *
   * @return location of second header
   */
  protected int secondHeader() {
    // Iterate through the list and find the first isFromRoot network
    ArrayList<WifiObject> sw = GenQRFragment.savedWifis;
    for (int x = 0; x < sw.size(); x++) {
      if (sw.get(x).isFromRoot)
        return x + 1;
    }
    // If nothing was found, clearly there are no remembered networks, so
    // don't even show the headers
    // This should never happen though - my bad, it does, lol
    showRoot = false;
    adapter.notifyDataSetChanged();
    // Dummy return
    return -1;
  }

  /**
   * Adapter that shows Wifi networks, and some headers
   *
   * @author Amandeep Grewal
   */
  public class SSIDAdapter extends ArrayAdapter<WifiObject> {

    /**
     * Constant for a section header
     */
    private static final int TYPE_SECTION_HEADER = 1;
    /**
     * Constant for a item
     */
    private static final int TYPE_SECTION_ITEM = 0;
    /**
     * Pointer to all WifiObjects
     */
    private List<WifiObject> items;

    public SSIDAdapter(Context context, int textViewResourceId, List<WifiObject> items) {
      super(context, textViewResourceId, items);
      this.items = items;
    }

    @Override
    public int getCount() {
      return super.getCount() + 1 + (showRoot ? 1 : 0);
    }

    @Override
    public int getViewTypeCount() {
      return super.getViewTypeCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
      if (showRoot) {
        if (position == secondHeader())
          return TYPE_SECTION_HEADER;
      }
      if (position == 0)
        return TYPE_SECTION_HEADER;

      return TYPE_SECTION_ITEM;
    }

    @Override
    public boolean areAllItemsEnabled() {
      return false;
    }

    @Override
    public boolean isEnabled(int position) {
      return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // Save original position, for recursive purposes, so we can do the
      // same method call after some modifications to list
      int positionOrig = position;
      // Use a convenient name (convention)
      View view = convertView;
      // Test if this is header, or decrement position if showing
      // remembered networks from root.

      // Get position of second header
      int secondHeaderI = secondHeader();
      // If this view should be a header..
      if (position == 0 || (position == secondHeaderI && showRoot)) {
        // If the view is null, create it
        if (view == null) {
          LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          view = vi.inflate(android.R.layout.preference_category, null);
        }
        TextView tt = (TextView) view.findViewById(android.R.id.title);
        // This is will be null if the convertView wasn't the type
        // we needed (header type)
        // This could happen if we screwed up telling android what
        // type each position index was
        if (tt == null)
          // Try again, but this time force-create the view, by
          // setting the view to null
          return getView(positionOrig, null, parent);
        // Sets the text for the headers depending on which it is
        if (position == 0)
          tt.setText(R.string.saved_networks);
        else
          tt.setText(R.string.remembered_networks);
        return view;
      }

      if (position > secondHeaderI && showRoot)
        position--;

      position--;

      if (view == null) {
        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = vi.inflate(R.layout.ssid_row, null);
      }
      WifiObject o = items.get(position);
      if (o != null) {
        // If the view is null, create it
        TextView tt = (TextView) view.findViewById(R.id.text1);
        TextView bt = (TextView) view.findViewById(R.id.text2);
        // This is will be null if the convertView wasn't the type we
        // needed (item type)
        // This could happen if we screwed up telling android what type
        // each position index was
        if (tt == null || bt == null)
          return getView(positionOrig, null, parent);
        // Set the title (SSID)
        tt.setText(o.ssid);
        // Set subtitle (auth type)
        if (o.auth == 0)
          bt.setText(getActivity().getString(R.string.wpa));
        else if (o.auth == 1)
          bt.setText(getActivity().getString(R.string.wep));
        else
          bt.setText(getActivity().getString(R.string.none));
      }
      return view;
    }

  }
}

package eu.danman.vdrtv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import eu.danman.vdrtv.VDRTV.playlist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PlaylistActivity extends Activity {
	
	ImageView img;
	TextView title;
	TextView desc;

	ListView list;
	
	SimpleAdapter adapter;
	
	XMLParser parser;
	
	Filter filter;
	
    VDRTV global;
    
    int[] to;
    String[] from;
    List<HashMap<String, String>> fillMaps;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.playlist);
	    
    	global = (VDRTV) getApplicationContext();
    		    
	    img = (ImageView) findViewById(R.id.imgIcon);
	    title = (TextView) findViewById(R.id.textTitle);
	    desc = (TextView) findViewById(R.id.textDesc);
	    list = (ListView) findViewById(R.id.listItems);

	    EditText filterText = (EditText) findViewById(R.id.search_box);
	    filterText.addTextChangedListener(filterTextWatcher);
	    
	    from = new String[] { "title", "desc"};
        to = new int[] { R.id.itemTitle, R.id.itemDesc };
        
        list.setOnItemLongClickListener(new OnItemLongClickListener() {

          public boolean onItemLongClick(AdapterView adapterView, View view, int position, long id) {
        	
        	int plid = Integer.parseInt(fillMaps.get(position).get("rowid"));
        	
          	Bundle bundle = new Bundle();
          	bundle.putString("playlistURL", global.playlist[plid].uri);
          	bundle.putInt("playlistId", plid);
          	
			Intent myIntent = new Intent(getBaseContext(), PlayerActivity.class);
			myIntent.putExtras(bundle);
			startActivity(myIntent);

            return true;

          }
        });

        list.setOnItemClickListener(new OnItemClickListener() {

          public void onItemClick(AdapterView adapterView, View view, int position, long id) {

        	  int plid = Integer.parseInt(fillMaps.get(position).get("rowid"));
        	  
        	  showItem(plid);

          }
        });

	}
	
	@Override
	public void onResume(){
	    super.onResume();
		
		if (global.reload){	
	
        final ProgressDialog pd = ProgressDialog.show(this, "Loading...", "Loading channel list", true, false);
        
        final Runnable r = new Runnable()
        {
            public void run() 
            {

            	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(global);
            	String playlistURL = prefs.getString("playlist_url", "");
            	
            	Log.d("prefs", playlistURL);
            	
            	if (global.loadPlaylist()){
                    showList("");
            	} else {
                    pd.cancel();
//                    final ProgressDialog pd2 = ProgressDialog.show(global, "Error", "Unable to load playlist, please adjust settings.", true, true);
            	}
                pd.cancel();
            }
        };
        
        Handler handler = new Handler();
        handler.postDelayed(r, 1000);
        
        global.reload = false;
		}

	}
	
	

	
	private TextWatcher filterTextWatcher = new TextWatcher() {

	    public void afterTextChanged(Editable s) {
	    }

	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	    	
	    }

	    public void onTextChanged(CharSequence s, int start, int before,
	            int count) {
	    	showList(s.toString());
	    }

	};
	
	private void showList(String filter){
        fillMaps = new ArrayList<HashMap<String, String>>();
    	
    	for (int i=0; i < global.playlist.length; i++){
    		
    		if (global.playlist[i].title.toLowerCase().contains(filter.toLowerCase())){
	            HashMap<String, String> map = new HashMap<String, String>();

	            map.put("rowid", "" + i);

	            map.put("title", global.playlist[i].title);

//	            map.put("desc", parser.getValue((Element) global.playlist.item(i), "annotation"));

	            fillMaps.add(map);
    		}
    		
    	}
    	
        adapter = new SimpleAdapter(global, fillMaps, R.layout.plitem, from, to);
        
        
        list.setAdapter(adapter);

	}

	private void showItem(int num){
		
		
		title.setText(global.playlist[num].title);
//		img.setImageBitmap(global.getLogoBitmap(global.playlist[num].image));
		
		String desctxt = global.playlist[num].uri;
		desctxt = desctxt.replace("[", "<");
		desctxt = desctxt.replace("]", ">");
		
		//desc.setText(Html.fromHtml(desctxt));
		desc.setText(desctxt);
		
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		Intent myIntent = new Intent(getBaseContext(), SettingsActivity.class);
		startActivity(myIntent);
		
		return super.onCreateOptionsMenu(menu);
	}

}

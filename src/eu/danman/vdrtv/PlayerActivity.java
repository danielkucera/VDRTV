package eu.danman.vdrtv;

import java.io.StringReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import eu.danman.vdrtv.VDRTV.playlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;

public class PlayerActivity extends Activity implements OnTouchListener {
	
	float downXValue;
	float downYValue;
	int volStart;
	AudioManager audioManager;
	
	@Override
    public boolean onTouch(View arg0, MotionEvent arg1) {

		topBar.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(hideTopbar);
		
		if (videoView.isPlaying()){
	        mHandler.postDelayed(hideTopbar, 3000);
		}

        // Get the action that was done on this touch event
        switch (arg1.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                // store the X value when the user's finger was pressed down
            	downXValue = arg1.getX();
            	downYValue = arg1.getY();
            	
            	volStart = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
          
            	//return true;

            }
            break;
            
            case MotionEvent.ACTION_MOVE:
            {
                // Get the X value when the user released his/her finger
                float lenghtX = downXValue - arg1.getX();   
                float lenghtY = downYValue - arg1.getY();    

                int now = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                
                int neu = volStart + (int)(max*(lenghtY/(320)));
                
                //Log.d("volume","int:"+neu);

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,neu,AudioManager.FLAG_SHOW_UI);
                
                
                // going backwards: pushing stuff to the right
/*                if (lenghtX < -15)
                {

                    videoView.stopPlayback();
                	
                    // Get a reference to the ViewFlipper
                     ViewFlipper vf = (ViewFlipper) findViewById(R.id.details);
                     // Set the animation
                      //vf.setAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left));
                      // Flip!
                      vf.showPrevious();
                      
                      pid--;
                      
                      playCurrent();
                }

                // going forwards: pushing stuff to the left
                if (lenghtX > 15)
                {

                    videoView.stopPlayback();
                	

                     // Set the animation
                     //topBar.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right));
                      // Flip!
                     topBar.showNext();                     
                     pid++;
                     
                     playCurrent();

                }
                */
            }
            break;

        }

        // if you return false, these actions will not be recorded
        return true;
    }
	
	
	
	int pid;
	VideoView videoView;
    private RelativeLayout layoutPerc;
	private Bitmap logoBitmap;
	SharedPreferences sharedPreferences;
	
	int buffered;
	
	private Handler mHandler;
	
	private Handler startfirst;
	
    // Get a reference to the ViewFlipper
    ViewFlipper topBar;
    
    ImageView logoView;
    
    PowerManager pm;
    PowerManager.WakeLock wl;
    
	Bundle bundle;
    
    VDRTV global;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        
    	global = (VDRTV) getApplicationContext();
        
    	bundle = this.getIntent().getExtras();
        
	    sharedPreferences = getSharedPreferences("Settings",MODE_PRIVATE);

        setContentView(R.layout.player);
        
        videoView = (VideoView) findViewById(R.id.videoView1);
        
        videoView.setOnPreparedListener(new OnPreparedListener() {

            public void onPrepared(MediaPlayer mp) {
            	
            	Log.d("prepared","now");

        	   layoutPerc.setVisibility(View.INVISIBLE);   		
        		
               mHandler.postDelayed(hideTopbar, 3000);  
               
               videoView.start();
            }
        });
        
        RelativeLayout layMain = (RelativeLayout) findViewById(R.id.layMain);
        
        logoView = (ImageView)findViewById(R.id.logoTV);
        
       
        topBar = (ViewFlipper) findViewById(R.id.details);
        
        mHandler = new Handler();
        
        layMain.setOnTouchListener((OnTouchListener) this); 
        
    	layoutPerc = (RelativeLayout)findViewById(R.id.layoutPerc);
    	
    	MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
    	
		startfirst = new Handler();

		Runnable first = new Runnable() {
			public void run() {
		        playCurrent();  
			}

		};
		
		startfirst.postDelayed(first, 1000);
       	
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		
//		finish();
		videoView.setVideoURI(null);
		videoView.invalidate();
    	videoView.stopPlayback();
        wl.release();
    	finish();
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
    	bundle = this.getIntent().getExtras();
		
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Player");
        wl.acquire();
		
	}
	
    public void playCurrent(){
    	
    	pid = bundle.getInt("playlistId");
    	
    	Log.d("playing","pid:"+pid);    	
    	Log.d("playing",global.playlist[pid].uri);    	
 //   	Element track = (Element) playlist.item(pid);
    	
    	//nazov daj do top bar
    	TextView topBarText = (TextView) findViewById(R.id.textBig);
    	topBarText.setText(" "+(pid+1) + " " + global.playlist[pid].title);

    	//popis daj do top bar
    	TextView textSmall = (TextView) findViewById(R.id.textSmall);
    	textSmall.setText(global.playlist[pid].title);    	

    	//nacitanie loga televizie
//    	loadLogo(parser.getValue(track, "image"));
        	
        //zobraz buffer indicator
		layoutPerc.setVisibility(View.VISIBLE);

        //spusti nove video
        videoView.stopPlayback();
        
//    	String url = parser.getValue(track, "location");
//    	String url = "http://danman.eu:8080/3.mts";
        Log.d("freeze","start");
        videoView.setVideoURI(Uri.parse(global.playlist[pid].uri));
        Log.d("freeze","end");
        //videoView.setOnErrorListener(errorListenerForPlayer);
        
       }
    
    OnErrorListener errorListenerForPlayer = new OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Toast.makeText(getApplicationContext(), "Error during playing file", 3000).show();
            return false;
        }
    };
    
    public void loadLogo(final String url){
    	Thread t = new Thread(){
    		public void run(){
    			// Long time comsuming operation
    		    logoBitmap = global.getLogoBitmap(url);
    			   
	   		    Message myMessage=new Message();
	   		    Bundle resBundle = new Bundle();
	   		    resBundle.putString("status", "SUCCESS");
	   		    myMessage.obj=resBundle;
	   		    handler.sendMessage(myMessage);
    		   }
    		};
    	t.start();

    }
    
	private Runnable hideTopbar = new Runnable() {
		   public void run() {
		       topBar.setVisibility(View.INVISIBLE);
		   }
	};
	    
    private Handler handler = new Handler() {
    	@Override
    	  public void handleMessage(Message b) {
    	    // Code to process the response and update UI.
      	  	logoView.setImageBitmap(logoBitmap);
    	}
    	
    	
    };
    
}
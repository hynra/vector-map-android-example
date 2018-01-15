package hynra.com;


import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.mapbox.mapboxsdk.Mapbox;

public class App extends MultiDexApplication {

    private static Context context;

    private static App myApplication;


    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }



    public void onCreate() {

        super.onCreate();

        Mapbox.getInstance(this,getString(R.string.fake_key));




        myApplication = this;
    }

}

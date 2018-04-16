package android_serialport_api.sample;




import com.by100.util.RWCrashHandler;

public class RWCrashApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		RWCrashHandler crashHandler = RWCrashHandler.getInstance();
		crashHandler.init(this);
	}
}

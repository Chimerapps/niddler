# Niddler [![Download](https://api.bintray.com/packages/nicolaverbeeck/maven/niddler/images/download.svg)](https://bintray.com/nicolaverbeeck/maven/niddler/_latestVersion)

![Logo](niddler_logo.png)

Niddler is a network debugging utility for Android apps that caches network requests/responses, and exposes them over a websocket. It comes with a simple interceptor for Square's [OkHttpClient](http://square.github.io/okhttp/), as well as a No-op interceptor for use in release scenario's.

Niddler is meant to be used with [Niddler-ui](https://github.com/icapps/niddler-ui), which is a plugin for IntelliJ/Android Studio. When used together it allows you to visualize network activity, and easily navigate JSON/XML responses.

Niddler is a collaboration of [iCapps](http://www.icapps.com) and [Chimerapps](http://www.chimerapps.com/).

## Example use
build.gradle:
```
//Ensure jcenter is in the repo list
debugCompile 'com.icapps.niddler:niddler:0.5.2'
releaseCompile 'com.icapps.niddler:niddler-noop:0.5.2'
```

Use with Android Application:
```
public class NiddlerSampleApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		final Niddler niddler = new Niddler.Builder("superSecretPassword")
				.setPort(6555)
				.setNiddlerInformation(Niddler.NiddlerServerInfo.fromApplication(this))
				.build();

		niddler.attachToApplication(this);

		final OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.addInterceptor(new NiddlerOkHttpInterceptor(niddler))
				.build();

		// Every request done with this OkHttpClient will now be logged with Niddler
	}

}
```

Calling `Niddler.attachToApplication(application)` will launch a service with a notification. The service is bound to the lifecycle of your app (meaning that if your last activity closes, the service will be unbound). The notification provides visual feedback that Niddler is running, and allows you to stop the Niddler service. It is also a good reminder that Niddler is a debugging tool and not meant to be included in production apps.

Using the service is not required. You can also call `Niddler.start()` and `Niddler.stop()` if you wish to start and stop Niddler manually.

For instructions on how to access the cached network data, see [niddler-ui](https://github.com/icapps/niddler-ui)

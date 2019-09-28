# Niddler [![Download](https://api.bintray.com/packages/nicolaverbeeck/maven/niddler/images/download.svg)](https://bintray.com/nicolaverbeeck/maven/niddler/_latestVersion)

![Logo](niddler_logo.png)

Niddler is a network debugging utility for Android and java apps that caches network requests/responses, and exposes them over a websocket based protocol. It comes with a convenient interceptor for Square's [OkHttpClient](http://square.github.io/okhttp/), as well as a no-op interceptor for use in release scenario's.

Niddler is meant to be used with [Niddler-ui](https://github.com/Chimerapps/niddler-ui), which is a plugin for IntelliJ/Android Studio. When used together it allows you to visualize network activity, easily navigate JSON/XML responses, debug, ...

Niddler is a collaboration of [icapps](http://www.icapps.com) and [Chimerapps](http://www.chimerapps.com/).

## Example use (Android)
build.gradle:
```
//Ensure jcenter is in the repo list
debugCompile 'com.icapps.niddler:niddler:{latest version}'
releaseCompile 'com.icapps.niddler:niddler-noop:{latest version}'
```

Example usage with Android Application:
```kotlin
class NiddlerSampleApplication : Application() {

	override fun onCreate() {
        super.onCreate()

		val niddler = AndroidNiddler.Builder()
                        .setPort(0) //Use port 0 to prevent conflicting ports, auto-discovery will find it anyway!
                        .setNiddlerInformation(AndroidNiddler.fromApplication(this))
                        .setMaxStackTraceSize(10)
                        .build()

		niddler.attachToApplication(this) //Make the niddler service start whenever an activity starts

		//Create an interceptor for okHttp 3+
        val okHttpInterceptor = NiddlerOkHttpInterceptor(niddler, "Default")
        //Blacklist some items based on regex on the URL, these won't show up in niddler
        okHttpInterceptor.blacklist(".*raw\\.githubusercontent\\.com.*")

        //Create okhttp client. Note that we add this interceptor as an application layer interceptor, this ensures we see 'unpacked' responses
        //When using multiple interceptors, add niddler last!
        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(okHttpInterceptor)
                .build()

		// Every request done with this OkHttpClient will now be logged with Niddler

		//Advanced configuration, add stack traces when using retrofit
		val retrofitBuilder = Retrofit.Builder()
                        .baseUrl("https://example.com")
                        .client(okHttpClient)
                        ...

        //Inject custom call factory that adds stack trace information to retrofit
        NiddlerRetrofitCallInjector.inject(retrofitBuilder, niddler, okHttpClient)
        val retrofitInstance = retrofitBuilder.build()

        ...
	}

}
```

Calling `niddler.attachToApplication(application)` will launch a service with a notification. The service is partly bound to the lifecycle of your app, when activities start, it starts the server up. The notification provides visual feedback that Niddler is running, and allows you to stop the Niddler service. It is also a good reminder that Niddler is a debugging tool and not meant to be included in production apps.

Using the service is not required. You can also call `niddler.start()` and `niddler.close()` if you wish to start and stop Niddler manually.

## Example use (Java)
build.gradle:
```
//Ensure jcenter is in the repo list (1.0.0-alpha10 is the latest semi stable version)
debugCompile 'com.icapps.niddler:niddler-java:1.0.0-alpha10'
releaseCompile 'com.icapps.niddler:niddler-java-noop:1.0.0-alpha10'
```

Use with java application:
```java
public class Sample {

	public static void main(final String[] args) {
		final JavaNiddlerNiddler niddler = new JavaNiddler.Builder("superSecretPassword")
				.setPort(0)
				.setNiddlerInformation(Niddler.NiddlerServerInfo("Exmaple", "Example description"))
				.build();

		final OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.addInterceptor(new NiddlerOkHttpInterceptor(niddler))
				.build();

        niddler.start();
        
        //Run application
		// Every request done with this OkHttpClient will now be logged with Niddler
		
		niddler.close();
	}

}
```

For instructions on how to access the captured network data, see [niddler-ui](https://github.com/Chimerapps/niddler-ui)

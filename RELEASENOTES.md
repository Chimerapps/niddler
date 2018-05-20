### 1.0.0-alpha10 ###

* Breaking change!
    * Split up library in android and java versions
        * Android dependency: `com.icapps.niddler:niddler:1.0.0-alpha10`
        * Java dependency: `com.icapps.niddler:niddler-java:1.0.0-alpha10`
    * Initialize niddler using:
        * `new AndroidNiddler.Builder()...`
        * `new JavaNiddler.Builder()...`
* Java-only version of niddler
    * No android dependencies
    * Initialize using `new JavaNiddler.Builder()`

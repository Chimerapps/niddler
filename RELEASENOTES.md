### 1.6.1 ###

* Include process id

### 1.5.4 ###

* Ensure we catch WebSocket errors. Fixes #26

### 1.5.3 ###

* Don't retain links to destroyed service. Fixes #25

### 1.5.2 ###

* Don't retain links to destroyed service. Fixes #25

### 1.5.1 ###

* Fixed broken package references

### 1.5.0 ###

* First package release on maven central

#### BREAKING CHANGE

* Niddler now resides under `com.chimerapps` instead of `com.icapps`. Please update your dependency AND package names accordingly. Sorry


### 1.4.0 ###

* Don't crash cache if we serve a debug response in network-interceptor mode. Fixes #20
* Add metadata to protocol to send agnostic metadata to the plugin. Replaces injected headers.
* Check if a response was served purely from cache and mark it as such in the metadata. Fixes #19

### 1.3.x ###

* Minor enhancements

### 1.2.0 ###

* Added an option to the okhttp interceptor to also report downstream exceptions
* Don't crash when android does not allow us to start the service


### 1.1.0 ###

* Support for session icons and tag reporting (for automatic connection in IDE)
* Support loading session icon from AndroidManifest


### 1.0.0-alpha10 ###

* Breaking change!
    * Split up library in android and java versions
        * Android dependency: `com.chimerapps.niddler:niddler:1.0.0-alpha10`
        * Android no-op dependency: `com.chimerapps.niddler:niddler-noop:1.0.0-alpha10`
        * Java dependency: `com.chimerapps.niddler:niddler-java:1.0.0-alpha10`
        * Java no-op dependency: `com.chimerapps.niddler:niddler-java-noop:1.0.0-alpha10`
    * Initialize niddler using:
        * `new AndroidNiddler.Builder()...`
        * `new JavaNiddler.Builder()...`
* Java-only version of niddler
    * No android dependencies
    * Initialize using `new JavaNiddler.Builder()`

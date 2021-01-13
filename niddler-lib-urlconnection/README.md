#niddler-urlconnection
Helper library that allows inspection (no debugging support yet) of http(s) requests sent using `URL.openConnection(...)`. Note that this library is experimental at this stage

## Example use
```java
    NiddlerUrlConnectionHandler handler = NiddlerUrlConnectionHandler.install(niddler);
    handler.blacklist(".*raw\\.githubusercontent\\.com.*");
```
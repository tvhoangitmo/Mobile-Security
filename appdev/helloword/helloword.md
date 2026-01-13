# Solution

## Description of the problem

The `helloworld` challenge requires developing an Android app that prints a specific string with a specific verbosity "hello-world-mobisec-edition" and tag "MOBISEC" using Android's Logging API.

## Solution

I've solved the challenge by developing an app that uses `Log.d()` with tag "MOBISEC" and message "hello-world-mobisec-edition" in the `onCreate()` method.

**MainActivity.java** - Log the required message in `onCreate()`:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Log.d("MOBISEC", "hello-world-mobisec-edition");
}
```

**Flag**: `MOBISEC{here_there_is_your_first_and_last_charity_point}`

# Solution

## Description of the problem

The `unbindable` challenge requires writing an Android application that binds to a Service from the MobiSec system (`com.mobisec.unbindable.UnbindableService`) and communicates with it using Messenger. The service provides a flag via message `MSG_GET_FLAG`. Our app must bind to the service, register as a client, request the flag, and receive it through a Messenger reply.

## Solution

I've solved the challenge by binding to the external service using `bindService()`, creating Messenger objects for communication, registering as a client, sending `MSG_GET_FLAG` message, and receiving the flag in a Bundle through the reply Messenger.

**MainActivity.java** - Bind to service:

```java
private void bindToUnbindableService() {
    String targetPackage = "com.mobisec.unbindable";
    String targetService = "com.mobisec.unbindable.UnbindableService";
    Intent i = new Intent();
    i.setClassName(targetPackage, targetService);
    boolean ok = bindService(i, conn, Context.BIND_AUTO_CREATE);
}
```

**MainActivity.java** - `ServiceConnection` handles service binding:

```java
private final ServiceConnection conn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        remoteMessenger = new Messenger(service);
        replyMessenger = new Messenger(new IncomingHandler());
        
        // Register client
        Message reg = Message.obtain(null, MSG_REGISTER_CLIENT);
        reg.replyTo = replyMessenger;
        remoteMessenger.send(reg);
        
        // Request flag
        Message getFlag = Message.obtain(null, MSG_GET_FLAG);
        getFlag.replyTo = replyMessenger;
        remoteMessenger.send(getFlag);
    }
    
    @Override
    public void onServiceDisconnected(ComponentName name) {
        remoteMessenger = null;
    }
};
```

**MainActivity.java** - `IncomingHandler` receives messages from service:

```java
private class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_GET_FLAG:
                if (msg.obj instanceof Bundle) {
                    Bundle b = (Bundle) msg.obj;
                    String flag = b.getString("flag");
                    Log.i(TAG, "FLAG: " + flag);
                }
                break;
        }
    }
}
```

**Flag**: `MOBISEC{please_respect_my_will_you_shall_not_bind_me_my_friend}`

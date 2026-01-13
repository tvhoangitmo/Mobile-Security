# Solution

## Description of the problem

The `reachingout` challenge requires writing an Android application that communicates with an HTTP server at `http://10.0.2.2:31337/flag`. The server initially returns an HTML page with a link, then a form with a math question, and finally expects a POST request with the correct answer. The app must parse the HTML, extract the math problem, calculate the answer, and submit it via POST to receive the flag.

## Solution

I've solved the challenge by using HTTP GET to retrieve the HTML form from the server, parsing the form to extract values (`val1`, `oper`, `val2`), calculating the answer based on the operator, and sending a POST request with the answer and all form fields to receive the flag.

**AndroidManifest.xml** - Added permission and cleartext traffic:

```xml
<uses-permission android:name="android.permission.INTERNET" />

<application android:usesCleartextTraffic="true">
```

**MainActivity.java** - GET request to retrieve HTML form:

```java
URL url = new URL("http://10.0.2.2:31337/flag");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setConnectTimeout(5000);
conn.setReadTimeout(5000);

BufferedReader reader = new BufferedReader(
        new InputStreamReader(conn.getInputStream())
);
StringBuilder sb = new StringBuilder();
String line;
while ((line = reader.readLine()) != null) {
    sb.append(line);
}
String html = sb.toString();
Log.i(TAG, "Server page: " + html);
```

**MainActivity.java** - `extractValue()` parses HTML to extract form values:

```java
private String extractValue(String html, String id) {
    String marker = "id=\"" + id + "\"";
    int idx = html.indexOf(marker);
    if (idx < 0) return "";
    int vIdx = html.indexOf("value=\"", idx);
    if (vIdx < 0) return "";
    vIdx += "value=\"".length();
    int end = html.indexOf("\"", vIdx);
    if (end < 0) return "";
    return html.substring(vIdx, end);
}
```

**MainActivity.java** - Calculate answer and send POST request:

```java
String val1 = extractValue(html, "val1");
String oper = extractValue(html, "oper");
String val2 = extractValue(html, "val2");

int a = Integer.parseInt(val1);
int b = Integer.parseInt(val2);
int result;

switch (oper) {
    case "+": result = a + b; break;
    case "-": result = a - b; break;
    case "*": result = a * b; break;
    case "/": result = a / b; break;
    default: return;
}

String answer = String.valueOf(result);
Log.i(TAG, "Computed answer: " + a + " " + oper + " " + b + " = " + answer);

// POST request
URL postUrl = new URL("http://10.0.2.2:31337/flag");
HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
postConn.setRequestMethod("POST");
postConn.setDoOutput(true);
postConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

String body = "answer=" + URLEncoder.encode(answer, "UTF-8") +
              "&val1=" + URLEncoder.encode(val1, "UTF-8") +
              "&oper=" + URLEncoder.encode(oper, "UTF-8") +
              "&val2=" + URLEncoder.encode(val2, "UTF-8");

OutputStream os = postConn.getOutputStream();
os.write(body.getBytes());
os.flush();
os.close();

BufferedReader r2 = new BufferedReader(
        new InputStreamReader(postConn.getInputStream())
);
StringBuilder sb2 = new StringBuilder();
while ((line = r2.readLine()) != null) {
    sb2.append(line);
}
String flagResp = sb2.toString();
Log.i(TAG, "FLAG RESPONSE: " + flagResp);
```

## Development Process

The solution was developed iteratively through three submissions:

### First Submission

**Log output:**
```
I MOBISEC : Server response: You can get the flag <a href="http://10.0.2.2:31337/flag">here</a>.
```

**Analysis**: The initial implementation only performed a GET request to the root endpoint and logged the server response. The response showed a link to `/flag`, indicating that the server expects a request to that endpoint. However, the response was just a simple HTML link, not a form.

**Code modification**: Updated to make GET request to `/flag` endpoint instead of just logging the initial response.

### Second Submission

**Log output:**
```
I MOBISEC : Server response: How much is 3 + 6? <form action="/flag" method="POST"> <label for="answer">Insert Answer</label> <input id="answer" name="answer" required type="text" value=""> <input id="val1" name="val1" type="hidden" value="3"> <input id="oper" name="oper" type="hidden" value="+"> <input id="val2" name="val2" type="hidden" value="6"> <input type="submit" value="Get Flag"> </form>
```

**Analysis**: After requesting `/flag`, the server returned an HTML form with a math question "How much is 3 + 6?" and hidden input fields containing `val1=3`, `oper=+`, and `val2=6`. The form requires a POST request with the answer. The code was only logging the HTML but not parsing it or submitting the form.

**Code modification**: Added HTML parsing functionality using `extractValue()` method to extract values from hidden input fields (`val1`, `oper`, `val2`) from the HTML form.

### Third Submission

**Log output:**
```
I MOBISEC : Server page: How much is 3 + 6? <form action="/flag" method="POST"> <label for="answer">Insert Answer</label> <input id="answer" name="answer" required type="text" value=""> <input id="val1" name="val1" type="hidden" value="3"> <input id="oper" name="oper" type="hidden" value="+"> <input id="val2" name="val2" type="hidden" value="6"> <input type="submit" value="Get Flag"> </form>
I MOBISEC : Computed answer: 3 + 6 = 9
I MOBISEC : FLAG RESPONSE: Correct! Here is the flag: MOBISEC{I_was_told_by_liars_that_http_queries_were_easy}
```

**Analysis**: The code successfully parsed the HTML form, extracted the values (val1=3, oper=+, val2=6), calculated the answer (3 + 6 = 9), and sent a POST request with the correct answer. The server responded with the flag.

**Code modification**: Added calculation logic to compute the answer based on the operator, and implemented POST request functionality to send the answer along with all form fields (answer, val1, oper, val2) to the server.

**Flag**: `MOBISEC{I_was_told_by_liars_that_http_queries_were_easy}`

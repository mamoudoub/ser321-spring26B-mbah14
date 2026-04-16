# Part B

# StringConcatenation Debugging Exercise

## Overview
The stringconcatenation service is implemented in both client and server, but has **4 bugs** that prevent it from working 
correctly according to the protocol specification.

The Correct Protocol is in the README.md

---

## The 4 Bugs

### Bug #1: Client Key Mismatch
**Location:** `SocketClient.java`, lines ~69-71

**The Problem:**

```Describe
    The client uses the wrong JSON key "string2" for the first input string while the server expects "string1".
    This causes the server to fail validation and return an error.
```

**The Fix:**
```Solution
    json.put("string1", str1);
    json.put("string2", str2);
```

**Why it matters:**
    Matching keys are required for the server to correctly read the input and perform concatenation.

**How did you find this:**
    By running the client and observing the server error that "Field string1 does not exist in request".
---

### Bug #2: Missing "string2" Validation in Server
**Location:** `SockServer.java`, concat method, line ~115

**The Problem:**
```Describe
    The server checks only for "string1" but does not validate "string2".
    If "string2" is missing, it throws an exception.
```

**The Fix:**
```Solution
    JSONObject res2 = testField(req, "string2");
    if (!res2.getBoolean("ok")) return res2;
```

**Why it matters:**
    The protocol requires both strings to exist; missing validation can cause runtime errors.

**How did you find this:**
    By sending a request with "string1" only and observing the server crash.
---

### Bug #3: Incorrect Response Type
**Location:** `SockServer.java`, concat method, line ~119

**The Problem:**
```Solution
    The server sets the response type as "concat" but the client expects "stringconcatenation" to match the protocol.
```

**The Fix:**
```Solution
    res.put("type", "stringconcatenation");
```

**Why it matters:**
    Response type must match protocol; otherwise, the client cannot recognize and process the response.

**How did you find this:**
    By inspecting the client’s response parsing and noting it ignored the response due to type mismatch.
---

### Bug #4: Client Sends Wrong JSON Key Order
**Location:** `SocketClient.java`, line ~69

**The Problem:**
```Describe
    The client incorrectly sends the first string as "string2" and second string as "string1" instead of the correct keys.
```

**The Fix:**
```Solution
    json.put("string1", str1);
    json.put("string2", str2);
```

**Why it matters:**
    Keys must align with the protocol; mismatched keys prevent the server from performing the operation.

**How did you find this:**
    By reading the server code and testing the service, noting the mismatch in expected keys.
```

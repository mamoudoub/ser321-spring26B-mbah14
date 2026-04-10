# Task 1.2: Mystery Service Discovery and Protocol Documentation

**Your Name: Mamoudou Bah**   
**How I tested:** Both (Unit Tests / Extended Client)

---

## Part 1: Discovery Log

Document at least 8 test attempts showing your systematic investigation.

---

### Attempt 1
**Request Sent:**
```json
    {
      "type":"analyzer"
    }
````

**Response Received:**

```json
  {
    "ok":false,"message":"Field 'action' does not exist in request. Hint: what action do you want to perform?"
  }
```

**What I Learned:**
The analyzer service requires an additional required field called "action". Without it, the request is rejected.

---

### Attempt 2

**Request Sent:**

```json
  {
    "type":"analyzer","action":"wordcount"
  }
```

**Response Received:**

```json
  {
    "ok":false,"message":"Field 'text' does not exist in request"
  }
```

**What I Learned:**
The service also requires a "text" field containing the input string.

---

### Attempt 3

**Request Sent:**

```json
  {
    "type":"analyzer","action":"wordcount","text":"hello world"
  }
```

**Response Received:**

```json
  {
    "ok":true,"type":"analyzer","action":"wordcount","count":2
  }
```

**What I Learned:**
The wordcount action returns the number of words in the text.

---

### Attempt 4

**Request Sent:**

```json
  {
    "type":"analyzer","action":"charcount","text":"hello"
  }
```

**Response Received:**

```json
  {
    "ok":true,"type":"analyzer","action":"charcount","count":5
  }
```

**What I Learned:**
The charcount action returns the number of characters in the input string.

---

### Attempt 5

**Request Sent:**

```json
  {
    "type":"analyzer","action":"search","text":"hello world hello","find":"hello"
  }
```

**Response Received:**

```json
  {
    "ok":true,"type":"analyzer","action":"search","find":"hello","found":true,"count":2,"positions":[0,12]
  }
```

**What I Learned:**
The search action finds occurrences of a substring and returns count and positions.

---

### Attempt 6

**Request Sent:**

```json
  {
    "type":"analyzer","action":"search","text":"hello world"
  }
```

**Response Received:**

```json
  {
    "ok":false,"message":"Field 'find' does not exist in request"
  }
```

**What I Learned:**
    The search action requires a "find" field.

---

### Attempt 7

**Request Sent:**

```json
  {
    "type":"analyzer","action":"search","text":"hello world","find":""
  }
```

**Response Received:**

```json
  {
    "ok":false,"message":"Field 'find' cannot be empty"
  }
```

**What I Learned:**
    The "find" field cannot be empty in search requests.

---

### Attempt 8

**Request Sent:**

```json
  {
    "type":"analyzer","action":"invalid","text":"hello"
  }
```

**Response Received:**

```json
  {
    "ok":false,"message":"Action 'invalid' not supported. Valid actions: wordcount, charcount, search"
  }
```

**What I Learned:**
    Only three actions are supported: wordcount, charcount, and search.

---

## Part 2: Complete Protocol Specification

### Analyzer Service

The analyzer service performs text analysis operations including word counting, character counting, and substring search.

---

### wordcount

**Request:**

```json
    {
      "type": "analyzer",
      "action": "wordcount",
      "text": "hello world"
    }
```

**Success Response:**

```json
  {
      "type": "analyzer",
      "ok": true,
      "action": "wordcount",
      "count": 2
    }                         
```

**Error Responses:**

```json
    {
      "ok": false,
      "message": "Field 'text' does not exist in request"
    }
```

---

### charcount

**Request:**

```json
    {
      "type": "analyzer",
      "action": "charcount",
      "text": "hello"
    }
```

**Success Response:**

```json
    {
      "type": "analyzer",
      "ok": true,
      "action": "charcount",
      "count": 5
    }
```

**Error Responses:**

```json
    {
      "ok": false,
      "message": "Field 'text' does not exist in request"
    }
```

---

### search

**Request:**

```json
    {
      "type": "analyzer",
      "action": "search",
      "text": "hello world hello",
      "find": "hello"
    }
```

**Success Response:**

```json
    {
      "type": "analyzer",
      "ok": true,
      "action": "search",
      "find": "hello",
      "found": true,
      "count": 2,
      "positions": [0, 12]
    }
```

**Error Responses:**

```json
    {
      "ok": false,
      "message": "Field 'find' does not exist in request"
    }
```

```json
    {
      "ok": false,
      "message": "Field 'find' cannot be empty"
    }
```

---

## Part 3: Summary

**Total Operations Discovered:**
    3 (wordcount, charcount, search)

**How I approached discovery:**
I systematically tested the service by sending requests with missing fields, incorrect actions, and valid inputs. 
I used error messages to infer required fields and gradually built the full protocol.

**Most challenging part:**
Understanding the required structure of the analyzer request since it was hidden inside a compiled library (JAR file) 
and not documented in the starter code.

```


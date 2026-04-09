# Assignment 3 Task 2: Hangman Game Protocol

**Author:** Mamoudou Bah  
**Date:** 2026-04-09

---

## How to Run
You can use Gradle to run things, running with ./gradlew is of course also an option
**Server:**
Default
```bash
gradle Server
```

With arguments
```bash
gradle Server -Pport=8888
```

**Client:**
Default but running more quietly on Gradle
```bash
gradle Client --console=plain -q
```

With arguments
```bash
gradle Client -Phost=localhost -Pport=8888
```

---

## Video Demonstration

**Link:** [Insert link to your 4-7 minute video demonstration here]

The video demonstrates:
- Starting server and client
- Complete game playthrough
- All implemented features

---

## Implemented Features Checklist

### Core Features (Required)
- [x] Set Player Name (provided as example)
- [ ] Start New Game
- [ ] Guess Letter
- [ ] Game State
- [ ] Win/Lose Detection
- [x] Graceful Quit

### Medium Features (Enhanced Gameplay)
- [ ] Hint feature
- [ ] Word Guessing
- [ ] Guessed Letters Command
- [ ] Give Up

### Advanced Features (Competition)
- [ ] Scoring System
- [ ] Leaderboard

**Note:** Mark [x] for completed features, [ ] for not implemented.

---

## Protocol Specification

### Overview
The Hangman protocol is a JSON-based communication system where the client sends requests with a "type" field indicating
the action and optional fields depending on the request type. The server is responsible for all game logic:
word selection, validation of guesses, tracking misses, and managing the game states. The client is "dumb":
it collects user input, sends requests, and displays server responses.

Each response includes an "ok" boolean indicating success, a "message" for human-readable feedback, and
other fields such as "word_display", "misses". etc. or "used_letters".

---

### 1. Set Player Name

**Request:**
```json
{
    "type": "name",
    "name": "<string>"
}
```

**Success Response:**
```json
{
    "type": "name",
    "ok": true,
    "message": "Welcome <name>! ..."
}
```

**Error Response:**
```json
{
    "ok": false,
    "message": "Name cannot be empty"
}
```

---

### 2. Start New Game

**Request:**
```json
{
  "type": "start_game"
}
```

**Success Response:**
```json
{
  "type": "start_game",
  "ok": true,
  "message": "Game started! Guess a letter or the whole word.",
  "word_display": "_ _ _ _ _",
  "misses": 0,
  "ascii_stage": "<ASCII art stage 0>"
}
```

**Error Response(s):**
```json
{
  "ok": false,
  "message": "A game is already in progress"
}
```

## Error Handling Strategy

Requests are validated server-side to make sure that protocol is consistent

**Server-side validation:**
- [What validations does your server perform?]
  The server checks for the presence of a "type" field in each request.

- [How do you handle missing fields?]
  Missing fields handled in a response with "ok": false and a meaningful "message".

- [How do you handle invalid data types?]
  Invalid data types are caught and responded with an error message.

- [How do you handle game state errors?]
  Game state errors are prevented.

---

## Robustness

It is explained as follows:

**Server robustness:**
- [How does server handle invalid input without crashing?]
- Invalid JSON input does not cause the server to stop; an error response is returned instead.
  Exceptions during processing are caught and logged.


**Client robustness:**
- [How does client handle unexpected responses?]
- The client validates server responses and displays errors clearly.

- [What happens if server is unavailable?]
- If the server is unavailable, the client displays a network error message and exits.

---

## Assumptions (if applicable)

[List any assumptions you made about the protocol or game rules]

1. Leaderboard stores only completed games.
2. Allow ony 6 misses before losing the game.
3. Hints cost 8 points.

---

## Known Issues

[List any known bugs or limitations]

1. Leaderboard is not yet implemented.
2. Hint feature is not yet implemented.

---

# Assignment 5 activity 1 – Distributed Consensus System

## Protocol Description

This project is about: Leader-Worker distributed consensus system.
    - The 'Leader node' coordinates computation by assigning arithmetic tasks to workers.
    - The 'Worker nodes' receive tasks, prompt a user to manually compute results, and send 
        responses back to the leader.
    - The system uses a 'majority voting consensus protocol' to determine the final correct 
        result.
The leader collects all worker responses, computes frequency distribution, and selects 
the value with the highest number of votes as the consensus result.

---

### How to Compile and Run (Gradle)

### 1. Navigate to project directory

    cd Assignment5/activity1

### 2. Run Leader (starting first)
    gradle runLeader

### or with custom port:

    gradle runLeader --args="9000"

### 3. Run Workers (run in separate terminals)

    gradle runWorker --args="Worker1 localhost 9000"
    gradle runWorker --args="Worker2 localhost 9000"
    gradle runWorker --args="Worker3 localhost 9000"
    gradle runWorker --args="Worker4 localhost 9000"
    gradle runWorker --args="Worker5 localhost 9000"

### Consensus Algorithm Design

### The system uses majority voting:

Leader sends a task (e.g., 23 + 19) to all workers.
Each worker manually inputs a result.
Leader collects all responses concurrently using CountDownLatch.
Leader counts frequency of each response.
The value with the highest number of votes becomes the consensus result.
### Decision Rule:
If a value has ≥ 50% of votes, it is accepted as consensus.
Otherwise, the system reports "No consensus reached".


### Worker Failure Handling

The system handles failures using a timeout functionality:
    - Leader waits for worker responses using CountDownLatch.await(timeout).
    - If a worker does not respond within the timeout period:
        - The leader proceeds with available responses.
        - Missing workers are excluded from consensus calculation.
This simulates real-world distributed systems where nodes may fail or late.

### Tie Handling Strategy

If two or more values have the same highest number of votes:
    - The system selects the first encountered highest-frequency value.
    - This ensures deterministic behavior.

### Edge Cases and Limitations
- If fewer than 50% of workers respond, consensus may not be reached.
- Worker input is manual, so human error may affect correctness.
- Network delays may cause timeout-based partial results.
- System assumes workers are reliable once connected (no reconnection logic).

### Issue Encountered and Fix
Issue:
    At first, the leader was not properly waiting for all worker responses before 
    computing consensus.

Diagnosis:
    Debugging showed that worker responses were being processed asynchronously without 
    proper synchronization.

Fix:
    Implemented CountDownLatch to ensure the leader waits until all worker threads 
    finish before proceeding to consensus computation.
    
This guaranteed correct synchronization in a distributed manner.
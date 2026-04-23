# Start leader
gradle runLeader

# Start workers (5 terminals)
gradle runWorker --args="Worker1 localhost 9000"
gradle runWorker --args="Worker2 localhost 9000"
gradle runWorker --args="Worker3 localhost 9000"
gradle runWorker --args="Worker4 localhost 9000"
gradle runWorker --args="Worker5 localhost 9000"
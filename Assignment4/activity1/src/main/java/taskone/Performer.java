package taskone;

import java.io.*;
import java.net.Socket;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;
import taskone.proto.Response;

// === FIX: Proto imports ===
import taskone.proto.Request;
import taskone.proto.TaskProto;

/**
 * Performer class handles client requests using JSON protocol.
 * This version uses JSON for serialization.
 */
public class Performer {
    private final Socket clientSocket;
    private final TaskList taskList;

    private InputStream inStream; // For proto
    private OutputStream outStream; // For proto

    private BufferedReader in; // For JSON
    private PrintWriter out; // For JSON

    public Performer(Socket clientSocket, TaskList taskList) {
        this.clientSocket = clientSocket;
        this.taskList = taskList;
    }

    /**
     * Main method to process client requests.
     * Reads requests, processes them, and sends responses.
     */
    public void doPerform() {
        try {
            inStream = clientSocket.getInputStream();
            outStream = clientSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(inStream));
            out = new PrintWriter(outStream, true);

            /////////////////////////////////////////////////////////////////////////////
            // Welcome JSON
            /////////////////////////////////////////////////////////////////////////////
            // Send welcome message. You can keep this as JSON.
            JSONObject welcomeMessage = JsonUtils.createSuccessResponse("connect", "Connected to Task Management Server");
            out.println(welcomeMessage);
            /////////////////////////////////////////////////////////////////////////////
            // End Welcome JSON
            /////////////////////////////////////////////////////////////////////////////



            /////////////////////////////////////////////////////////////////////////////
            // Welcome Proto
            /////////////////////////////////////////////////////////////////////////////
//            Response.Builder protoResp = Response.newBuilder().setType(Response.ResponseType.SUCCESS).setMessage("Connected to Proto Task Management Server");
//            protoResp.build().writeDelimitedTo(outStream);
            /////////////////////////////////////////////////////////////////////////////
            // End Welcome Proto
            /////////////////////////////////////////////////////////////////////////////




            // Process requests
            String request; // would need to be changed to proto

            // === FIX: use Proto request instead of JSON string ===
            Request protoRequest;

            while (true) {

                // === FIX: read Proto instead of JSON ===
                protoRequest = Request.parseDelimitedFrom(inStream);
                if (protoRequest == null) break;

                // We intentionally skip error handling here to focus on Proto conversion.
                // This may fail if the request is malformed or missing expected fields, which is ok this time.

                // Once you start changing things more and more to proto, the JSON parts might not work anymore.
                // That is fine, just do not call these requests until converted.
                // Start with the "add" request.

                System.out.println(protoRequest);

                // JSONObject requestJSON = new JSONObject(request);
                // String type = requestJSON.getString("type");

                // === FIX: get type from Proto ===
                Request.RequestType type = protoRequest.getType();

                JSONObject responseJSON;
                Response response;

                System.out.println(type);

                // Change all the following requests/responses to JSON here and in Client.java
                switch (type) {
                    case ADD:
                        // === FIX: use Proto handler ===
                        response = handleAddProto(protoRequest);
                        break;

                    case LIST:
                        responseJSON = handleList(new JSONObject());
                        out.println(responseJSON.toString());
                        continue;

                    case FINISH:
                        responseJSON = handleFinish(new JSONObject());
                        out.println(responseJSON.toString());
                        continue;

                    case QUIT:
                        // === FIX: Proto quit response ===
                        response = Response.newBuilder()
                                .setType(Response.ResponseType.SUCCESS)
                                .setMessage("Goodbye!")
                                .build();
                        break;

                    default:
                        // === FIX: Proto error response ===
                        response = Response.newBuilder()
                                .setType(Response.ResponseType.ERROR)
                                .setMessage("Unknown request type: " + type)
                                .build();
                }

                // === FIX: send Proto instead of JSON ===
                response.writeDelimitedTo(outStream);

                // If quit, break the loop
                if (type == Request.RequestType.QUIT) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    // === FIX: Proto version of add ===
    private Response handleAddProto(Request request) {
        String description = request.getDescription();
        String category = request.getCategory();

        Task task = taskList.addTask(description, category); // Assume valid input for this starter version.

        TaskProto taskProto = TaskProto.newBuilder()
                .setId(task.getId())
                .setDescription(task.getDescription())
                .setCategory(task.getCategory())
                .setFinished(task.isFinished())
                .build();

        return Response.newBuilder()
                .setType(Response.ResponseType.SUCCESS)
                .setMessage("Task added")
                .setTask(taskProto)
                .build();
    }

    private JSONObject handleAdd(JSONObject request) { // will need to change to not use JSON anymore - or make new method
        // Validation is intentionally removed so students can focus on Proto conversion.
        // These comments show what production-style validation would look like. You can also delete all these
        // if they annoy you since it makes it harder to read it

//        // Validate required fields
//        if (!request.has("description")) {
//            return JsonUtils.createErrorResponse("add", "Missing 'description' field");
//        }
//        if (!request.has("category")) {
//            return JsonUtils.createErrorResponse("add", "Missing 'category' field");
//        }

        String description = request.getString("description");
        String category = request.getString("category");

//        // Validate description not empty
//        if (description.trim().isEmpty()) {
//            return JsonUtils.createErrorResponse("add", "Description cannot be empty");
//        }
//
//        // Validate category value
//        if (!category.equals("work") && !category.equals("personal") && !category.equals("school") && !category.equals("other")) {
//            return JsonUtils.createErrorResponse("add", "Invalid category value. Must be 'work', 'personal', 'school', or 'other'");
//        }

        // Add task
        Task task = taskList.addTask(description, category); // Assume valid input for this starter version.

        // Return success response with created task
        return JsonUtils.createSuccessResponse("add", JsonUtils.taskToJson(task));
    }

    private JSONObject handleList(JSONObject request) {
        // Get filter (defaults to "all")
        String filter = request.optString("filter", "all");

        List<Task> tasks;
        switch (filter) {
            case "all":
                tasks = taskList.listAllTasks();
                break;
            case "pending":
                tasks = taskList.listPendingTasks();
                break;
            case "finished":
                tasks = taskList.listFinishedTasks();
                break;
            default:
                return JsonUtils.createErrorResponse("list", "Invalid filter value. Must be 'all', 'pending', or 'finished'"); // Keep similar error semantics in Proto.
        }

        // Convert tasks to JSON array
        JSONArray taskArray = new JSONArray();
        for (Task task : tasks) {
            taskArray.put(JsonUtils.taskToJson(task));
        }

        // Create response data
        JSONObject data = new JSONObject();
        data.put("tasks", taskArray);
        data.put("count", tasks.size());

        return JsonUtils.createSuccessResponse("list", data);
    }

    private JSONObject handleFinish(JSONObject request) {
        // Validation intentionally skipped.

        int id = request.getInt("id");
        // Mark task as finished
        boolean success = taskList.finishTask(id);

        if (success) {
            JSONObject data = new JSONObject();
            data.put("message", "Task #" + id + " marked as finished");
            return JsonUtils.createSuccessResponse("finish", data);
        } else {
            return JsonUtils.createErrorResponse("finish", "Task not found with ID: " + id);
        }
    }

    private JSONObject handleQuit() {
        JSONObject data = new JSONObject();
        data.put("message", "Goodbye!");
        return JsonUtils.createSuccessResponse("quit", data);
    }
}
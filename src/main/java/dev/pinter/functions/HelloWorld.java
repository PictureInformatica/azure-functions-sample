package dev.pinter.functions;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.io.Serializable;
import java.util.Optional;
import java.util.Random;

public class HelloWorld {
    /**
     * Function to create a message in the queue containing the 'name' received from http request.
     *
     * @param ctx context
     * @param req the http request
     * @param msg the message queue
     * @return http response
     */
    @FunctionName("helloWorld")
    public HttpResponseMessage helloWorld(ExecutionContext ctx,
                                          @HttpTrigger(name = "helloWorld", methods = {HttpMethod.GET},
                                                  authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> req,
                                          @QueueOutput(name = "msg", queueName = "my-message-queue",
                                                  connection = "AzureWebJobsStorage") OutputBinding<String> msg
    ) {
        ctx.getLogger().info("Request " + req.toString());
        String name = req.getQueryParameters().get("name");
        ctx.getLogger().info("Name: " + name);

        if (name != null && !name.isBlank()) {
            // send name to Azure Queue Storage
            msg.setValue(name);

            return req.createResponseBuilder(HttpStatus.OK)
                    //POJO is serialized to JSON
                    .body(new Example(new Random().nextInt(100000), name))
                    .build();
        }

        return req.createResponseBuilder(HttpStatus.BAD_REQUEST).body("ERROR: 'name' query parameter not specified").build();
    }

    /**
     * Function to receive message created by {@link HelloWorld#helloWorld}. Triggered when the message enters the queue.
     *
     * @param message the message from queue
     * @param ctx     context
     */
    @FunctionName("receiveMsg")
    public void receiveMsg(@QueueTrigger(name = "msg", queueName = "my-message-queue",
            connection = "AzureWebJobsStorage") String message, ExecutionContext ctx) {
        ctx.getLogger().info("Message received: " + message);
    }

    public static class Example implements Serializable {
        private final int id;
        private final String name;

        public Example(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

}

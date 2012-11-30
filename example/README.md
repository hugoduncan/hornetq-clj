# example

Example of using hornetq-clj.

## Usage

Start server with lein hornetq-server.
    lein hornetq-server :security-enabled false

Start a REPL with the project classpath (e.g. `lein swank`).

Make the queues in the server.
    (require 'stomp-example.core-api)
    (stomp-example.core-api/make-queues)

Start the service.
    (require 'stomp-example.stomp)
    (def service (stomp-example.stomp/run-service))

Run the consumer.
    (stomp-example.stomp/consumer)

You should see something like:

    Consumer connected
    Consumer subscribed
    Consumer sent message
    {:body {:type :MESSAGE, :headers {:destination /queue/consumer, :message-id 4294967313, :content-length 20, :priority 0, :subscription subscription//queue/consumer, :expires 0, :redelivered false, :timestamp 1295137286538}, :body Hello: i'm a message}}

## License

Copyright (C) 2010, 2011, 2012 Hugo Duncan

Distributed under the Eclipse Public License.

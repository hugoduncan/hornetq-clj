# example

Example of using hornetq-clj.

## Usage

Start server with lein hornetq-server.

    lein hornetq-server --disable-security

Start a REPL with the project classpath (e.g. `lein repl`).

Make the queues in the server.

```clj
(require 'hornetq-clj.example.core-api)
(hornetq-clj.example.core-api/make-queues {})
```

Start the service.

```clj
(def service (hornetq-clj.example.core-api/run-service))
```

Run the consumer.

```clj
(hornetq-clj.example.core-api/consumer)
```

You should see something like:

```
Message sent to service
Message received from service #<ClientMessageImpl ClientMessage[messageID=110, durable=false, address=/queue/consumer,userID=null,properties=TypedProperties[null]]>
Hello: I'm a message
```

## License

Copyright (C) 2010, 2011, 2012 Hugo Duncan

Distributed under the Eclipse Public License.

# stomp-example

Example of using stomp with hornetq.

## Usage

Start server with lein hornetq-server.
    lein hornetq-server :security-enabled false

Make the queues in the server.
    (stomp-example.core-api/make-queues)

Start the service.
    (def service (stomp-example.stomp/run-service))

Run the consumer.
    (stomp-example.stomp/consumer)

## Installation

Not designed to be installed...

## License

Copyright (C) 2010 Hugo Duncan

Distributed under the Eclipse Public License, the same as Clojure.

# lein-hornetq

Simple [lein](http://github.com/technomancy/leiningen) plugin to start
a [hornetq](http://www.jboss.org/hornetq) server.

## Usage

See the stomp
[example](http://github.com/hugoduncan/hornetq-clj/tree/master/stomp-example/).

To start a hornetq server with security disabled
    lein hornetq-server :security-enabled false

## Installation

Add the following to you project.clj :dev-dependencies
    [hornetq-clj/lein-hornetq "0.2.1"]

## License

Copyright (C) 2010, 2011 Hugo Duncan

Distributed under the Eclipse Public License.

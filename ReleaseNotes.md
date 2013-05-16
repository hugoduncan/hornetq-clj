# Release Notes

## 0.2.1

- Changed temporary-queue fn signatures to match `create-queue` and
  `ensure-queue`

- Fixed create-consumer docstring

- Update client/src/hornetq_clj/core_client.clj
  The setReconnectAttempts is a method of ServerLocator, not
  ClientSessionFactory , so the invoke order of setReconnectAttempts and
  createSessionFactory must be swap.


## 0.2.0

- Remove stomp client example
  No release of clj-stomp available

- Add core client test

- Update stomp example

- Update server starting code

- Add support for registering a message handler

- Add read-message-string and write-message-string

- Update server-locator code

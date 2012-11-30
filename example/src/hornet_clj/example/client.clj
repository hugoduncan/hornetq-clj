(ns hornetq-clj.example.client
  "Simple client test"
  (:import
   java.io.IOException
   java.net.Socket
   (java.util.concurrent
    Callable Future ExecutorService Executors TimeUnit
    ThreadFactory
    CancellationException ExecutionException TimeoutException)))

;;; test removed due to lack of stomp-clj release

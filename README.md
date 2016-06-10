A simple composable future implementation for Java.

A future can be created either directly by specifying the code to run, or
through a promise. For each operation you can specify the exector to run on
(and if no exector is specified, ForkJoinPool.commonPool() is used by default)


```java
Promise<String> p = CFutures.promise();
p.success("hello");
p.future().forEach(System.out::println);

CFutures.from(() -> 123)
        .map(e -> e * 2, anExector)  // this map() and the following forEach will run on the given exector  
        .forEach(System.out::println);

CFutures.from(() -> 1 / 0)
        .onFailure(System.out::println);
```


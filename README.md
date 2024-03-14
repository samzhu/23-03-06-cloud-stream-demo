# 消息平台-跨平台使用範例

這份文件提供了使用 Spring Cloud Stream 框架來開發跨平台解耦不同消息平台的範例程式碼。  

[下載專案模板](https://start.spring.io/#!type=gradle-project&language=java&platformVersion=3.0.4&packaging=jar&jvmVersion=17&groupId=com.example&artifactId=cloud-stream-demo&name=cloud-stream-demo&description=Spring%20Cloud%20Stream%20Demo&packageName=com.example.demo&dependencies=lombok,devtools,web,cloud-stream,cloud-gcp)  

## 增加 Binder

首先需要根據需要使用的消息平台，增加相應的 Binder 依賴。這裡提供了三種常用 Binder 的依賴引入方式：GCP Pub/Sub、Kafka 和 Nats。以 GCP Pub/Sub 為例：

``` groovy
implementation 'com.google.cloud:spring-cloud-gcp-pubsub-stream-binder'  
```

如果需要使用 Kafka

``` groovy
implementation 'org.springframework.cloud:spring-cloud-stream-binder-kafka'  
```

如果需要使用 Nats

``` groovy
implementation 'io.nats:nats-spring-cloud-stream-binder:0.5.6'  
```

在加入相應 Binder 的依賴後，就可以開始進行發送和接收消息的程式碼編寫。

## 發送消息設定對應

Spring Cloud Stream 提供了高層次的操作抽象，所以在程式碼中只需定義發送到 Binding Name 即可，具體發送到哪個消息平台則由設定檔決定。以發送到 GCP Pub/Sub 為例，程式碼如下：

``` java
private final StreamBridge streamBridge;

private final String bindingName = "order-created-out";

@GetMapping("/publish")
public void publish(@RequestParam("msg") String msg) {
    MsgDTO msgDTO = new MsgDTO(counter.incrementAndGet() + ":" + msg);
    Message<MsgDTO> message = CloudEventMessageBuilder.withData(msgDTO)
        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
        .build();
    streamBridge.send(bindingName, message);
}

```

可以看到，在程式碼中只需指定 Binding Name 為 "order-created-out"，而不需知道具體發送到 GCP Pub/Sub 上的哪個 Topic。這是因為在設定檔中已經指定了該 Binding Name 的 Binder 為 pubsub1，並且在 pubsub1 的設定中已經定義了目標 Topic 為 ORDERS.CREATED。

``` yaml
spring:
  cloud:
    stream:
      bindings:
        order-created-out:
          destination: ORDERS.CREATED
          content-type: application/json
          binder: pubsub1
      binders:
        pubsub1:
          type: pubsub
```

如果要切換成 Kafka，只需修改相應的設定即可。比如，在 Kafka 的設定中，需要指定 Broker 的地址：

``` yaml
spring:
  cloud:
    stream:
      bindings:
        order-created-out:
          destination: ORDERS.CREATED
          content-type: application/json
          binder: kafka1
      binders:
        kafka1:
          type: kafka
          environment:
            spring:
              cloud:
                stream:
                  kafka:
                    binder:
                      brokers: localhost:9092
```

## 接收消息設定對應

接收消息的程式碼需要註冊一個 Bean，並實現 Consumer 接口。Spring Cloud Stream 會自動把註冊的 Bean 和 Binding Name 對應起來，從而實現接收消息的功能。以 GCP Pub/Sub 為例，程式碼如下：

``` java
@Configuration
public class ConsumerConfiguration {

    @Bean
    public Consumer<Message<MsgDTO>> orderCreated() {
        return message -> {
            log.info("message={}", message.getPayload().getMsg());
        };
    }
}
```

在這個範例中，orderCreated 方法會被自動對應到 orderCreated-in-0 的 Binding Name，而 orderCreated-in-0 的 Binder 已經在設定檔中被指定為 pubsub1。設定檔如下：

``` yaml
spring:
  cloud:
    stream:
      bindings:
        orderCreated-in-0:
          destination: ORDERS.CREATED
          content-type: application/json
          group: orders
          binder: pubsub1
```

在這個設定檔中，orderCreated-in-0 已經被定義為訂閱 GCP Pub/Sub 上的 ORDERS.CREATED Topic，而且指定了消費者分組名為 orders。

## 備註

1. 透過此範例，可以在 application.yml 中進行 spring.application.profiles.active 來做快速切換試驗。
2. 在 GCP 範例中會建立 ORDERS.CREATED Topic 以及 ORDERS.CREATED.orders 的 Subscription。
3. Kafka 不支援 ORDERS.* 萬用字元，因此在設定中應避免使用。
4. 關於 Function 訂閱的命名請參考 [Functional binding names](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#_functional_binding_names)

除此之外，還可以根據實際需要進行其他設定，例如指定消息的 Content-Type、使用 SSL/TLS 加密等等。

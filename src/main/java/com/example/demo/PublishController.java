package com.example.demo;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PublishController {

    private final AtomicLong counter = new AtomicLong(0);

    private final StreamBridge streamBridge;

    private final String bindingName = "order-created-out";

    @GetMapping("/publish")
    public void publish(@RequestParam("msg") String msg) {

        MsgDTO msgDTO = new MsgDTO(counter.incrementAndGet() + ":" + msg);

        streamBridge.send(bindingName, msgDTO);

    }

}

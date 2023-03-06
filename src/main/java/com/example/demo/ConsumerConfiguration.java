package com.example.demo;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ConsumerConfiguration {

	@Bean
	public Consumer<Message<MsgDTO>> orderCreated() {
		return message -> {
			// log.info("message.getHeaders()={}", message.getHeaders());
			// log.info("message.getHeaders(ce-id)={}", message.getHeaders().get("ce-id"));
			// log.info("message.getPayload().getId()={}", message.getPayload().getId());
			log.info("message={}", message.getPayload().getMsg());
			// log.info("payload={}", message.getPayload());
		};
	}

}
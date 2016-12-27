package com.hand.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.ConsumerThread;
import com.hand.config.Config;
import com.hand.config.TopicConfig;
import com.hand.oracle.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by Hand on 2016/11/14.
 */

@RestController
public class TestController {
    @Autowired
    private StaffRepository staffRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }

    @RequestMapping(value = "add")
    public String add() {


        Properties props = new Properties();
//        try {
//            props.load(new FileInputStream("resources/consumer.properties"));
//        } catch (IOException e) {
//
//        }
        Config config = new Config(props);
        final Map<String, List<ConsumerThread>> consumersMap = new TreeMap<String, List<ConsumerThread>>();
        for (String topic : config.getTopics()) {
            TopicConfig topicConfig = new TopicConfig(props, topic);
            List<ConsumerThread> consumers = new ArrayList<ConsumerThread>(topicConfig.getThreadCount());
            for (int i = 0; i < topicConfig.getThreadCount(); i++) {
                try {
                    consumers.add(new ConsumerThread(config, topicConfig, staffRepository));
                } catch (Exception e) {

                }
            }
            consumersMap.put(topic, consumers);
        }
        for (Map.Entry<String, List<ConsumerThread>> entry : consumersMap.entrySet()) {
            for (ConsumerThread consumer : entry.getValue()) {
                consumer.start();
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (Map.Entry<String, List<ConsumerThread>> entry : consumersMap.entrySet()) {
                    for (ConsumerThread consumer : entry.getValue()) {
                        consumer.setRunning(false);
                    }
                }
            }
        });

        for (Map.Entry<String, List<ConsumerThread>> entry : consumersMap.entrySet()) {
            for (ConsumerThread consumer : entry.getValue()) {
                try {
                    consumer.join();
                } catch (Exception e) {
                }
            }
        }


        return "ok";
    }

}

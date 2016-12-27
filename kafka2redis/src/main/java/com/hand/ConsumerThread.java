package com.hand;

import com.hand.config.Config;
import com.hand.config.TopicConfig;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.hand.redis.StaffDao;
import java.util.*;
import java.util.logging.Logger;

public class ConsumerThread extends Thread {
    private static final Logger logger = Logger.getLogger(ConsumerThread.class.getName());
    private StaffDao staffDao;
    private Config config;
    private TopicConfig topicConfig;
    private KafkaConsumer<String,String> consumer;
    private Boolean running = false;
//    private Jedis jedis;

    public ConsumerThread(Config config, TopicConfig topicConfig, StaffDao staffDao) throws Exception {
        try {
            this.config = config;
            this.topicConfig = topicConfig;
            this.staffDao = staffDao;
            Properties props = new Properties();
            props.put(ConsumerConfig.GROUP_ID_CONFIG,config.getGroupId());
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,config.getBootstrapServer());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,org.apache.kafka.common.serialization.StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,org.apache.kafka.common.serialization.StringDeserializer.class);
            consumer = new KafkaConsumer<String, String>(props);
            consumer.subscribe(Collections.singletonList(topicConfig.getTopic()));
            running = true;
//            jedis = new Jedis(config.getHost(),config.getPort());
        }catch (Exception e){
            if (consumer!=null) {
                consumer.close();
            }
//            if (jedis!=null){
//                jedis.close();
//            }
            throw e;
        }
    }
    @Override
    public void run() {
        Thread.currentThread().setName("ConsumerThread-"+topicConfig.getTopic());
        try {
            while (running) {

                ConsumerRecords<String, String> records = consumer.poll(1000);
                logger.info(String.format("poll count:"+records.count()));
                HashMap<String, Object> staff1 = new HashMap<>();
                for (ConsumerRecord<String, String> record : records) {
                    String key = record.key();
                    String value = record.value();
                    System.out.println(record);
                    if(key!=null && value!=null) {
                        String[] names = value.split("\\|");
                        for (int i = 0; i < names.length; i++);
                        if(names[0].equals("add"))
                        {
                            HashMap<String, String> map = new HashMap<String, String>();
                            // 将json字符串转换成jsonObject
                            JSONObject jsonObject = JSONObject.fromObject(names[1]);
                            Iterator it = jsonObject.keys();
                            // 遍历jsonObject数据，添加到Map对象
                            while (it.hasNext())
                            {
                                String key1 = String.valueOf(it.next());
                                String value1 = jsonObject.get(key1).toString();
                                map.put(key1, value1);
                            }
//                                System.out.println(map);
                            staffDao.add(map);
                        }
                        else if(names[0].equals("del"))
                        {
                            staffDao.delete(names[1]);
                        }
                        else if(names[0].equals("up"))
                        {
                            HashMap<String, String> map1 = new HashMap<String, String>();
                            // 将json字符串转换成jsonObject
                            JSONObject jsonObject = JSONObject.fromObject(names[1]);
                            Iterator it = jsonObject.keys();
                            // 遍历jsonObject数据，添加到Map对象
                            while (it.hasNext())
                            {
                                String key1 = String.valueOf(it.next());
                                String value1 = jsonObject.get(key1).toString();
                                map1.put(key1, value1);
                            }
//                                System.out.println(map);
                            staffDao.update(map1);
                        }
                    }
                }
//                jedisPipe.sync();
            }
        }finally {
            if (consumer!=null) {
                consumer.close();
            }
        }
    }

    public Boolean getRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }
}

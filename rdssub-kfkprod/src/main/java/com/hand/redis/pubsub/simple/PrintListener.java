package com.hand.redis.pubsub.simple;

import com.hand.kafka.producer.kafkaProducer;
import org.apache.commons.lang.time.DateFormatUtils;
import redis.clients.jedis.JedisPubSub;

import java.util.Date;

public class PrintListener extends JedisPubSub {

    static String msg;
    private Thread kafakp = new Thread() {
        @Override
        public void run() {
            kafkaProducer kafkaproduce = new kafkaProducer();
            if (msg != null) {
                kafkaproduce.kp(msg);
                System.out.println("111");
            }
            System.out.println(msg);
        }
    };

    @Override
    public void onMessage(String channel, final String message) {
        String time = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");

        msg = message;


        kafakp.run();


        System.out.println("message receive:" + message + ",channel:" + channel + "..." + time);
        //此处我们可以取消订阅
        if (message.equalsIgnoreCase("quit")) {
            this.unsubscribe(channel);
        }
    }


    @Override
    public void onPMessage(String pattern, String channel, String message) {
        System.out.println("message receive:" + message + ",pattern channel:" + channel);

    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        System.out.println("subscribe:" + channel + ";total channels : " + subscribedChannels);

    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        System.out.println("unsubscribe:" + channel + ";total channels : " + subscribedChannels);

    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        System.out.println("unsubscribe pattern:" + pattern + ";total channels : " + subscribedChannels);

    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println("subscribe pattern:" + pattern + ";total channels : " + subscribedChannels);
    }
}

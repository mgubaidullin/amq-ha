package org.acme;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.activemq.ActiveMQComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AmqRoute extends RouteBuilder {
    private static final Logger LOGGER = Logger.getLogger(AmqRoute.class.getName());

    private AtomicInteger counter0 = new AtomicInteger();
    private AtomicLong sum0 = new AtomicLong();
    private AtomicLong min0 = new AtomicLong(Long.MAX_VALUE);
    private AtomicLong max0 = new AtomicLong(0);
    private AtomicReference<String> name0 = new AtomicReference("");

    @ConfigProperty(name = "broker")
    String broker;

    @Override
    public void configure() throws Exception {
        LOGGER.info("Start consumer for broker: " + broker);
        errorHandler(deadLetterChannel("log:dead-letter")
                .maximumRedeliveries(10000).redeliveryDelay(50));

        from("activemq:demo").id("consumer").process(e -> process(e, counter0, sum0, min0, max0, name0));

    }

    private void process(Exchange exchange, AtomicInteger counter, AtomicLong sum, AtomicLong min, AtomicLong max, AtomicReference<String> name) {
        try {
            long currentTime = System.currentTimeMillis();
            String message = exchange.getIn().getBody(String.class);
            String[] data = message.split(",");
            String hostname = data[0];
            String sendTime = data[1];
            String value = data[2];
            long time = currentTime - Long.parseLong(sendTime);
            if (name.get().equals(hostname)) {
                name.set(hostname);
                counter.set(0);
                sum.set(0);
                min.set(Long.MAX_VALUE);
                max.set(0);
            }
            counter.incrementAndGet();
            sum.addAndGet(time);
            if (time > max.get()) max.set(time);
            if (time < min.get()) min.set(time);
            if (counter.get() % 1000 == 0) {
                LOGGER.infof("Count: %d, Sum: %d, Min: %d, Max: %d, Avg: %d", counter.get(), sum.get(), min.get(), max.get(), (sum.get() / counter.get()));
                counter.set(0);
                sum.set(0);
                min.set(Long.MAX_VALUE);
                max.set(0);
            }
        } catch (Exception e) {

        }
    }

    @Named("activemq")
    ActiveMQComponent createActiveMQComponent0() {
        ActiveMQComponent activemq = new ActiveMQComponent();
        activemq.setBrokerURL(broker);
        return activemq;
    }
}

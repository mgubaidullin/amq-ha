package org.acme;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.activemq.ActiveMQComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AmqRoute extends RouteBuilder {

    @ConfigProperty(name = "broker")
    String broker;

    private static final Logger LOGGER = Logger.getLogger(AmqRoute.class.getName());

    private AtomicInteger counter = new AtomicInteger();

    @Override
    public void configure() throws Exception {
        LOGGER.info("Start consumer for broker: " + broker);

        errorHandler(deadLetterChannel("log:dead-letter")
                .maximumRedeliveries(10000).redeliveryDelay(50));

        from("activemq:demo").id("consumer")
                .log("${body}")
                .process(this::process);
    }

    private void process(Exchange exchange) {
        LOGGER.info("Messages counter : " + counter.incrementAndGet());
    }

    @Named("activemq")
    ActiveMQComponent createActiveMQComponent0() {
        ActiveMQComponent activemq = new ActiveMQComponent();
        activemq.setBrokerURL(broker);
        return activemq;
    }
}

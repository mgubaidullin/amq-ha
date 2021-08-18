package org.acme;

import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class AmqRoute extends EndpointRouteBuilder {
    private static final Logger LOGGER = Logger.getLogger(AmqRoute.class.getName());

    private AtomicInteger counter = new AtomicInteger(0);
    private final String period = "1s";
    private final int repeatCount = 10;

    @ConfigProperty(name = "hostname")
    String hostname;

    @ConfigProperty(name = "broker")
    String broker;

    @Override
    public void configure() throws Exception {
        LOGGER.info("Start producer: " + hostname + ", broker: " + broker);

        errorHandler(deadLetterChannel("log:dead-letter")
                .maximumRedeliveries(10000).redeliveryDelay(50));

        from(timer("demo").period(period).repeatCount(repeatCount)).id("producer")
                .loop(100)
                    .process(this::process)
                    .to("activemq:demo");
    }

    private void process(Exchange exchange) {
        exchange.getIn().setBody(hostname + ", " + System.currentTimeMillis() + ", " + counter.getAndIncrement());
    }

    @Named("activemq")
    ActiveMQComponent createActiveMQComponent0() {
        ActiveMQComponent activemq = new ActiveMQComponent();
        activemq.setBrokerURL(broker);
        return activemq;
    }
}

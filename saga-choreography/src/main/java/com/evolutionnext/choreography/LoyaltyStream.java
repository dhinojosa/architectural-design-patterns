package com.evolutionnext.choreography;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.time.Instant;
import java.util.Properties;

public class LoyaltyStream {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,
            "my_streams_app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
            Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
            Serdes.Integer().getClass());


        Serde<PointsEvent> pointsEventSerde = new SpecificAvroSerde<>();
        StreamsBuilder builder = new StreamsBuilder();
        KStream<Long, PaymentEvent> paymentStream =
            builder.stream("payments");
        Produced<Long, PointsEvent> kvProduced = Produced.with(Serdes.Long(), pointsEventSerde);
        KStream<Long, PointsEvent> mapped = paymentStream
            .filter((key, event) -> event.getAction() instanceof PaymentTaken)
            .map((key, event) -> new KeyValue<>(key, (PaymentTaken) event.getAction()))
            .map((key, event) -> new KeyValue<>(key, new PointsAwarded(Instant.now(), event.getOrderId(), 30)))
            .map((key, event) -> new KeyValue<>(key, new PointsEvent(event, event.getOrderId())));
        mapped
            .to("points", kvProduced);


    }
}

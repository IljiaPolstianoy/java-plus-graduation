package ru.practicum.kafka;

import stats.service.collector.UserActionProto;

public interface SendKafka {

    boolean send(UserActionProto request);
}

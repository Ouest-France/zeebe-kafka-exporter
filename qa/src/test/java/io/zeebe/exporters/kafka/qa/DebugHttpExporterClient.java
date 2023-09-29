/*
 * Copyright © 2019 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.exporters.kafka.qa;

import io.camunda.zeebe.protocol.record.Record;
import io.zeebe.exporters.kafka.serde.RecordId;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;


/**
 * A dumb client for the DebugHttpExporter. This exporter starts a server on a single broker for all
 * known partitions (of that broker), and simply exposes a poll mechanism for the records.
 *
 * <p>NOTE: the server returns records in reverse order, from newest to oldest, which is the
 * opposite of what we typically want, i.e. sorted in causal order. The {@link #streamRecords()}
 * method therefore returns them reversed.
 *
 * <p>NOTE: the streaming is "dumb", and really only returns the records from the server as is as a
 * stream. This is fine for now since we typically don't have a lot of records, but it means you may
 * have to call the method multiple times.
 */
final class DebugHttpExporterClient {

  private final Consumer<RecordId, Record<?>> consumer;
  private final List<ConsumerRecord<RecordId, Record<?>>> records = new ArrayList<>();

  DebugHttpExporterClient(final Consumer<RecordId, Record<?>> consumer) {
    this.consumer = consumer;
  }

  Stream<Record<?>> streamRecords() {

    final var timeout = Duration.ofSeconds(5);

      final var consumedRecords = consumer.poll(timeout);
      for (final var consumedRecord : consumedRecords) {
        records.add(consumedRecord);
      }


      Collections.reverse(records);

      return records.stream().map(r -> r.value());

  }
}

package hs.jfx.eventstream.impl;

import hs.jfx.eventstream.ChangeSource;
import hs.jfx.eventstream.util.Sink;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultStreamTest {
  private final ChangeSource<String> source = new ChangeSource<>();
  private final DefaultStream<String> stream = new DefaultStream<>(source, () -> "Default");

  @Test
  public void shouldEmitDefaultOnSubscribe() {
    Sink<String> sink = new Sink<>();

    stream.subscribe(sink::add);

    assertEquals("Default", sink.single());

    source.push("New");

    assertEquals("New", sink.single());

    Sink<String> sink2 = new Sink<>();

    stream.subscribe(sink2::add);

    assertEquals("Default", sink2.single());
    assertTrue(sink.isEmpty());
  }
}

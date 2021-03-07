package hs.jfx.eventstream.core;

import hs.jfx.eventstream.api.ChangeStream;
import hs.jfx.eventstream.api.EventStream;
import hs.jfx.eventstream.api.ValueStream;
import hs.jfx.eventstream.core.impl.RootChangeStream;

import static org.junit.jupiter.api.Assertions.fail;

public class TestUtil {

  public static <T> boolean filterFailOnNull(T input) {
    if(input == null) {
      fail("Null not allowed");
    }

    return true;
  }

  public static <T> T mapFailOnNull(T input) {
    if(input == null) {
      fail("Null not allowed");
    }

    return input;
  }

  public static <T> ValueStream<T> valueFlatMapFailOnNull(T input) {
    if(input == null) {
      fail("Null not allowed");
    }

    return Values.constant(input);
  }

  public static <T> ChangeStream<T> changeFlatMapFailOnNull(T input) {
    if(input == null) {
      fail("Null not allowed");
    }

    return RootChangeStream.empty();
  }

  public static <T> EventStream<T> eventFlatMapFailOnNull(T input) {
    if(input == null) {
      fail("Null not allowed");
    }

    return Events.empty();
  }
}

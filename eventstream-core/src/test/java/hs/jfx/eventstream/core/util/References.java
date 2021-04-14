package hs.jfx.eventstream.core.util;

import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class References {

  public static void assertUncollectable(Object obj, Runnable clearRefs) {
    WeakReference<Object> ref = new WeakReference<>(obj);

    clearRefs.run();
    obj = null;

    System.gc();

    assertNotNull(ref.get());
  }

  public static void assertCollectable(Object obj, Runnable clearRefs) {
    WeakReference<Object> ref = new WeakReference<>(obj);

    clearRefs.run();
    obj = null;

    System.gc();

    assertNull(ref.get());
  }
}

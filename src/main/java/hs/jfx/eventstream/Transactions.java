package hs.jfx.eventstream;

import java.util.ArrayList;
import java.util.List;

// TODO experimental
public class Transactions {
  private static boolean inhibitted;
  private static List<Runnable> callbacks = new ArrayList<>();

  public static void inhibit() {
    inhibitted = true;
  }

  public static void uninhibit() {
    inhibitted = false;

    for(Runnable runnable : callbacks) {
      runnable.run();
    }

    callbacks.clear();
  }

  public static Subscription register(Runnable callback) {  // TODO should be named registerOnce to make clear this happens only once
    callbacks.add(callback);

    return () -> unregister(callback);
  }

  public static void unregister(Runnable callback) {
    callbacks.remove(callback);
  }

  public static boolean inhibitted() {
    return inhibitted;
  }

  public static boolean inProgress() {
    return inhibitted;
  }

  // TODO rename
  public static void doWhile(Runnable runnable) {
    inhibit();
    runnable.run();
    uninhibit();
  }
}
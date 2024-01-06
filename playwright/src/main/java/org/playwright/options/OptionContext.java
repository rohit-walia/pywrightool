package org.playwright.options;

import com.microsoft.playwright.PlaywrightException;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class OptionContext {
  @Getter
  private static final Map<OptionContext.Key, IOption> context = new LinkedHashMap<>();

  private OptionContext() {
    throw new PlaywrightException("TestContext should not be instantiated!");
  }

  public static void add(OptionContext.Key key, IOption value) {
    context.put(key, value);
  }

  public static void addAll(Map<OptionContext.Key, IOption> testContext) {
    context.putAll(testContext);
  }

  public static void clearTestContext() {
    context.clear();
  }

  public static boolean exists(OptionContext.Key key) {
    return context.containsKey(key);
  }

  public enum Key {
    BROWSER_CONTEXT_OPTION,
    BROWSER_LAUNCH_OPTION,
    PLAYWRIGHT_OPTION,
    SCREENSHOT_OPTION,
    TRACE_START_OPTION,
    TRACE_STOP_OPTION,
  }
}

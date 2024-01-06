package org.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import lombok.SneakyThrows;
import org.playwright.failsafe.FailsafeRetry;
import org.playwright.options.BrowserContextOption;
import org.playwright.options.BrowserLaunchOption;
import org.playwright.options.OptionContext;
import org.playwright.options.PlaywrightOption;
import org.playwright.options.ResourceOptionArg;
import org.playwright.options.TracingStartOption;
import org.playwright.options.TracingStopOption;

import java.util.Arrays;
import java.util.List;

/**
 * Abstract factory interface for Playwright resources. Use create() and close() methods
 * to manage the resources.
 */
public interface PlaywrightResourceFactory {
  /**
   * Create Playwright resources which includes: Playwright, Browser, BrowserContext. <br><br>
   *
   * <p>
   * The BrowserContext resource is always created as a new instance. <br>
   * The Browser and Playwright instances are reused if there is one already existing unless the
   * ResourceOptionArg.NEW_BROWSER_INSTANCE or ResourceOptionArg.NEW_PLAYWRIGHT_INSTANCE is passed as an argument.
   * These arguments will force the creation of new instances of Browser and Playwright regardless of whether are existing
   * instances initialized.</p><br>
   *
   * <p>The arguments passed to create() method are optional and can be passed in any order. In addition to passing
   * ResourceOptionArg constants, any of the Playwright resource option classes that implements IOption
   * can be passed as arguments. This will override the default options.</p>
   *
   * @param clazz Class name of resource
   * @param <T>   Type of class
   * @param args  Optional arguments for resource creation
   * @return Playwright resource
   */
  @SuppressWarnings("unchecked")
  static <T extends AutoCloseable> T create(Class<T> clazz, Object... args) {
    if (clazz.equals(Playwright.class)) {
      List<ResourceOptionArg> argsList = Arrays.stream(args)
          .filter(arg -> arg instanceof ResourceOptionArg)
          .map(arg -> (ResourceOptionArg) arg)
          .toList();

      if (PlaywrightObj.getInstance() != null && !argsList.contains(ResourceOptionArg.NEW_PLAYWRIGHT_INSTANCE)) {
        return (T) PlaywrightObj.getInstance();
      }

      PlaywrightOption options = Arrays.stream(args)
          .filter(arg -> arg instanceof PlaywrightOption)
          .map(arg -> (PlaywrightOption) arg)
          .findFirst()
          .orElse(OptionContext.exists(OptionContext.Key.PLAYWRIGHT_OPTION)
              ? (PlaywrightOption) OptionContext.getContext().get(OptionContext.Key.PLAYWRIGHT_OPTION)
              : PlaywrightOption.builder().build());

      // failsafe retry put in place to avoid rare occurrence of playwright driver failing to initialize at Runtime.
      FailsafeRetry.tryAgain(() -> PlaywrightObj.setInstance(Playwright.create(options.forPlaywright())), 5, 1);

      OptionContext.add(OptionContext.Key.PLAYWRIGHT_OPTION, options);

      return (T) PlaywrightObj.getInstance();
    }

    if (clazz.equals(Browser.class)) {
      List<ResourceOptionArg> argsList = Arrays.stream(args)
          .filter(arg -> arg instanceof ResourceOptionArg)
          .map(arg -> (ResourceOptionArg) arg)
          .toList();

      if (BrowserObj.getInstance() != null && !argsList.contains(ResourceOptionArg.NEW_BROWSER_INSTANCE)) {
        return (T) BrowserObj.getInstance();
      }

      if (PlaywrightObj.getInstance() == null) {
        throw new PlaywrightException("Playwright instance is not initialized. Please initialize Playwright before "
            + "creating a Browser.");
      }
      Playwright playwright = PlaywrightObj.getInstance();

      BrowserLaunchOption options = Arrays.stream(args)
          .filter(arg -> arg instanceof BrowserLaunchOption)
          .map(arg -> (BrowserLaunchOption) arg)
          .findFirst()
          .orElse(OptionContext.exists(OptionContext.Key.BROWSER_LAUNCH_OPTION)
              ? (BrowserLaunchOption) OptionContext.getContext().get(OptionContext.Key.BROWSER_LAUNCH_OPTION)
              : BrowserLaunchOption.builder().build());

      Browser browser = switch (options.getBrowser()) {
        case "chromium", "chrome", "msedge" -> playwright.chromium().launch(options.forPlaywright());
        case "firefox" -> playwright.firefox().launch(options.forPlaywright());
        case "webkit" -> playwright.webkit().launch(options.forPlaywright());
        default -> throw new PlaywrightException("Unsupported browser: " + options.getBrowser());
      };

      BrowserObj.setInstance(browser);
      OptionContext.add(OptionContext.Key.BROWSER_LAUNCH_OPTION, options);
      return (T) BrowserObj.getInstance();
    }

    if (clazz.equals(BrowserContext.class)) {
      BrowserContextOption browserContextOption = Arrays.stream(args)
          .filter(arg -> arg instanceof BrowserContextOption)
          .map(arg -> (BrowserContextOption) arg)
          .findFirst()
          .orElse(OptionContext.exists(OptionContext.Key.BROWSER_CONTEXT_OPTION)
              ? (BrowserContextOption) OptionContext.getContext().get(OptionContext.Key.BROWSER_CONTEXT_OPTION)
              : BrowserContextOption.builder().build());

      TracingStartOption tracingStartOption = Arrays.stream(args)
          .filter(arg -> arg instanceof TracingStartOption)
          .map(arg -> (TracingStartOption) arg)
          .findFirst()
          .orElse(OptionContext.exists(OptionContext.Key.TRACE_START_OPTION)
              ? (TracingStartOption) OptionContext.getContext().get(OptionContext.Key.TRACE_START_OPTION)
              : TracingStartOption.builder().build());

      if (BrowserObj.getInstance() == null) {
        throw new PlaywrightException("Browser instance is not initialized. Please initialize Browser before "
            + "creating a BrowserContext.");
      }
      BrowserContext browserCtx = BrowserObj.getInstance().newContext(browserContextOption.forPlaywright());
      browserCtx.tracing().start(tracingStartOption.forPlaywright());

      OptionContext.add(OptionContext.Key.BROWSER_CONTEXT_OPTION, browserContextOption);
      OptionContext.add(OptionContext.Key.TRACE_START_OPTION, tracingStartOption);

      return (T) browserCtx;
    }

    throw new PlaywrightException("Not a Playwright resource.");
  }


  /**
   * Close Playwright resource.
   *
   * @param object resource
   * @param args   Optional arguments for resource creation
   */
  @SneakyThrows
  static <T extends AutoCloseable> void close(T object, Object... args) {
    if (object instanceof BrowserContext) {
      TracingStopOption tracingStopOption = Arrays.stream(args)
          .filter(arg -> arg instanceof TracingStopOption)
          .map(arg -> (TracingStopOption) arg)
          .findFirst()
          .orElse(OptionContext.exists(OptionContext.Key.TRACE_STOP_OPTION)
              ? (TracingStopOption) OptionContext.getContext().get(OptionContext.Key.TRACE_STOP_OPTION)
              : TracingStopOption.builder().build());

      ((BrowserContext) object).tracing().stop(tracingStopOption.forPlaywright());
    }
    object.close();
  }

  class PlaywrightObj {
    private static ThreadLocal<Playwright> playwright = new ThreadLocal<>();

    private PlaywrightObj() {
      throw new PlaywrightException("PlaywrightObj should not be instantiated!");
    }

    public static Playwright getInstance() {
      return playwright.get();
    }

    public static void setInstance(Playwright playwright) {
      PlaywrightObj.playwright.set(playwright);
    }
  }

  class BrowserObj {
    private static ThreadLocal<Browser> browser = new ThreadLocal<>();

    private BrowserObj() {
      throw new PlaywrightException("BrowserObj should not be instantiated!");
    }

    public static synchronized Browser getInstance() {
      return browser.get();
    }

    public static synchronized void setInstance(Browser browser) {
      BrowserObj.browser.set(browser);
    }
  }
}

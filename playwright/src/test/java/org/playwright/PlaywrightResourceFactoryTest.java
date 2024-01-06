package org.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.playwright.jackson.JacksonUtils;
import org.playwright.options.BrowserContextOption;
import org.playwright.options.BrowserLaunchOption;
import org.playwright.options.IOption;
import org.playwright.options.OptionContext;
import org.playwright.options.PlaywrightOption;
import org.playwright.options.ResourceOptionArg;
import org.playwright.options.TracingStartOption;

class PlaywrightResourceFactoryTest {

  @Test
  void testPlaywrightResourceFactory_CreatingInChronologicalOrder() {
    Playwright playwright1 = PlaywrightResourceFactory.create(Playwright.class);
    Browser browser1 = PlaywrightResourceFactory.create(Browser.class);
    BrowserContext browserContext1 = PlaywrightResourceFactory.create(BrowserContext.class);

    // verify all resources are not null
    Assertions.assertNotNull(playwright1);
    Assertions.assertNotNull(browser1);
    Assertions.assertNotNull(browserContext1);

    // When no arguments passed to create, the default PlaywrightOption is set in the context
    // verify it exists in the context, and it has default values
    IOption playwrightOption = OptionContext.getContext().get(OptionContext.Key.PLAYWRIGHT_OPTION);
    Assertions.assertEquals(JacksonUtils.serializeToString(playwrightOption),
        JacksonUtils.serializeToString(PlaywrightOption.builder().build()));

    // When no arguments passed to create, the default BrowserLaunchOption is set in the context
    // verify it exists in the context, and it has default values
    IOption browserLaunchOption = OptionContext.getContext().get(OptionContext.Key.BROWSER_LAUNCH_OPTION);
    Assertions.assertEquals(JacksonUtils.serializeToString(browserLaunchOption),
        JacksonUtils.serializeToString(BrowserLaunchOption.builder().build()));

    // When no arguments passed to create, the default BrowserLaunchOption and TracingStartOption are set in the context
    // verify it exists in the context, and it has default values
    IOption browserContextOption = OptionContext.getContext().get(OptionContext.Key.BROWSER_CONTEXT_OPTION);
    Assertions.assertEquals(JacksonUtils.serializeToString(browserContextOption),
        JacksonUtils.serializeToString(BrowserContextOption.builder().build()));

    IOption tracingStartOption = OptionContext.getContext().get(OptionContext.Key.TRACE_START_OPTION);
    Assertions.assertEquals(JacksonUtils.serializeToString(tracingStartOption),
        JacksonUtils.serializeToString(TracingStartOption.builder().build()));

    // creating additional BrowserContext resources should create new instance
    BrowserContext browserContext2 = PlaywrightResourceFactory.create(BrowserContext.class);
    Assertions.assertNotEquals(browserContext1, browserContext2);

    // creating additional Browser resources, without passing args, should reuse existing instance
    Browser browser2 = PlaywrightResourceFactory.create(Browser.class);
    Assertions.assertEquals(browser1, browser2);


    // creating additional Browser resources, with passing args, should reuse existing instance
    // and override the existing BrowserLaunchOption
    Browser browser3 = PlaywrightResourceFactory.create(Browser.class, ResourceOptionArg.NEW_BROWSER_INSTANCE,
        BrowserLaunchOption.builder().headless(true).build());
    Assertions.assertNotEquals(browser2, browser3);

    IOption browserContextOption2 = OptionContext.getContext().get(OptionContext.Key.BROWSER_CONTEXT_OPTION);
    Assertions.assertEquals(JacksonUtils.serializeToString(browserContextOption2),
        JacksonUtils.serializeToString(BrowserContextOption.builder().build()));

    // close all resources
    PlaywrightResourceFactory.close(browserContext1);
    PlaywrightResourceFactory.close(browserContext2);
    PlaywrightResourceFactory.close(browser3);
    PlaywrightResourceFactory.close(browser2);
    PlaywrightResourceFactory.close(browser1);
    PlaywrightResourceFactory.close(playwright1);

    // verify resources are disconnected and can't be invoked after they are closed
    Assertions.assertFalse(browser1.isConnected());
    Assertions.assertFalse(browser2.isConnected());
    Assertions.assertFalse(browser3.isConnected());
    Assertions.assertThrows(PlaywrightException.class, () -> browserContext2.setDefaultTimeout(1));
    Assertions.assertThrows(PlaywrightException.class, () -> playwright1.chromium().launch());
  }

  @Test
  void testPlaywrightResourceFactory_CreatingInNonChronologicalOrder() {
    // try creating BrowserContext without creating upstream resources first
    Assertions.assertThrows(PlaywrightException.class, () -> PlaywrightResourceFactory.create(BrowserContext.class));

    // try creating Browser without creating upstream resource first
    Assertions.assertThrows(PlaywrightException.class, () -> PlaywrightResourceFactory.create(Browser.class));
  }
}
package org.playwright.options;

import com.microsoft.playwright.BrowserType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Setter
@Getter
@Builder(toBuilder = true)
@Jacksonized
public class BrowserLaunchOption implements IOption {
  @Builder.Default
  private boolean headless = false;

  @Builder.Default
  private double slowmo = 300;

  @Builder.Default
  private String browser = "chrome";

  @Builder.Default
  private double browserStartTimeout = 30000;

  /**
   * Converts this builder BrowserLaunchOption instance to Playwright BrowserType.LaunchOptions object.
   *
   * @return Page.ScreenshotOptions
   */
  public BrowserType.LaunchOptions forPlaywright() {
    BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
        .setHeadless(headless)
        .setSlowMo(slowmo)
        .setTimeout(browserStartTimeout);

    if ("chrome".equalsIgnoreCase(browser) || "msedge".equalsIgnoreCase(browser)) {
      launchOptions.setChannel(browser);
    }

    return launchOptions;
  }
}

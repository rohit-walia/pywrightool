package org.playwright.options;

import com.microsoft.playwright.Playwright;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Setter
@Getter
@Builder(toBuilder = true)
@Jacksonized
public class PlaywrightOption implements IOption {
  @Builder.Default
  private boolean enableDebugMode = false;


  /**
   * Converts this builder PlaywrightOption instance to Playwright Playwright.CreateOptions object.
   *
   * @return Playwright.CreateOptions
   */
  public Playwright.CreateOptions forPlaywright() {
    Playwright.CreateOptions options = new Playwright.CreateOptions();

    if (enableDebugMode) {
      options.setEnv(Map.of("PWDEBUG", "1", "PLAYWRIGHT_JAVA_SRC", "src/test/java"));
    }

    return options;
  }
}

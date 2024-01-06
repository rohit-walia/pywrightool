package org.playwright.options;

import com.microsoft.playwright.Page;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.nio.file.Path;

@Setter
@Getter
@Builder(toBuilder = true)
@Jacksonized
public class ScreenshotOption implements IOption {
  @Builder.Default
  private Path path = Path.of("target/screenshot/screenshot.png");

  @Builder.Default
  private boolean fullPage = true;

  /**
   * Converts this builder ScreenshotOption instance to Playwright Page.ScreenshotOptions object.
   *
   * @return Page.ScreenshotOptions
   */
  public Page.ScreenshotOptions forPlaywright() {
    return new Page.ScreenshotOptions()
        .setPath(path)
        .setFullPage(fullPage);
  }
}

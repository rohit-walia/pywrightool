package org.playwright.options;


import com.microsoft.playwright.Tracing;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Setter
@Getter
@Builder(toBuilder = true)
@Jacksonized
public class TracingStartOption implements IOption {
  @Builder.Default
  private boolean enableScreenshot = true;

  @Builder.Default
  private boolean enableSnapshot = true;

  @Builder.Default
  private boolean enableSource = false;

  /**
   * Converts this builder TracingStartOption instance to Playwright Tracing.StartOptions object.
   *
   * @return Tracing.StartOptions
   */
  public Tracing.StartOptions forPlaywright() {
    return new Tracing.StartOptions()
        .setScreenshots(enableScreenshot)
        .setSnapshots(enableSnapshot)
        .setSources(enableSource);
  }
}

package org.playwright.options;

import com.microsoft.playwright.Browser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.nio.file.Paths;

@Setter
@Getter
@Builder(toBuilder = true)
@Jacksonized
public class BrowserContextOption implements IOption {
  @Builder.Default
  private String recordVideoDir = "target/video/";

  @Builder.Default
  private Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();


  /**
   * Converts this builder Browser.NewContextOptions instance to Playwright Browser.NewContextOptions object.
   *
   * @return Browser.NewContextOptions
   */
  public Browser.NewContextOptions forPlaywright() {
    return new Browser.NewContextOptions()
        .setViewportSize(dimension.width, dimension.height)
        .setRecordVideoDir(Paths.get(recordVideoDir))
        .setRecordVideoSize(dimension.width, dimension.height);
  }
}

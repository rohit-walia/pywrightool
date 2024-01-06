package org.playwright.options;


import com.microsoft.playwright.Tracing;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Setter
@Getter
@Builder(toBuilder = true)
@Jacksonized
public class TracingStopOption implements IOption {
  @Builder.Default
  private Path tracingPath = Paths.get("target/trace/default.zip");

  /**
   * Converts this builder TracingStopOption instance to Playwright Tracing.StopOptions object.
   *
   * @return Tracing.StopOptions
   */
  public Tracing.StopOptions forPlaywright() {
    log.info("TracingStopOptions: trace file recorded in directory: {}", tracingPath);
    return new Tracing.StopOptions().setPath(tracingPath);
  }
}

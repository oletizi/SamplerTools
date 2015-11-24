package com.orion.sampler.tools.program;

import com.orion.sampler.features.SamplePower;
import net.beadsproject.beads.data.Sample;
import org.apache.commons.io.output.StringBuilderWriter;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

public class PercussionProgramMaker {

  public String createRegion(byte noteNumber, List<Sample> samples) {
    final StringBuilderWriter sbw = new StringBuilderWriter();
    final PrintWriter out = new PrintWriter(sbw);

    // sort samples by power
    Collections.sort(samples, (o1, o2) -> {
      final SamplePower samplePower = new SamplePower();
      return (int) (samplePower.getPower(o1) - samplePower.getPower(o2));
    });

    final int velspan = 128 / samples.size();

    int currentVelocity = 0;
    for (Sample sample : samples) {
      out.println("<region>");
      out.println("sample=" + new File(sample.getFileName()).getName());
      out.println("key=" + noteNumber);
      out.println("lowvel=" + currentVelocity);
      out.println("hivel=" + (currentVelocity + velspan - 1));
      out.println();
      currentVelocity += velspan;
    }

    return sbw.toString();
  }
}

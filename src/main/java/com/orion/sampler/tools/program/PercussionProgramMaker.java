package com.orion.sampler.tools.program;

import com.orion.sampler.features.TransientLocator;
import com.orion.sampler.io.PercussionSourceSampleSelector;
import com.orion.sampler.midi.Percussion;
import com.orion.sampler.tools.Slicer;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.data.Sample;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PercussionProgramMaker {

  private final String programName;
  private final File sourceDir;
  private final File destDir;
  private float transientThreshold;
  private final AudioContext ac;
  private int preroll;

  public PercussionProgramMaker(final String programName, final float transientThreshold, final int preroll,
                                final File sourceDir, final File destDir) throws IOException {
    this.programName = programName;
    this.transientThreshold = transientThreshold;
    this.preroll = preroll;
    this.sourceDir = sourceDir;
    this.destDir = destDir;
    ac = new AudioContext(new NonrealtimeIO());
  }

  public String createRegion(final Percussion key, final List<Sample> samples) {
    final StringBuilderWriter sbw = new StringBuilderWriter();
    final PrintWriter out = new PrintWriter(sbw);

    // TODO: debug the power sorting
//    Collections.sort(samples, (o1, o2) -> {
//      final SamplePower samplePower = new SamplePower();
//      return (int) (samplePower.getPower(o1) - samplePower.getPower(o2));
//    });

    final int velspan = 128 / samples.size();

    int currentVelocity = 0;
    for (int i = 0; i < samples.size(); i++) {
      currentVelocity += velspan;
      final Sample sample = samples.get(i);
      out.println("<region>");
      out.println("sample=" + new File(sample.getFileName()).getName());
      out.println("key=" + key.key().getValue());
      //out.println("lovel=" + currentVelocity);
      out.println("hivel=" + ((i + 1 == samples.size()) ? 127 : currentVelocity));
      out.println();
    }

    return sbw.toString();
  }

  public String createProgramFromSamples(final PercussionSourceSampleSelector selector) {
    final StringBuilderWriter sbw = new StringBuilderWriter();
    final PrintWriter out = new PrintWriter(sbw);

    final Map<Percussion, List<Sample>> samples = selector.getAllSamples();
    info("Creating program from " + samples.size() + " samples...");
    for (Map.Entry<Percussion, List<Sample>> entry : samples.entrySet()) {
      out.append(createRegion(entry.getKey(), new ArrayList<>(entry.getValue())));
    }

    return sbw.toString();
  }

  public void writeProgramFromSource() throws IOException {
    sliceSource();
    final Writer out = new OutputStreamWriter(new FileOutputStream(new File(destDir, programName + ".sfz")));
    final String program = createProgramFromSamples(new PercussionSourceSampleSelector(destDir));
    info("Writing program: " + program);
    out.write(program);
    out.flush();
    out.close();
  }

  private void sliceSource() throws IOException {
    if (destDir.exists() && !destDir.isDirectory()) {
      throw new IOException("Destination is not a directory: " + destDir);
    }
    FileUtils.forceMkdir(destDir);

    final Map<Percussion, List<Sample>> sourceSamples = new PercussionSourceSampleSelector(sourceDir).getAllSamples();
    for (Map.Entry<Percussion, List<Sample>> entry : sourceSamples.entrySet()) {
      // slice the source audio...
      info("Slicing: key: " + entry.getKey());
      for (Sample source : entry.getValue()) {
        final TransientLocator thisLocator = new TransientLocator(ac, source, this.transientThreshold);
        final List<Sample> slices = new Slicer(ac, thisLocator).slice(preroll);
        for (int i = 0; i < slices.size(); i++) {
          final String filename = new File(destDir, entry.getKey().name() + "-" + i + ".wav").getAbsolutePath();
          info("Writing slice: " + filename);
          slices.get(i).write(filename);
        }
      }
    }
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }
}

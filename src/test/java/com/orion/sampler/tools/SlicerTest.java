package com.orion.sampler.tools;

import com.orion.sampler.features.TransientLocator;
import com.orion.sampler.features.TransientObserver;
import com.orion.sampler.midi.Percussion;
import com.orion.sampler.tools.percussion.ui.progress.ProgressObserver;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.IIRFilter;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class SlicerTest {

  protected Slicer slicer;
  protected TransientObserver observer;
  private ProgressObserver progressObserver;

  public void setup(final String resource, float transientThreshold) throws Exception {
    final AudioContext ac = new AudioContext(new NonrealtimeIO());
    final Sample sample = new Sample(ClassLoader.getSystemResource(resource).getFile());
    final IIRFilter filter = new BiquadFilter(ac, 2);
    observer = mock(TransientObserver.class);

    final TransientLocator locator = new TransientLocator(ac, sample, transientThreshold, filter, observer);
    progressObserver = mock(ProgressObserver.class);
    slicer = new Slicer(ac, locator, progressObserver);
  }

  @Test
  public void testTambo() throws Exception {
    setup("audio/count.wav", 0.002f);
    final List<Sample> slices = slicer.slice(10);

    verify(observer, times(4)).notifyTransient();
    assertEquals(4, slices.size());
  }

  @Test
  public void testCrash2() throws Exception {
    setup("audio/drumsource/crash2.wav", Percussion.CRASH2.getTransientThreshold());
    final List<Sample> slices = slicer.slice(10);

    verify(observer, times(8)).notifyTransient();
    assertEquals(8, slices.size());

  }

}
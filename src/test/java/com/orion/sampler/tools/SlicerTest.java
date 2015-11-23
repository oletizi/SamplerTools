package com.orion.sampler.tools;

import com.orion.sampler.features.TransientLocator;
import com.orion.sampler.features.TransientObserver;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.IIRFilter;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class SlicerTest {

  private Slicer slicer;
  private TransientObserver observer;

  @Before
  public void before() throws Exception {
    final AudioContext ac = new AudioContext(new NonrealtimeIO());
    final Sample sample = new Sample(ClassLoader.getSystemResource("audio/count.wav").getFile());
    final IIRFilter filter = new BiquadFilter(ac, 2);
    observer = mock(TransientObserver.class);

    final TransientLocator locator = new TransientLocator(ac, sample, 0, filter, observer);
    slicer = new Slicer(ac, locator);
  }

  @Test
  public void test() throws Exception {
    final List<Sample> slices = slicer.slice();

    verify(observer, times(4)).notifyTransient();
    assertEquals(4, slices.size());
  }

}
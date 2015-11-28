package com.orion.sampler.features;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.IIRFilter;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class TransientLocatorTest {

  private URL resource;
  private Sample sample;
  private float threshold;
  private IIRFilter filter;
  private TransientObserver observer;
  private AudioContext ac;

  @Before
  public void before() throws Exception {
    resource = ClassLoader.getSystemResource("audio/count.wav");
    sample = new Sample(resource.getFile());
    ac = new AudioContext(new NonrealtimeIO());
    filter = new BiquadFilter(ac, 2);
    observer = mock(TransientObserver.class);
  }

  @Test
  public void test() throws Exception {
    final TransientLocator locator = new TransientLocator(ac, sample, threshold, filter, observer);
    verify(observer, times(4)).notifyTransient();
    final List<Slice> slices = locator.getSlices();
    assertEquals(4, slices.size());

    Slice previousSlice = null;
    for (int i = 0; i < slices.size(); i++) {
      final Slice slice = slices.get(i);
      info("previous slice: " + previousSlice);
      info("current slice : " + slice);
      assertFalse("slice end frame " + slice.getEndFrame() + " is before or equal to slice start frame: " + slice.getStartFrame(),
          slice.getEndFrame() <= slice.getStartFrame());
      if (previousSlice != null) {
        assertTrue("previous slice start frame " + previousSlice.getStartFrame() + " is after current slice start frame " + slice.getStartFrame(),
            previousSlice.getStartFrame() < slice.getStartFrame());
        assertTrue("previous slice end frame " + previousSlice.getEndFrame() + " is after current slice start frame " + slice.getStartFrame(),
            previousSlice.getEndFrame() < slice.getStartFrame());
      }
      previousSlice = slice;
    }
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }
}
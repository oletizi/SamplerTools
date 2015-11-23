package com.orion.sampler.features;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.IIRFilter;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

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
  }
}
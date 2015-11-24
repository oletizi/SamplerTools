package com.orion.sampler.features;

import com.orion.sampler.util.SampleUtil;
import net.beadsproject.beads.data.Sample;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SamplePowerTest {

  private List<Sample> samples;

  @Before
  public void before() throws Exception {
    samples = new SampleUtil().loadSamples(ClassLoader.getSystemResource("samples/snare/"));
    assertEquals(8, samples.size());
  }

  @Test
  public void test() throws Exception {
    final SamplePower samplePower = new SamplePower();
    float power1 = samplePower.getPower(samples.get(0));
    float power2 = samplePower.getPower(samples.get(2));

    assertTrue("power1: " + power1 + " not greater than power2: " + power2, power1 < power2);
  }

}
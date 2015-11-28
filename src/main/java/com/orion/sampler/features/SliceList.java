package com.orion.sampler.features;

import java.util.ArrayList;
import java.util.List;

// TODO: Refactor this. Extending ArrayList is a terrible abstraction.
public class SliceList<T extends Slice> extends ArrayList<Slice> {

  private final List<Integer> zeroLevels = new ArrayList<>();

  public void notifyZeroLevel(int frameIndex) {
    zeroLevels.add(frameIndex);
  }

  public void commitZeroLevels() {
    for (Integer zeroLevel : zeroLevels) {
      final Slice slice = findSliceForFrameIndex(zeroLevel);
      if (slice != null) {
        slice.notifyZeroLevelIndex(zeroLevel);
      }
    }
  }

  private Slice findSliceForFrameIndex(int frameIndex) {
    Slice rv = null;
    for (int i = 0; i < size(); i++) {
      final Slice slice = get(i);
      if (frameIndex > slice.getStartTransient().getFrameIndex() && slice.getEndFrame() > 0) {
        // this frame index is after this slice's transient onset.
        // check to see if it's before the next slice. If this is the last slice AND it's after this slice's transient
        // onset, then we'll set the next slice to this slice and the boundary calculation should work.
        final Slice nextSlice = (i + 1 == size() ? slice : get(i + 1));
        info("current slice: " + slice + ", checking next slice: " + nextSlice);
        if (frameIndex <= nextSlice.getStartTransient().getFrameIndex()) {
          // this frame index is within the bounds of the current slice. Set the return value to this slice and
          // short-circuit the loop
          rv = slice;
          break;
        }
      }
    }
    // if we fell through, the return value will be null. Otherwise, it will be the slice that contains the frame
    // index
    return rv;
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

}

package org.p0.calendly.strategies;

import java.util.TreeMap;

public interface OverlapStrategy {

    public TreeMap<Long,Long> findOverlaps(TreeMap<Long,Long> u1, TreeMap<Long,Long> u2);
}

package org.p0.calendly.strategies;

import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeMap;

@Service
public class DefaultOverlapStrategy implements OverlapStrategy{

    @Override
    public TreeMap<Long, Long> findOverlaps(TreeMap<Long, Long> map1, TreeMap<Long, Long> map2) {
        TreeMap<Long, Long> overlapMap = new TreeMap<>();

        if(map1 == null || map1.isEmpty() || map2 == null || map2.isEmpty())
            return overlapMap;

        // Iterators for both maps
        Iterator<Map.Entry<Long, Long>> iterator1 = map1.entrySet().iterator();
        Iterator<Map.Entry<Long, Long>> iterator2 = map2.entrySet().iterator();

        // Get first intervals from both maps
        Map.Entry<Long, Long> interval1 = iterator1.hasNext() ? iterator1.next() : null;
        Map.Entry<Long, Long> interval2 = iterator2.hasNext() ? iterator2.next() : null;

        // Traverse both maps
        while (interval1 != null && interval2 != null) {
            long start1 = interval1.getKey();
            long end1 = interval1.getValue();
            long start2 = interval2.getKey();
            long end2 = interval2.getValue();

            // Find the maximum of the starting points and the minimum of the ending points
            long overlapStart = Math.max(start1, start2);
            long overlapEnd = Math.min(end1, end2);

            // Check if there is an overlap
            if (overlapStart <= overlapEnd) {
                overlapMap.put(overlapStart, overlapEnd);
            }

            // Move the iterator with the smaller ending point forward
            if (end1 < end2) {
                interval1 = iterator1.hasNext() ? iterator1.next() : null;
            } else {
                interval2 = iterator2.hasNext() ? iterator2.next() : null;
            }
        }

        return overlapMap;
    }
}

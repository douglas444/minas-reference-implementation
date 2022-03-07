package br.ufu.facom.minas.core.datastructure;

import java.util.Comparator;

/**
 * Class used to compare the distance of two points to a third point.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class PointDistanceComparator implements Comparator<Point> {

    private final Point target;

    public PointDistanceComparator(final Point target) {
        this.target = target;
    }

    /** Compares the distances of the two instances passed as argument to the
     * target instance defined as a class attribute.
     *
     * @return Returns 0 if p1 and p2 have the same distance to the target
     * instance, -1 if p1 are closer to the target instance, and returns 1 if p1
     * are closer to the target instance.
     */
    @Override
    public int compare(final Point p1, final Point p2) {
        final double d1 = p1.distance(this.target);
        final double d2 = p2.distance(this.target);
        return Double.compare(d1, d2);
    }

}

package br.ufu.facom.minas.core.datastructure;

import java.util.Arrays;
import java.util.List;

/**
 * Class representing a point.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class Point {

    private final double[] x;

    public Point(final double[] x) {
        this.x = x;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Point point = (Point) o;
        return Arrays.equals(x, point.x);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(x);
    }

    public static Point calculateCentroid(final List<? extends Point> points) {
        Point sum = points.get(0);
        if (points.size() > 1) {
            for (final Point point : points.subList(1, points.size())) {
                sum = Point.add(sum, point);
            }
        }
        return Point.divide(sum, points.size());
    }

    public static Point add(final Point p1, final Point p2) {
        final double[] result = new double[p1.getLength()];
        for (int i = 0; i < p1.getLength(); ++i) {
            result[i] = p1.get(i) + p2.get(i);
        }
        return new Point(result);
    }

    public static Point divide(final Point point, final double scalar) {
        final double[] result = new double[point.getLength()];
        for (int i = 0; i < point.getLength(); ++i) {
            result[i] = point.get(i) / scalar;
        }
        return new Point(result);
    }

    public double distance(final Point point) {
        double sum = 0;
        for (int i = 0; i < this.x.length; ++i) {
            sum += (this.x[i] - point.get(i)) * (this.x[i] - point.get(i));
        }
        return Math.sqrt(sum);
    }

    public Point calculateClosestPoint(final List<Point> points) {

        Point closest = null;
        double minDistance = Double.MAX_VALUE;
        for (final Point point : points) {
            final double distance = point.distance(this);
            if (distance < minDistance) {
                minDistance = distance;
                closest = point;
            }
        }
        return closest;
    }

    public int getLength() {
        return this.x.length;
    }

    public double get(final int i) {
        return x[i];
    }
}

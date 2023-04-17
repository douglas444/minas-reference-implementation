package br.ufu.facom.minas.core.datastructure;

import java.util.*;

/**
 * Class representing a micro-cluster.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class MicroCluster {

    private int timestamp;
    private String label;
    private Category category;
    private int n;
    private final double[] ls;
    private final double[] ss;
    private final Set<Integer> timestamps;

    public MicroCluster(final int timestamp,
                        final String label,
                        final Category category,
                        final int n,
                        final double[] ls,
                        final double[] ss) {

        this.timestamps = new HashSet<>();
        this.timestamp = timestamp;
        this.label = label;
        this.category = category;
        this.n = n;
        this.ls = ls;
        this.ss = ss;
    }

    public MicroCluster(final DataInstance instance) {

        final int dimensions = instance.getLength();

        this.timestamps = new HashSet<>();
        this.timestamp = 0;
        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        this.incrementAndUpdateTimestamp(instance);
    }

    public MicroCluster(final List<DataInstance> instances) {

        this.timestamps = new HashSet<>();
        this.timestamp = 0;
        final int dimensions = instances.get(0).getLength();

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        for (final DataInstance instance : instances) {
            incrementAndUpdateTimestamp(instance);
        }
    }

    public void updateTimestamp(final DataInstance instance) {
        this.timestamp = instance.getTimestamp();
    }

    public void incrementAndUpdateTimestamp(final DataInstance instance) {


        for (int i = 0; i < instance.getLength(); ++i) {
            this.ls[i] += instance.get(i);
            this.ss[i] += instance.get(i) * instance.get(i);
        }

        ++this.n;

        this.timestamps.add(instance.getTimestamp());
        this.updateTimestamp(instance);
    }

    public Point calculateCentroid() {

        final double[] x = this.ls.clone();
        for (int i = 0; i < x.length; ++i) {
            x[i] /= this.n;
        }

        return new Point(x);
    }

    public double calculateStandardDeviation() {

        double sum = 0;

        for (int i = 0; i < this.ss.length; ++i) {
            sum += (this.ss[i] / this.n) - Math.pow(this.ls[i] / this.n, 2);
        }

        return Math.sqrt(sum);

    }

    public double distance(final MicroCluster microCluster) {
        return this.calculateCentroid().distance(microCluster.calculateCentroid());
    }

    public double distance(final Point point) {
        return this.calculateCentroid().distance(point);
    }

    public MicroCluster calculateClosestMicroCluster(final List<MicroCluster> microClusters) {
        final Point centroid = this.calculateCentroid();
        return calculateClosestMicroCluster(centroid, microClusters);
    }

    public static MicroCluster calculateClosestMicroCluster(final Point point,
                                                            final List<MicroCluster> microClusters) {

        MicroCluster closest = null;
        double minDistance = Double.MAX_VALUE;
        for (final MicroCluster microCluster : microClusters) {
            final double distance = microCluster.distance(point);
            if (distance < minDistance) {
                minDistance = distance;
                closest = microCluster;
            }
        }
        return closest;
    }

    public static MicroCluster merge(final MicroCluster m1, final MicroCluster m2) {

        final int n = m1.n + m2.n;
        final double[] ss = m1.ss.clone();
        final double[] ls = m1.ls.clone();

        final int timestamp = Math.max(m1.timestamp, m2.timestamp);

        final String label = m1.label;
        final Category category = m1.getCategory();

        for (int i = 0; i < ss.length; ++i) {
            ss[i] += m2.ss[i];
            ls[i] += m2.ls[i];
        }

        return new MicroCluster(timestamp, label, category, n, ls, ss);
    }


    public static double calculateSilhouette(final MicroCluster microCluster,
                                             final List<MicroCluster> microClusters) {

        final Point centroid = microCluster.calculateCentroid();

        final List<Point> centroids = new ArrayList<>();
        for (final MicroCluster cluster : microClusters) {
            centroids.add(cluster.calculateCentroid());
        }
        Collections.sort(centroids, new PointDistanceComparator(centroid));

        final double a = microCluster.calculateStandardDeviation();
        final double b;
        if (centroids.size() > 0) {
            final Point closestCentroid = centroids.get(0);
            b = centroid.distance(closestCentroid);
        } else {
            b = Double.MAX_VALUE;
        }

        return (b - a) / Math.max(b, a);
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    public int getN() {
        return n;
    }

    public Set<Integer> getTimestamps() {
        return Collections.unmodifiableSet(timestamps);
    }

}

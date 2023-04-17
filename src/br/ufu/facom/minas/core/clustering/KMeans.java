package br.ufu.facom.minas.core.clustering;

import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.datastructure.MicroCluster;
import br.ufu.facom.minas.core.datastructure.Point;

import java.util.*;

/**
 * Implementation of the {@link ClusteringAlgorithm} interface for the KMeans
 * algorithm. The initial centroids are chosen deterministically by maximizing
 * the distance between each other.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class KMeans implements ClusteringAlgorithm {

    private final int k;

    public KMeans(final int k) {
        this.k = k;
    }

    @Override
    public List<MicroCluster> execute(final List<DataInstance> instances) {

        final List<List<DataInstance>> clusters = execute(instances, this.k);
        final List<MicroCluster> microClusters = new ArrayList<>(clusters.size());
        for (final List<DataInstance> cluster : clusters) {
            if (!cluster.isEmpty()) {
                final MicroCluster microCluster = new MicroCluster(cluster);
                microClusters.add(microCluster);
            }
        }
        return microClusters;
    }

    private static List<List<DataInstance>> execute(final List<DataInstance> instances, final int k) {

        final ArrayList<Point> centroids = chooseCentroids(instances, k);

        ArrayList<List<DataInstance>> clusters;
        ArrayList<Point> oldCentroids;

        do {

            clusters = groupByClosestCentroid(instances, centroids);
            oldCentroids = new ArrayList<>(centroids);

            for (int i = 0; i < clusters.size(); ++i) {
                final List<DataInstance> cluster = clusters.get(i);
                if (!cluster.isEmpty()) {
                    final Point centroid = Point.calculateCentroid(cluster);
                    centroids.set(i, centroid);
                }
            }
        } while(!oldCentroids.containsAll(centroids));

        return clusters;
    }

    private static ArrayList<Point> chooseCentroids(final List<DataInstance> instances, final int k) {

        final ArrayList<Point> centroids = new ArrayList<>(k);
        for (int i = 0; i < k; ++i) {
            final Point centroid = selectNextCentroid(new ArrayList<>(instances), centroids);
            centroids.add(centroid);
        }

        return centroids;
    }

    private static Point selectNextCentroid(final ArrayList<DataInstance> instances,
                                            final List<Point> centroids) {

        DataInstance selected = instances.get(0);
        double maxDistance = 0;
        for (final DataInstance instance : instances.subList(1, instances.size())) {
            final double distance = KMeans.distanceToTheClosestCentroid(instance, centroids);
            if (distance > maxDistance) {
                selected = instance;
                maxDistance = distance;
            }
        }

        return selected;
    }

    private static ArrayList<List<DataInstance>> groupByClosestCentroid(final List<DataInstance> instances,
                                                                        final ArrayList<Point> centroids) {

        final ArrayList<List<DataInstance>> centroidsInstances = new ArrayList<>(centroids.size());

        for (int i = 0; i < centroids.size(); ++i) {
            centroidsInstances.add(i, new ArrayList<DataInstance>());
        }

        for (final DataInstance instance : instances) {

            final Point closestCentroid = instance.calculateClosestPoint(centroids);

            for (int j = 0; j < centroids.size(); ++j) {
                if (closestCentroid.equals(centroids.get(j))) {
                    centroidsInstances.get(j).add(instance);
                }
            }
        }

        return centroidsInstances;
    }

    private static double distanceToTheClosestCentroid(final DataInstance instance, final List<Point> centroids) {
        if (centroids.isEmpty()) {
            return 0.0;
        }
        final Point closestCentroid = instance.calculateClosestPoint(centroids);
        return instance.distance(closestCentroid);
    }

}

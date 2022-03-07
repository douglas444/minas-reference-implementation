package br.ufu.facom.minas.core.clustering;

import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.datastructure.MicroCluster;
import br.ufu.facom.minas.core.datastructure.Point;

import java.util.*;

/**
 * Implementation of the {@link ClusteringAlgorithm} interface for the KMeans++
 * algorithm.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class KMeansPlusPlus implements ClusteringAlgorithm {

    private final int k;
    private final Random random;

    public KMeansPlusPlus(final int k, final Random random) {
        this.k = k;
        this.random = random;
    }

    @Override
    public List<MicroCluster> execute(final List<DataInstance> instances) {

        final List<List<DataInstance>> clusters = execute(instances, this.k, this.random);
        final List<MicroCluster> microClusters = new ArrayList<>(clusters.size());
        for (final List<DataInstance> cluster : clusters) {
            if (!cluster.isEmpty()) {
                final MicroCluster microCluster = new MicroCluster(cluster);
                microClusters.add(microCluster);
            }
        }
        return microClusters;
    }

    private static List<List<DataInstance>> execute(final List<DataInstance> instances,
                                                    final int k,
                                                    final Random random) {

        final ArrayList<Point> centroids = chooseCentroids(instances, k, random);

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

    private static ArrayList<Point> chooseCentroids(final List<DataInstance> instances,
                                                    final int k,
                                                    final Random random) {

        final ArrayList<Point> centroids = new ArrayList<>(k);
        for (int i = 0; i < k; ++i) {
            final Point centroid = selectNextCentroid(new ArrayList<>(instances), centroids, random);
            centroids.add(centroid);
        }

        return centroids;
    }

    private static Point selectNextCentroid(final ArrayList<DataInstance> instances,
                                            final List<Point> centroids,
                                            final Random random) {

        final ArrayList<Double> probabilities = calculateProbabilities(instances, centroids);

        double cumulativeProbability = 0;
        DataInstance selected = null;
        final double r = random.nextDouble();

        int i = 0;
        while (selected == null) {
            final double probability = probabilities.get(i);
            cumulativeProbability += probability;
            if (r <= cumulativeProbability || i == instances.size() - 1) {
                selected = instances.get(i);
            }
            ++i;
        }

        return selected;
    }

    private static ArrayList<Double> calculateProbabilities(final ArrayList<DataInstance> instances,
                                                            final List<Point> centroids) {

        final ArrayList<Double> probabilities = new ArrayList<>(instances.size());
        for (int i = 0; i < instances.size(); ++i) {
            final double distance = KMeansPlusPlus.distanceToTheClosestCentroid(instances.get(i), centroids);
            probabilities.add(i, Math.pow(distance, 2));
        }

        double sum = 0.0;
        for (final double probability : probabilities) {
            sum += probability;
        }
        if (sum > 0) {
            for (int i = 0; i < probabilities.size(); ++i) {
                final double probability = probabilities.get(i);
                probabilities.set(i, probability / sum);
            }
        }

        return probabilities;
    }


    private static ArrayList<List<DataInstance>> groupByClosestCentroid(final List<DataInstance> instances,
                                                                        final ArrayList<Point> centroids) {

        final ArrayList<List<DataInstance>> centroidsInstances = new ArrayList<>(centroids.size());

        for (int i = 0; i < centroids.size(); ++i) {
            centroidsInstances.add(i, new LinkedList<DataInstance>());
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

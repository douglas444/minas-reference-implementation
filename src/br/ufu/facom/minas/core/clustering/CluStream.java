package br.ufu.facom.minas.core.clustering;

import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.datastructure.MicroCluster;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Implementation of the {@link ClusteringAlgorithm} interface for the
 * CluStream algorithm.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class CluStream implements ClusteringAlgorithm {

    private final int trainingDataSize;
    private final int bufferSize;
    private final Random random;

    public CluStream(final int trainingDataSize, final int bufferSize, final Random random) {
        this.random = random;
        this.trainingDataSize = trainingDataSize;
        this.bufferSize = bufferSize;
    }

    @Override
    public List<MicroCluster> execute(final List<DataInstance> instances) {

        if (instances.size() <= this.trainingDataSize) {
            return CluStream.buildBuffer(instances, Math.min(instances.size(), this.bufferSize), this.random);
        }

        final List<DataInstance> offlineData = new LinkedList<>(
                instances.subList(0, this.trainingDataSize));

        final List<DataInstance> onlineData = new LinkedList<>(
                instances.subList(this.trainingDataSize, instances.size()));

        final List<MicroCluster> buffer = CluStream.buildBuffer(offlineData,
                Math.min(instances.size(), this.bufferSize), this.random);

        for (final DataInstance instance : onlineData) {
            process(instance, buffer);
        }

        return buffer;
    }

    private static List<MicroCluster> buildBuffer(final List<DataInstance> instances,
                                                  final int bufferMaxSize,
                                                  final Random random) {

        final KMeansPlusPlus kMeans = new KMeansPlusPlus(bufferMaxSize, random);
        return kMeans.execute(instances);
    }

    private static void process(final DataInstance instance, final List<MicroCluster> buffer) {


        final MicroCluster closestMicroCluster = MicroCluster.calculateClosestMicroCluster(instance, buffer);

        final double distance = instance.distance(closestMicroCluster.calculateCentroid());
        final double radius;

        if (closestMicroCluster.getN() > 1) {
            radius = closestMicroCluster.calculateStandardDeviation() * 2;
        } else {

            final List<MicroCluster> bufferSubSet = new LinkedList<>(buffer);
            bufferSubSet.remove(closestMicroCluster);

            radius = MicroCluster
                    .calculateClosestMicroCluster(closestMicroCluster.calculateCentroid(), bufferSubSet)
                    .distance(closestMicroCluster);
        }

        if (distance < radius) {
            closestMicroCluster.incrementAndUpdateTimestamp(instance);
        } else {
            final MicroCluster microCluster = new MicroCluster(instance);
            addMicroCluster(microCluster, buffer);
        }

    }

    private static void addMicroCluster(final MicroCluster microCluster,
                                        final List<MicroCluster> microClustersBuffer) {

        MicroCluster m1 = null;
        MicroCluster m2 = null;

        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < microClustersBuffer.size(); i++) {

            final MicroCluster a = microClustersBuffer.get(i);

            for (int j = i + 1; j < microClustersBuffer.size(); j++) {

                final MicroCluster b = microClustersBuffer.get(j);

                final double distance = a.distance(b);
                if (distance < minDistance) {
                    minDistance = distance;
                    m1 = a;
                    m2 = b;
                }
            }
        }

        if (m1 != null) {
            microClustersBuffer.remove(m1);
            microClustersBuffer.remove(m2);
            microClustersBuffer.add(MicroCluster.merge(m1, m2));
        }

        microClustersBuffer.add(microCluster);

    }

}

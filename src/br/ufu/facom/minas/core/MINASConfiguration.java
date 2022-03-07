package br.ufu.facom.minas.core;

import br.ufu.facom.minas.core.clustering.ClusteringAlgorithm;
import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.decisionrule.datainstance.DataInstanceDecisionRule;
import br.ufu.facom.minas.core.decisionrule.microcluster.MicroClusterDecisionRule;

/**
 * This class wraps all the MINAS' parameters required in the
 * {@link MINAS#process(DataInstance, MINASModel, MINASConfiguration)} method.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class MINASConfiguration {

    private final ClusteringAlgorithm clusteringForInitialization;
    private final ClusteringAlgorithm clusteringForNoveltyDetection;
    private final MicroClusterDecisionRule microClusterDecisionRule;
    private final DataInstanceDecisionRule dataInstanceDecisionRule;
    private final int temporaryMemoryMaxSize;
    private final int minimumClusterSize;
    private final int windowSize;
    private final int microClusterLifespan;
    private final int instanceLifespan;
    private final boolean isIncremental;

    public MINASConfiguration(final ClusteringAlgorithm clusteringForInitialization,
                              final ClusteringAlgorithm clusteringForNoveltyDetection,
                              final MicroClusterDecisionRule microClusterDecisionRule,
                              final DataInstanceDecisionRule dataInstanceDecisionRule,
                              final int temporaryMemoryMaxSize,
                              final int minimumClusterSize,
                              final int windowSize,
                              final int microClusterLifespan,
                              final int instanceLifespan,
                              final boolean isIncremental) {

        this.clusteringForInitialization = clusteringForInitialization;
        this.clusteringForNoveltyDetection = clusteringForNoveltyDetection;
        this.microClusterDecisionRule = microClusterDecisionRule;
        this.dataInstanceDecisionRule = dataInstanceDecisionRule;
        this.temporaryMemoryMaxSize = temporaryMemoryMaxSize;
        this.minimumClusterSize = minimumClusterSize;
        this.windowSize = windowSize;
        this.microClusterLifespan = microClusterLifespan;
        this.instanceLifespan = instanceLifespan;
        this.isIncremental = isIncremental;

    }

    public ClusteringAlgorithm getClusteringForInitialization() {
        return clusteringForInitialization;
    }

    public ClusteringAlgorithm getClusteringForNoveltyDetection() {
        return clusteringForNoveltyDetection;
    }

    public MicroClusterDecisionRule getMicroClusterDecisionRule() {
        return microClusterDecisionRule;
    }

    public DataInstanceDecisionRule getDataInstanceDecisionRule() {
        return dataInstanceDecisionRule;
    }

    public int getTemporaryMemoryMaxSize() {
        return temporaryMemoryMaxSize;
    }

    public int getMinimumClusterSize() {
        return minimumClusterSize;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getMicroClusterLifespan() {
        return microClusterLifespan;
    }

    public int getInstanceLifespan() {
        return instanceLifespan;
    }

    public boolean isIncremental() {
        return isIncremental;
    }
}

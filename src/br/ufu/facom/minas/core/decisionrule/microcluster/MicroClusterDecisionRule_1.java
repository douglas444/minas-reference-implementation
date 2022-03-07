package br.ufu.facom.minas.core.decisionrule.microcluster;

import br.ufu.facom.minas.core.decisionrule.Classification;
import br.ufu.facom.minas.core.datastructure.MicroCluster;

import java.util.List;

/**
 * Implementation of the {@link MicroClusterDecisionRule} interface that uses
 * the <a href="https://doi.org/10.1007/s10618-015-0433-y">TV1</a> strategy to
 * calculate the threshold value for micro-cluster classification.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class MicroClusterDecisionRule_1 implements MicroClusterDecisionRule {

    private final double factor;

    public MicroClusterDecisionRule_1(final double factor) {
        this.factor = factor;
    }

    @Override
    public Classification classify(final MicroCluster target, final List<MicroCluster> microClusters) {

        if (microClusters.isEmpty()) {
            return new Classification(null, false);
        }

        final MicroCluster closestMicroCluster = target.calculateClosestMicroCluster(microClusters);
        final double distance = closestMicroCluster.distance(target);
        final double threshold = closestMicroCluster.calculateStandardDeviation() * this.factor;

        if (distance < threshold) {
            return new Classification(closestMicroCluster, true);
        } else {
            return new Classification(closestMicroCluster, false);
        }
    }
}

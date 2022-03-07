package br.ufu.facom.minas.core.decisionrule.microcluster;

import br.ufu.facom.minas.core.decisionrule.Classification;
import br.ufu.facom.minas.core.datastructure.MicroCluster;

import java.util.List;

/**
 * Implementation of the {@link MicroClusterDecisionRule} interface that sets
 * the threshold value for micro-cluster classification as the sum of the
 * standard deviation of the target micro-cluster and the micro-cluster that is
 * closer to it.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class MicroClusterDecisionRule_4 implements MicroClusterDecisionRule {

    @Override
    public Classification classify(final MicroCluster target, final List<MicroCluster> microClusters) {

        if (microClusters.isEmpty()) {
            return new Classification(null, false);
        }

        final MicroCluster closestMicroCluster = target.calculateClosestMicroCluster(microClusters);
        final double distance = closestMicroCluster.distance(target);
        final double threshold = closestMicroCluster.calculateStandardDeviation() + target.calculateStandardDeviation();

        if (distance < threshold) {
            return new Classification(closestMicroCluster, true);
        } else {
            return new Classification(closestMicroCluster, false);
        }
    }
}

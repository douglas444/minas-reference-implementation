package br.ufu.facom.minas.core.decisionrule.datainstance;

import br.ufu.facom.minas.core.decisionrule.Classification;
import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.datastructure.MicroCluster;

import java.util.List;

/**
 * Implementation of the {@link DataInstanceDecisionRule} interface that uses
 * the <a href="https://doi.org/10.1007/s10618-015-0433-y">paper</a>'s
 * default strategy to calculate the threshold value for data instance
 * classification.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class DataInstanceDecisionRule_1 implements DataInstanceDecisionRule {

    private final double factor;

    public DataInstanceDecisionRule_1(final double factor) {
        this.factor = factor;
    }

    @Override
    public Classification classify(final DataInstance target, final List<MicroCluster> microClusters) {

        if (microClusters.isEmpty()) {
            return new Classification(null, false);
        }

        final MicroCluster closestMicroCluster = MicroCluster.calculateClosestMicroCluster(target, microClusters);
        final double distance = target.distance(closestMicroCluster.calculateCentroid());

        if (distance <= closestMicroCluster.calculateStandardDeviation() * this.factor) {
            return new Classification(closestMicroCluster, true);
        }
        return new Classification(closestMicroCluster, false);
    }
}
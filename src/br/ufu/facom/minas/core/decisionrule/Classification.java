package br.ufu.facom.minas.core.decisionrule;

import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.datastructure.MicroCluster;
import br.ufu.facom.minas.core.decisionrule.datainstance.DataInstanceDecisionRule;
import br.ufu.facom.minas.core.decisionrule.microcluster.MicroClusterDecisionRule;

import java.util.List;

/**
 * This class represents the classification result of the
 * {@link DataInstanceDecisionRule#classify(DataInstance, List)} and
 * {@link MicroClusterDecisionRule#classify(MicroCluster, List)} methods.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class Classification {

    private final MicroCluster closest;
    private final boolean explained;

    public Classification(final MicroCluster closest, final boolean explained) {

        this.closest = closest;
        this.explained = explained;
    }

    public MicroCluster getClosestMicroCluster() {
        return closest;
    }

    public boolean isExplained() {
        return explained;
    }
}

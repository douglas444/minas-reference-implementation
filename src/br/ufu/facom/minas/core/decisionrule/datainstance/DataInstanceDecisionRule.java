package br.ufu.facom.minas.core.decisionrule.datainstance;

import br.ufu.facom.minas.core.decisionrule.Classification;
import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.datastructure.MicroCluster;

import java.util.List;

/**
 * This interface represents the behavior of a decision rule used by
 * MINAS to classify a data instance.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public interface DataInstanceDecisionRule {
    Classification classify(final DataInstance target, final List<MicroCluster> microClusters);
}

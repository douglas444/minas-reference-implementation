package br.ufu.facom.minas.core.decisionrule.microcluster;

import br.ufu.facom.minas.core.decisionrule.Classification;
import br.ufu.facom.minas.core.datastructure.MicroCluster;

import java.util.List;

/**
 * This interface represents the behavior of a decision rule used by
 * MINAS to classify a micro-cluster.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public interface MicroClusterDecisionRule {
    Classification classify(final MicroCluster target, final List<MicroCluster> microClusters);
}

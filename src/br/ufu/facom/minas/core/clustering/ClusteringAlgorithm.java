package br.ufu.facom.minas.core.clustering;

import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.datastructure.MicroCluster;

import java.util.List;

/**
 * This interface represents the behavior of a clustering algorithm used by
 * MINAS both in the offline and online phase.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public interface ClusteringAlgorithm {
    List<MicroCluster> execute(final List<DataInstance> instances);
}

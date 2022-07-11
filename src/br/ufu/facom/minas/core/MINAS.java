package br.ufu.facom.minas.core;

import br.ufu.facom.minas.core.clustering.ClusteringAlgorithm;
import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.datastructure.*;
import br.ufu.facom.minas.core.decisionrule.Classification;
import br.ufu.facom.minas.core.decisionrule.datainstance.DataInstanceDecisionRule;
import br.ufu.facom.minas.core.decisionrule.microcluster.MicroClusterDecisionRule;

import java.util.*;

/**
 * This class contain the main methods required to initialize and execute the
 * <a href="https://doi.org/10.1007/s10618-015-0433-y">MINAS framework</a>.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class MINAS {

    /**
     * Initializes and returns a new {@link MINASModel}, ready to be used
     * online. This method corresponds to the Algorithm 1 from the
     * <a href="https://doi.org/10.1007/s10618-015-0433-y">paper</a>.
     *
     * @param trainingSet the list of data instances used to initialize
     *                    the model. The labels of the data instances
     *                    passed as argument are accessed through the
     *                    {@link DataInstance#getLabel() getLabel}
     *                    method.
     * @param config the MINAS configuration to be used.
     * @return a MINAS model ready to be used online.
     */
    public static MINASModel initializeModel(final List<DataInstance> trainingSet,
                                             final MINASConfiguration config) {

        // Sorts the instances by timestamp (ascending)
        Collections.sort(trainingSet, new Comparator<DataInstance>() {
            @Override
            public int compare(final DataInstance o1, final DataInstance o2) {
                return Integer.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });

        // Builds a set containing all the known labels
        final Set<String> knownLabels = new HashSet<>();
        for (final DataInstance labelledInstance : trainingSet) {
            final String label = labelledInstance.getLabel();
            knownLabels.add(label);
        }

        final List<MicroCluster> decisionModel = new LinkedList<>();

        // For each label...
        for (final String label : knownLabels) {

            // Builds a list contaning only instance of the label.
            final List<DataInstance> instances = new LinkedList<>();
            for (final DataInstance dataInstance : trainingSet) {
                if (label.equals(dataInstance.getLabel())) {
                    instances.add(dataInstance);
                }
            }

            // Applies the clustering algorithm to an unmodifiable copy of the
            // list. The list is made unmodifiable so that no instance is
            // added or removed by the clustering algorithm implementation.
            final List<MicroCluster> microClusters = config.getClusteringForInitialization()
                    .execute(Collections.unmodifiableList(instances));

            // Labels the resulting micro-clusters with the label respective to
            // the list.
            for (final MicroCluster microCluster : microClusters) {
                microCluster.setLabel(label);
                microCluster.setCategory(Category.KNOWN);
            }

            // Adds to the decision model the micro-clusters that meet the
            // required condition.
            for (final MicroCluster microCluster : microClusters) {
                if (microCluster.getN() >= 3) {
                    decisionModel.add(microCluster);
                }
            }
        }

        // Initializes the confusion matrix
        final DynamicConfusionMatrix confusionMatrix = new DynamicConfusionMatrix(knownLabels);

        // Instantiates and returns the model
        return new MINASModel(decisionModel, confusionMatrix);
    }

    /**
     * Tries to classify a single data instance, and if the result is
     * successful, returns a list containing the labelling mapping the data
     * instance's timestamp to the predicted label. However, if the
     * classification fails, the data instance is added to the {@code model}'s
     * temporary memory and its classification is delayed. This method
     * corresponds to the Algorithm 2 from the
     * <a href="https://doi.org/10.1007/s10618-015-0433-y">paper</a>.
     *
     * <p>If during the execution of this method the {@code model}'s temporary
     * memory reaches its maximum size, the novelty detection procedure is
     * executed and when this happens, labellings for delayed classifications
     * may be added to the return list.
     *
     * @param instance the instance to be classified. Its label is expected to
     *                 be unknown; however, in order to update the confusion
     *                 matrix, the true label of the data instance must be
     *                 accessible through the
     *                 {@link DataInstance#getLabel() getLabel} method.
     * @param model the model used to process the instance.
     * @param config the MINAS configuration to be used.
     * @return a list that may include delayed classification labellings and/or
     * the labelling for the instance passed as argument. The list will be empty
     * if the model fails at classifying the data instance and no pattern is
     * detected by the novelty detection procedure.
     */
    public static List<Labelling> process(final DataInstance instance,
                                          final MINASModel model,
                                          final MINASConfiguration config) {

        // Updates the model last seen timestamp.
        model.setLastTimestamp(instance.getTimestamp());

        // Classifies the data instance using the configured decision rule and
        // the decision model.
        final DataInstanceDecisionRule decisionRule = config.getDataInstanceDecisionRule();
        final Classification classification = decisionRule.classify(instance, model.getDecisionModel());

        final List<Labelling> labellings = new LinkedList<>();

        // If one of the model's micro-clusters manages to explain the data
        // instance, the referred micro-cluster is updated and a labelling
        // mapping the current timestamp to the micro-cluster's label is added
        // to the list that will be returned.
        if (classification.isExplained()) {

            // If the configuration states so, the micro-cluster that explained
            // the data instance is incremented. Otherwise, only its timestamp
            // is updated.
            if (config.isIncremental()) {
                classification.getClosestMicroCluster().incrementAndUpdateTimestamp(instance);
            } else {
                classification.getClosestMicroCluster().updateTimestamp(instance);
            }

            final Labelling labelling = new Labelling(
                    instance.getTimestamp(),
                    classification.getClosestMicroCluster().getLabel(),
                    classification.getClosestMicroCluster().getCategory().equals(Category.NOVELTY));

            // Adds to the list that will be returned at the end of this method a
            // labelling mapping the current timestamp to the micro-cluster's
            // label.
            labellings.add(labelling);

        } else {

            // If none of the model's micro-cluster managed to explain the data
            // instance, the data instance is added to the model's temporary
            // memory.
            model.getTemporaryMemory().add(instance);

            // If the model's temporary memory has reached its max size, the
            // novelty detection procedure is called and any resultant delayed
            // classifications labellings are added to the list that will be
            // returned.
            if (model.getTemporaryMemory().size() >= config.getTemporaryMemoryMaxSize()) {
                labellings.addAll(detectNoveltyAndUpdate(model, config));
            }

        }

        // If a window has being completed, inactive micro-cluster will be
        // removed from the decision model and added to the sleep memory.
        // Beyond that, instances that have being in the temporary memory for
        // too long will be removed.
        if (model.getLastTimestamp() % config.getWindowSize() == 0) {

            // Searches for inactive micro-clusters inside the decision model.
            final List<MicroCluster> inactiveMicroClusters = new LinkedList<>();
            for (final MicroCluster microCluster : model.getDecisionModel()) {
                final long microClusterAge = model.getLastTimestamp() - microCluster.getTimestamp();
                if (microClusterAge > config.getMicroClusterLifespan()) {
                    inactiveMicroClusters.add(microCluster);
                }
            }

            // Removes from the decision model all the inactive
            // micro-clusters.
            model.getDecisionModel().removeAll(inactiveMicroClusters);
            model.getSleepMemory().addAll(inactiveMicroClusters);

            // Searches for inactive data instances inside the temporary
            // memory.
            final List<DataInstance> instancesToBeRemoved = new LinkedList<>();
            for (final DataInstance dataInstance : model.getTemporaryMemory()) {
                final long instanceAge = model.getLastTimestamp() - dataInstance.getTimestamp();
                if (instanceAge > config.getInstanceLifespan()) {
                    instancesToBeRemoved.add(dataInstance);
                }
            }

            // Removes from the temporary memory all the inactive data
            // instances.
            model.getTemporaryMemory().removeAll(instancesToBeRemoved);
        }

        // Updates the confusion matrix.
        if (classification.isExplained()) {
            final boolean isNovel = classification.getClosestMicroCluster().getCategory() == Category.NOVELTY;
            model.getConfusionMatrix().addPrediction(instance, classification.getClosestMicroCluster().getLabel(), isNovel);
        } else {
            model.getConfusionMatrix().addUnknown(instance);
        }

        // Return the list of labellings. It may be empty if the data instance
        // wasn't explained by the decision model and if no new micro-cluster
        // was generated by the novelty detection procedure.
        return labellings;

    }

    /**
     * Detects patterns inside the {@code model}'s temporary memory. This
     * method corresponds to the Algorithm 3 from the
     * <a href="https://doi.org/10.1007/s10618-015-0433-y">paper</a>.
     *
     * <p>For each pattern detected, a micro-cluster will be generated,
     * classified and added to the {@code model}'s decision model.
     *
     * <p>If one or more patterns are detected, this method will return a list
     * containing the delayed classification labellings corresponding to the
     * data instances composing those patterns. If no pattern is detected, an
     * empty list will be returned.
     *
     * @param model the model over which the novelty detection procedure will
     *              be applied.
     * @param config the MINAS configuration to be used.
     * @return the list of delayed classification labellings of the instances
     * composing the patterns detected. If no pattern is detected, an empty
     * list will be returned.
     */
    private static List<Labelling> detectNoveltyAndUpdate(final MINASModel model,
                                                          final MINASConfiguration config) {

        // Applies to the model's temporary memory the clustering algorithm
        // configured.
        final ClusteringAlgorithm clusteringAlgorithm = config.getClusteringForNoveltyDetection();
        final List<MicroCluster> microClusters = clusteringAlgorithm.execute(model.getTemporaryMemory());

        // Searches for micro-clusters that do not meet the required criteria
        // to be declared as a pattern.
        final List<MicroCluster> microClustersToBeRemoved = new LinkedList<>();
        for (final MicroCluster microCluster : microClusters) {
            final double silhouette = MicroCluster.calculateSilhouette(microCluster, model.getDecisionModel());
            if (microCluster.getN() < config.getMinimumClusterSize() || silhouette <= 0) {
                microClustersToBeRemoved.add(microCluster);
            }
        }

        // Removes the micro-clusters that do not meet the required criteria
        // to be declared as a pattern.
        microClusters.removeAll(microClustersToBeRemoved);

        final List<Labelling> labellings = new LinkedList<>();

        // Tries to classify each micro-cluster, first using the decision
        // model, and if the decision model fails to explain the micro-cluster,
        // tries to classify it using the sleep memory. If the micro-cluster is
        // explained by the decision model or sleep memory, it is declared an
        // extension, otherwise, a novelty. Finally, the micro-cluster is added
        // to the decision model and the respective instances are removed from
        // the temporary memory.
        for (final MicroCluster microCluster : microClusters) {

            // Classifies the micro-cluster using the configured decision rule
            // and the decision model.
            final MicroClusterDecisionRule decisionRule = config.getMicroClusterDecisionRule();
            Classification classification = decisionRule.classify(microCluster, model.getDecisionModel());

            // If the micro-cluster is explained by the decision model, it is
            // declared an extension.
            if (classification.isExplained()) {

                microCluster.setCategory(classification.getClosestMicroCluster().getCategory());
                microCluster.setLabel(classification.getClosestMicroCluster().getLabel());

            } else {

                // If the micro-cluster is not explained by the decision model,
                // it is classified using the configured decision rule and the
                // sleep memory.
                classification = decisionRule.classify(microCluster, model.getSleepMemory());

                // If the micro-cluster is explained by the sleep memory, it is
                // declared an extension.
                if (classification.isExplained()) {

                    microCluster.setCategory(classification.getClosestMicroCluster().getCategory());
                    microCluster.setLabel(classification.getClosestMicroCluster().getLabel());
                    model.getSleepMemory().remove(classification.getClosestMicroCluster());
                    model.getDecisionModel().add(classification.getClosestMicroCluster());

                } else {

                    // If the micro-cluster is not explained by the sleep
                    // memory, it is declared a novelty.
                    microCluster.setCategory(Category.NOVELTY);
                    microCluster.setLabel(String.valueOf(model.getNoveltyCount()));
                    model.setNoveltyCount(model.getNoveltyCount() + 1);

                }
            }

            // Adds to the decision model.
            model.getDecisionModel().add(microCluster);

            // Searches for the instances respective to micro-cluster.
            final List<DataInstance> instances = new LinkedList<>();
            for (final DataInstance instance : model.getTemporaryMemory()) {
                if (microCluster.getTimestamps().contains(instance.getTimestamp())) {
                    instances.add(instance);
                }
            }

            // Removes from the temporary memory all the instances respective
            // to the micro-cluster.
            model.getTemporaryMemory().removeAll(instances);

            // For each of the instances respective to the micro-cluster, adds
            // to the return list a labelling mapping the instance's timestamp
            // to the micro-cluster label.
            for (final DataInstance instance : instances) {

                final boolean isNovel = microCluster.getCategory() == Category.NOVELTY;

                // Updates the confusion matrix.
                model.getConfusionMatrix().updatedDelayed(instance, microCluster.getLabel(), isNovel);

                final Labelling labelling = new Labelling(
                        instance.getTimestamp(),
                        microCluster.getLabel(),
                        microCluster.getCategory().equals(Category.NOVELTY));

                labellings.add(labelling);
            }
        }

        return labellings;
    }
}

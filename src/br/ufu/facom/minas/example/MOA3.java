package br.ufu.facom.minas.example;

import br.ufu.facom.minas.core.DatasetFileReader;
import br.ufu.facom.minas.core.MINAS;
import br.ufu.facom.minas.core.MINASConfiguration;
import br.ufu.facom.minas.core.MINASModel;
import br.ufu.facom.minas.core.clustering.CluStream;
import br.ufu.facom.minas.core.clustering.ClusteringAlgorithm;
import br.ufu.facom.minas.core.clustering.KMeans;
import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.datastructure.Labelling;
import br.ufu.facom.minas.core.decisionrule.datainstance.DataInstanceDecisionRule;
import br.ufu.facom.minas.core.decisionrule.datainstance.DataInstanceDecisionRule_1;
import br.ufu.facom.minas.core.decisionrule.microcluster.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Example of how to execute MINAS and how to print the results in external
 * files. In this example, MINAS is applied to the MOA3 dataset.
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class MOA3 {

    public static final String DATASET_COLUMN_SEPARATOR = ",";
    public static final String[] DATASET = new String[]{"./datasets/MOA3.csv"};
    public static final int TRAINING_DATA_SIZE = 10000;
    public static final int CLU_STREAM_INITIAL_DATA_SIZE = 1000;
    public static final int CLU_STREAM_BUFFER_MAX_SIZE = 100;
    public static final int K_MEANS_K = 100;
    public static final double DECISION_RULE_FACTOR = 2;
    public static final int TEMPORARY_MEMORY_MAX_SIZE = 2000;
    public static final int MINIMUM_CLUSTER_SIZE = 20;
    public static final int WINDOW_SIZE = 4000;
    public static final int MICRO_CLUSTER_LIFESPAN = 4000;
    public static final int INSTANCE_LIFESPAN = 4000;
    public static final boolean IS_INCREMENTAL = false;

    static final String OUTPUT_FILE_STATISTICS = "./output_statistics_.csv";
    static final String OUTPUT_FILE_PREDICTIONS = "./output_predictions_.csv";
    static final String OUTPUT_FILE_FINAL_CONFUSION_MATRIX = "./output_final_confusion_matrix.csv";

    public static void main(final String[] args) throws Exception {

        // Initializes a file reader for the dataset.
        final DatasetFileReader datasetFileReader = new DatasetFileReader(DATASET_COLUMN_SEPARATOR, DATASET);

        // Reads the data instances that are going to be used in the offline phase.
        final List<DataInstance> trainingInstances = datasetFileReader.getBatch(TRAINING_DATA_SIZE);

        // Configures the clustering algorithm for the offline phase.
        final ClusteringAlgorithm clusteringForInitialization = new CluStream(CLU_STREAM_INITIAL_DATA_SIZE, CLU_STREAM_BUFFER_MAX_SIZE);

        // Configures the clustering algorithm for the online phase.
        final ClusteringAlgorithm clusteringForNoveltyDetection = new KMeans(K_MEANS_K);

        // Configures the decision rule for micro-cluster classification.
        final MicroClusterDecisionRule microClusterDecisionRule = new MicroClusterDecisionRule_4();

        // Configures the decision rule for data instance classification.
        final DataInstanceDecisionRule dataInstanceDecisionRule = new DataInstanceDecisionRule_1(DECISION_RULE_FACTOR);

        // Define the parameters that will be used by MINAS.
        final MINASConfiguration config = new MINASConfiguration(
                clusteringForInitialization,
                clusteringForNoveltyDetection,
                microClusterDecisionRule,
                dataInstanceDecisionRule,
                TEMPORARY_MEMORY_MAX_SIZE,
                MINIMUM_CLUSTER_SIZE,
                WINDOW_SIZE,
                MICRO_CLUSTER_LIFESPAN,
                INSTANCE_LIFESPAN,
                IS_INCREMENTAL);

        System.out.println("Training...");

        // Executes the offline phase, initializing the model.
        final MINASModel model = MINAS.initializeModel(trainingInstances, config);

        // Opens the file where the statistics will be printed.
        final FileWriter statisticsWriter = new FileWriter(OUTPUT_FILE_STATISTICS);
        final BufferedWriter statisticsBw = new BufferedWriter(statisticsWriter);

        // Writes the header of the statistics file
        statisticsBw.write("timestamp;novelty_count;unkr;cer\n");

        // This list is where the predictions will be stored.
        final List<Labelling> labellings = new LinkedList<>();

        // Reads the first instance for the online phase
        DataInstance instance = datasetFileReader.getNext();

        // Loop for the online phase
        while (instance != null) {

            // Executes MINAS' online processing algorithm for the instance.
            final List<Labelling> results = MINAS.process(instance, model, config);

            // Stores the resultant predictions.
            labellings.addAll(results);

            // Reads next instance.
            instance = datasetFileReader.getNext();

            // Gets statistics at the current timestamp.
            final String output = model.getLastTimestamp()
                    + ";" + model.getNoveltyCount()
                    + ";" + model.getConfusionMatrix().measureUnkR()
                    + ";" + model.getConfusionMatrix().measureCER() + "\n";

            // Writes the statistics to the file.
            statisticsBw.write(output);

            // Prints the timestamp in the console.
            System.out.println("Timestamp = " + model.getLastTimestamp());
        }

        // Prints the final statistics in the console.
        System.out.println("Timestamp = " + model.getLastTimestamp()
                + "; Novelty count = " + model.getNoveltyCount()
                + "; UnkR = " + model.getConfusionMatrix().measureUnkR()
                + "; CER = " + model.getConfusionMatrix().measureCER() + ";");

        // Closes the statistics file and also the dataset file reader.
        statisticsBw.close();
        datasetFileReader.close();

        // Create an array where the predictions are stored ordered by the
        // respective timestamp.
        final String[] labels = new String[(int) model.getLastTimestamp()];
        for (final Labelling labelling : labellings) {
            labels[(int) labelling.getTimestamp() - 1] = labelling.getLabel();
        }

        // Open the file where the predictions will be printed.
        final FileWriter labellingsWriter = new FileWriter(OUTPUT_FILE_PREDICTIONS);
        final BufferedWriter labellingsBw = new BufferedWriter(labellingsWriter);

        // Writes the header of the predictions file
        labellingsBw.write("timestamp;label\n");

        // For each timestamp
        for (int i = 0; i < model.getLastTimestamp(); ++i) {

            // If there is a label for the timestamp, gets it, otherwise, set
            // the label as an empty string.
            final String label = labels[i] != null ? labels[i] : "";

            // Writes the timestamp and the label to the file;
            labellingsBw.write((i + 1) + ";" + label + "\n");
        }

        // Closes the predictions file.
        labellingsBw.close();

        // Writes the final confusion matrix to a file.
        final FileWriter finalConfusionMatrixWriter = new FileWriter(OUTPUT_FILE_FINAL_CONFUSION_MATRIX);
        final BufferedWriter finalConfusionMatrixBw = new BufferedWriter(finalConfusionMatrixWriter);
        finalConfusionMatrixBw.write(model.getConfusionMatrix().toString());
        finalConfusionMatrixBw.close();

    }
}

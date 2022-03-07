package br.ufu.facom.minas.example;

import br.ufu.facom.minas.core.DatasetFileReader;
import br.ufu.facom.minas.core.MINAS;
import br.ufu.facom.minas.core.MINASConfiguration;
import br.ufu.facom.minas.core.MINASModel;
import br.ufu.facom.minas.core.clustering.CluStream;
import br.ufu.facom.minas.core.clustering.KMeansPlusPlus;
import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.decisionrule.datainstance.DataInstanceDecisionRule_1;
import br.ufu.facom.minas.core.decisionrule.microcluster.MicroClusterDecisionRule_4;

import java.util.List;
import java.util.Random;

/**
 * This class can be used to apply MINAS to the covtype dataset. If you are
 * looking for a more detailed example of how to execute MINAS and also how to
 * print the results in external files, please check the {@link MOA3} class.
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class covtype {

    public static final String DATASET_COLUMN_SEPARATOR = ",";
    public static final String[] DATASET = new String[]{"./datasets/covtype.csv"};
    public static final int TRAINING_DATA_SIZE = 47045;
    public static final int CLU_STREAM_INITIAL_DATA_SIZE = 6000;
    public static final int CLU_STREAM_BUFFER_MAX_SIZE = 300;
    public static final int K_MEANS_PLUS_PLUS_K = 100;
    public static final double DECISION_RULE_FACTOR = 2;
    public static final int TEMPORARY_MEMORY_MAX_SIZE = 8000;
    public static final int MINIMUM_CLUSTER_SIZE = 20;
    public static final int WINDOW_SIZE = 8000;
    public static final int MICRO_CLUSTER_LIFESPAN = 8000;
    public static final int INSTANCE_LIFESPAN = 8000;
    public static final boolean IS_INCREMENTAL = false;


    public static void main(final String[] args) throws Exception {

        final Random random = new Random(0);

        final DatasetFileReader datasetFileReader = new DatasetFileReader(DATASET_COLUMN_SEPARATOR, DATASET);
        final List<DataInstance> trainingInstances = datasetFileReader.getBatch(TRAINING_DATA_SIZE);

        final MINASConfiguration config = new MINASConfiguration(
                new CluStream(CLU_STREAM_INITIAL_DATA_SIZE, CLU_STREAM_BUFFER_MAX_SIZE, random),
                new KMeansPlusPlus(K_MEANS_PLUS_PLUS_K, random),
                new MicroClusterDecisionRule_4(),
                new DataInstanceDecisionRule_1(DECISION_RULE_FACTOR),
                TEMPORARY_MEMORY_MAX_SIZE,
                MINIMUM_CLUSTER_SIZE,
                WINDOW_SIZE,
                MICRO_CLUSTER_LIFESPAN,
                INSTANCE_LIFESPAN,
                IS_INCREMENTAL);

        System.out.println("Training...");

        final MINASModel model = MINAS.initializeModel(trainingInstances, config);

        DataInstance instance = datasetFileReader.getNext();
        while (instance != null) {

            MINAS.process(instance, model, config);

            instance = datasetFileReader.getNext();

            final String output = "Timestamp = " + model.getLastTimestamp()
                    + "; Novelty count = " + model.getNoveltyCount()
                    + "; UnkR = " + model.getConfusionMatrix().measureUnkR()
                    + "; CER = " + model.getConfusionMatrix().measureCER() ;

            System.out.println(output);
        }

        datasetFileReader.close();

    }
}

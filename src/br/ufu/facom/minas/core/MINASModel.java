package br.ufu.facom.minas.core;

import br.ufu.facom.minas.core.datastructure.DataInstance;
import br.ufu.facom.minas.core.datastructure.DynamicConfusionMatrix;
import br.ufu.facom.minas.core.datastructure.MicroCluster;

import java.util.ArrayList;
import java.util.List;

public class MINASModel {

    private long lastTimestamp;
    private int noveltyCount;
    private final List<DataInstance> temporaryMemory;
    private final List<MicroCluster> decisionModel;
    private final List<MicroCluster> sleepMemory;
    private final DynamicConfusionMatrix confusionMatrix;

    MINASModel(final List<MicroCluster> modelMicroClusters, final DynamicConfusionMatrix confusionMatrix) {
        this.confusionMatrix = confusionMatrix;
        this.decisionModel = modelMicroClusters;
        this.noveltyCount = 0;
        this.sleepMemory = new ArrayList<>();
        this.temporaryMemory = new ArrayList<>();
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    void setLastTimestamp(final long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public int getNoveltyCount() {
        return noveltyCount;
    }

    public DynamicConfusionMatrix getConfusionMatrix() {
        return confusionMatrix;
    }

    void setNoveltyCount(final int noveltyCount) {
        this.noveltyCount = noveltyCount;
    }

    List<DataInstance> getTemporaryMemory() {
        return temporaryMemory;
    }

    List<MicroCluster> getDecisionModel() {
        return decisionModel;
    }

    List<MicroCluster> getSleepMemory() {
        return sleepMemory;
    }

}

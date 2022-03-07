package br.ufu.facom.minas.core.datastructure;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class representing a dynamic confusion matrix.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class DynamicConfusionMatrix {

    private final List<String> rowLabels;
    private final List<String> knownColumnLabels;
    private final List<String> noveltyColumnLabels;

    //Number of columns
    private int knownColumnsCount;
    private int noveltyColumnsCount;

    //Indices for matrix access
    private final Map<String, Integer> knownColumnIndexByLabel;
    private final Map<String, Integer> noveltyColumnIndexByLabel;
    private final Map<String, Integer> rowIndexByLabel;

    //Matrix
    private final List<List<Integer>> knownColumnsMatrix;
    private final List<List<Integer>> noveltyColumnsMatrix;
    private final List<Integer> unknownColumn;

    public DynamicConfusionMatrix(final Set<String> knownLabels) {

        this.rowLabels = new LinkedList<>();
        this.knownColumnLabels = new LinkedList<>();
        this.noveltyColumnLabels = new LinkedList<>();

        this.knownColumnsCount = 0;
        this.noveltyColumnsCount = 0;

        this.knownColumnIndexByLabel = new HashMap<>();
        this.noveltyColumnIndexByLabel = new HashMap<>();
        this.rowIndexByLabel = new HashMap<>();

        this.knownColumnsMatrix = new LinkedList<>();
        this.noveltyColumnsMatrix = new LinkedList<>();
        this.unknownColumn = new LinkedList<>();

        for (final String knownLabel : knownLabels) {
            addLabel(knownLabel);
        }
    }

    private void addLabel(final String label) {

        if (this.knownColumnLabels.contains(label)) {
            return;
        }

        this.addKnownColumn(label);
        if (!this.rowLabels.contains(label)) {
            this.addRow(label);
        }
    }

    public void updatedDelayed(final DataInstance instance, final String predictedLabel, final boolean isNovel) {

        final String realLabel = instance.getLabel();

        final int rowIndex = this.rowIndexByLabel.get(realLabel);
        final int value = this.unknownColumn.get(rowIndex);
        this.unknownColumn.set(rowIndex, value - 1);
        this.addPrediction(instance, predictedLabel, isNovel);
    }

    public void addUnknown(final DataInstance instance) {
        final String realLabel = instance.getLabel();
        if (!this.rowLabels.contains(realLabel)) {
            this.addRow(realLabel);
        }
        final int rowIndex = this.rowIndexByLabel.get(realLabel);
        final int value = this.unknownColumn.get(rowIndex);
        this.unknownColumn.set(rowIndex, value + 1);
    }

    public void addPrediction(final DataInstance instance, final String predictedLabel, final boolean isNovel) {

        final String realLabel = instance.getLabel();

        if (!this.rowLabels.contains(realLabel)) {
            this.addRow(realLabel);
        }

        final int rowIndex = this.rowIndexByLabel.get(realLabel);

        if (isNovel) {

            if (!this.noveltyColumnLabels.contains(predictedLabel)) {
                this.addNoveltyColumn(predictedLabel);
            }

            final int columnIndex = this.noveltyColumnIndexByLabel.get(predictedLabel);
            final int count = this.noveltyColumnsMatrix.get(rowIndex).get(columnIndex);
            this.noveltyColumnsMatrix.get(rowIndex).set(columnIndex, count + 1);

        } else {

            if (!this.knownColumnIndexByLabel.containsKey(predictedLabel)) {
                this.addKnownColumn(predictedLabel);
            }

            final int columnIndex = this.knownColumnIndexByLabel.get(predictedLabel);
            final int count = this.knownColumnsMatrix.get(rowIndex).get(columnIndex);
            this.knownColumnsMatrix.get(rowIndex).set(columnIndex, count + 1);

        }

    }

    public double measureCER() {

        final int totalExplainedSamples = this.numberOfExplainedSamples();
        final Map<String, List<String>> association = calculateNoveltyAssociationByRow();

        double sum = 0;
        for (final String rowLabel : this.rowLabels) {

            final int fp = this.calculateFP(rowLabel, association);
            final int fn = this.calculateFN(rowLabel, association);
            final int tn = this.calculateTN(rowLabel, association);
            final int tp = this.calculateTP(rowLabel, association);

            final int numberOfExplainedSamples = this.numberOfExplainedSamplesPerLabel(rowLabel);
            if (numberOfExplainedSamples > 0) {
                final double rate = numberOfExplainedSamples / (double) totalExplainedSamples;
                sum += rate * (fp / (double) Math.max(1, fp + tn)) + rate * (fn / (double) Math.max(1, fn + tp));
            }
        }

        return sum / 2;
    }

    public double measureUnkR() {

        double sum = 0;

        for (final String rowLabel : this.rowLabels) {

            final double unexplained = this.unknownColumn.get(this.rowIndexByLabel.get(rowLabel));
            final double explained = this.numberOfExplainedSamplesPerLabel(rowLabel);

            if (explained > 0) {
                sum += unexplained / (explained + unexplained);
            } else if (unexplained > 0) {
                sum += 1;
            }
        }

        return sum / this.rowLabels.size();
    }

    @Override
    public String toString() {

        final List<String> sortedKnownColumnLabels = new LinkedList<>(this.knownColumnLabels);
        Collections.sort(sortedKnownColumnLabels, new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2) {
                return Integer.compare(rowLabels.indexOf(o1), rowLabels.indexOf(o2));
            }
        });

        final String[][] matrix = new String[this.rowLabels.size() + 1][this.knownColumnsCount + this.noveltyColumnsCount + 2];

        for (int i = 0; i < sortedKnownColumnLabels.size(); ++i) {
            matrix[0][i + 1] = sortedKnownColumnLabels.get(i);
        }

        for (int i = 0; i < this.noveltyColumnLabels.size(); ++i) {
            matrix[0][i + sortedKnownColumnLabels.size() + 1] = this.noveltyColumnLabels.get(i);
        }

        for (int i = 0; i < this.rowLabels.size(); ++i) {
            matrix[i + 1][0] = this.rowLabels.get(i);
        }

        for (int i = 0; i < this.rowLabels.size(); ++i) {
            for (int j = 0; j < this.knownColumnsCount; ++j) {

                final String row = this.rowLabels.get(i);
                final String column = sortedKnownColumnLabels.get(j);

                final int rowIndex = this.rowIndexByLabel.get(row);
                final int columnIndex = this.knownColumnIndexByLabel.get(column);

                matrix[i + 1][j + 1] = this.knownColumnsMatrix.get(rowIndex).get(columnIndex).toString();
            }
        }

        for (int i = 0; i < this.unknownColumn.size(); ++i) {
            matrix[i + 1][this.knownColumnsCount + this.noveltyColumnsCount + 1] = this.unknownColumn.get(i).toString();
        }

        for (int i = 0; i < this.rowLabels.size(); ++i) {
            for (int j = 0; j < this.noveltyColumnsCount; ++j) {

                final String row = this.rowLabels.get(i);
                final String column = this.noveltyColumnLabels.get(j);

                final int rowIndex = this.rowIndexByLabel.get(row);
                final int columnIndex = this.noveltyColumnIndexByLabel.get(column);

                matrix[i + 1][j + this.knownColumnsCount + 1] =
                        this.noveltyColumnsMatrix.get(rowIndex).get(columnIndex).toString();
            }

        }

        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[0].length; ++j) {
                if (i == 0 && j == 0) {
                    stringBuilder.append(String.format("  %6s;", ""));
                } else if (i == 0 && j > this.knownColumnsCount && j < this.knownColumnsCount + this.noveltyColumnsCount + 1) {
                    stringBuilder.append(String.format("PN%6d;", this.noveltyColumnIndexByLabel.get(matrix[i][j])));
                } else if (i == 0 && j > this.knownColumnsCount) {
                    stringBuilder.append(String.format("%1sUNKNOWN;", ""));
                } else if (j == 0 && i > this.knownColumnsCount){
                    stringBuilder.append(String.format("CN%6s;", matrix[i][j]));
                } else if (i == 0 || j == 0){
                    stringBuilder.append(String.format("CK%6s;", matrix[i][j]));
                } else {
                    stringBuilder.append(String.format("  %6s;", matrix[i][j]));
                }
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    private void addRow(final String label) {
        this.rowIndexByLabel.put(label, this.rowLabels.size());
        this.rowLabels.add(label);
        this.knownColumnsMatrix.add(new LinkedList<>(Collections.nCopies(knownColumnsCount, 0)));
        this.noveltyColumnsMatrix.add(new LinkedList<>(Collections.nCopies(noveltyColumnsCount, 0)));
        this.unknownColumn.add(0);

    }

    private void addKnownColumn(final String label) {
        this.knownColumnLabels.add(label);
        this.knownColumnIndexByLabel.put(label, this.knownColumnsCount++);
        for (final List<Integer> row : this.knownColumnsMatrix) {
            row.add(0);
        }
    }

    private void addNoveltyColumn(final String label) {
        this.noveltyColumnLabels.add(label);
        this.noveltyColumnIndexByLabel.put(label, this.noveltyColumnsCount++);
        for (final List<Integer> row : this.noveltyColumnsMatrix) {
            row.add(0);
        }
    }

    private Map<String, List<String>> calculateNoveltyAssociationByRow() {

        final Map<String, List<String>> noveltyAssociationByRow = new HashMap<>();

        for (final String noveltyColumnLabel: this.noveltyColumnLabels) {

            int max = 0;
            String label = null;

            for (final String rowLabel: this.rowLabels) {
                final int row = this.rowIndexByLabel.get(rowLabel);
                final int column = this.noveltyColumnIndexByLabel.get(noveltyColumnLabel);
                if (this.noveltyColumnsMatrix.get(row).get(column) > max) {
                    max = this.noveltyColumnsMatrix.get(row).get(column);
                    label = rowLabel;
                }
            }

            if (label != null) {
                List<String> novelties;
                if ((novelties = noveltyAssociationByRow.get(label)) != null) {
                    novelties.add(noveltyColumnLabel);
                } else {
                    novelties = new LinkedList<>();
                    novelties.add(noveltyColumnLabel);
                    noveltyAssociationByRow.put(label, novelties);
                }
            }
        }

        return noveltyAssociationByRow;

    }

    private int numberOfExplainedSamplesPerLabel(final String label) {

        final int rowIndex = this.rowIndexByLabel.get(label);

        int sum = 0;
        for (final int value : this.knownColumnsMatrix.get(rowIndex)) {
            sum += value;
        }

        for (final int value : this.noveltyColumnsMatrix.get(rowIndex)) {
            sum += value;
        }

        return sum;
    }

    private int numberOfExplainedSamples() {
        int sum = 0;
        for (final String rowLabel : this.rowLabels) {
            sum += numberOfExplainedSamplesPerLabel(rowLabel);
        }
        return sum;
    }

    private int calculateTP(final String label, final Map<String, List<String>> noveltyAssociationByRow) {

        int sum = 0;
        final int rowIndex = this.rowIndexByLabel.get(label);

        if (this.knownColumnLabels.contains(label)) {
            final int columnIndex = this.knownColumnIndexByLabel.get(label);
            sum += this.knownColumnsMatrix.get(rowIndex).get(columnIndex);
        }

        final List<String> novelties = noveltyAssociationByRow.get(label);
        if (novelties == null) {
            return sum;
        }

        for (final String novelty : novelties) {
            final int noveltyIndex = this.noveltyColumnIndexByLabel.get(novelty);
            sum += this.noveltyColumnsMatrix.get(rowIndex).get(noveltyIndex);
        }

        return sum;

    }

    private int calculateFP(final String label, final Map<String, List<String>> noveltyAssociationByRow) {

        int sum = 0;

        if (this.knownColumnLabels.contains(label)) {

            final int columnIndex = this.knownColumnIndexByLabel.get(label);

            for (final String rowLabel : this.rowLabels) {
                if (!Objects.equals(rowLabel, label)) {
                    final int rowIndex = this.rowIndexByLabel.get(rowLabel);
                    sum += this.knownColumnsMatrix.get(rowIndex).get(columnIndex);
                }
            }

        }

        final List<String> novelties = noveltyAssociationByRow.get(label);
        if (novelties == null) {
            return sum;
        }

        for (final String novelty : novelties) {
            final int noveltyIndex = this.noveltyColumnIndexByLabel.get(novelty);
            for (final String rowLabel : this.rowLabels) {
                if (!Objects.equals(rowLabel, label)) {
                    final int rowIndex = this.rowIndexByLabel.get(rowLabel);
                    sum += this.noveltyColumnsMatrix.get(rowIndex).get(noveltyIndex);
                }
            }
        }

        return sum;

    }

    private int calculateFN(final String label, final Map<String, List<String>> noveltyAssociationByRow) {

        int sum = 0;

        final int rowIndex = this.rowIndexByLabel.get(label);

        if (this.knownColumnLabels.contains(label)) {
            for (final String columnLabel : this.knownColumnLabels) {
                if (!Objects.equals(columnLabel, label)) {
                    final int columnIndex = this.knownColumnIndexByLabel.get(columnLabel);
                    sum += this.knownColumnsMatrix.get(rowIndex).get(columnIndex);
                }
            }
        }

        final List<String> novelties = noveltyAssociationByRow.get(label);
        if (novelties == null) {
            return sum;
        }

        for (final String columnLabel : this.noveltyColumnLabels) {
            if (!novelties.contains(columnLabel)) {
                final int columnIndex = this.noveltyColumnIndexByLabel.get(columnLabel);
                sum += this.noveltyColumnsMatrix.get(rowIndex).get(columnIndex);
            }
        }

        return sum;
    }

    private int calculateTN(final String label, final Map<String, List<String>> noveltyAssociationByRow) {

        int sum = 0;
        for (final String rowLabel : this.rowLabels) {
            if (!Objects.equals(rowLabel, label)) {
                final int measureTP = this.calculateTP(rowLabel, noveltyAssociationByRow);
                sum += measureTP;
            }
        }
        return sum;

    }
}
package br.ufu.facom.minas.core;

import br.ufu.facom.minas.core.datastructure.DataInstance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is used to read data instances from a dataset csv file and 
 * control the data instances' timestamp.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class DatasetFileReader {

    private int timestamp;
    private final String separator;
    private final BufferedReader[] bufferedReaders;
    private int activeBuffer;

    public DatasetFileReader(final String separator, final String... paths) throws FileNotFoundException {

        this.timestamp = 1;
        this.separator = separator;
        this.activeBuffer = 0;

        this.bufferedReaders = new BufferedReader[paths.length];
        for (int i = 0; i < paths.length; i++) {
            final File file = new File(paths[i]);
            final FileReader reader = new FileReader(file);
            this.bufferedReaders[i] = new BufferedReader(reader);
        }

    }
    
    /**
     * Reads and returns the next data instance in the stream. The timestamp
     * increases incrementally between data instances.
     *
     * @throws IOException if a problem occurs while reading the dataset file.
     *
     * @return the next data instance in the stream.
     */
    public DataInstance getNext() throws IOException {
        final DataInstance instance = this.read();
        ++this.timestamp;
        return instance;
    }

    private DataInstance read() throws IOException {
        final String line = this.bufferedReaders[this.activeBuffer].readLine();
        if (line == null) {
            if (this.activeBuffer == this.bufferedReaders.length - 1) {
                return null;
            } else {
                this.activeBuffer++;
                return read();
            }
        }

        final String[] values = line.split(this.separator);
        final int numberOfFeatures = values.length - 1;
        final String y = values[values.length - 1];
        final double[] x = new double[numberOfFeatures];

        for (int i = 0; i < numberOfFeatures; ++i) {
            x[i] = Double.parseDouble(values[i]);
        }

        return new DataInstance(x, y, this.timestamp);
    }

    /**
     * Reads and returns an {@link ArrayList} containing the next
     * {@code n} data instances in the stream. All the instances returned by
     * this method share the same timestamp.
     *
     * @param n number of instances to be returned
     *
     * @throws IOException if a problem occurs while reading the dataset file.
     *
     * @return an {@link ArrayList} list containing the next  {@code n} data
     * instances in the stream.
     */
    public ArrayList<DataInstance> getBatch(final int n) throws IOException {
        final ArrayList<DataInstance> samples = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            samples.add(this.read());
        }
        return samples;
    }

    public void close() throws Exception {
        for (final BufferedReader bufferedReader : this.bufferedReaders) {
            bufferedReader.close();
        }
    }
}
package br.ufu.facom.minas.core.datastructure;

import java.util.Objects;

/**
 * Class representing a data instance.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class DataInstance extends Point {

    private final int timestamp;
    private final String label;

    public DataInstance(final double[] x, final String label, final int timestamp) {
        super(x);
        this.timestamp = timestamp;
        this.label = label;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final DataInstance instance = (DataInstance) o;
        return timestamp == instance.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), timestamp);
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public String getLabel() {
        return this.label;
    }

}

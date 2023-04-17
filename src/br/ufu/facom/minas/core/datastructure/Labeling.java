package br.ufu.facom.minas.core.datastructure;

/**
 * Class used to map a timestamp to a label.
 *
 * @author <a href="https://github.com/douglas444">Douglas M. Cavalcanti</a>
 * @since 1.0
 */
public class Labeling {

    private final long timestamp;
    private final String label;
    private final boolean isNovelty;

    public Labeling(final long timestamp,
                    final String label,
                    final boolean isNovelty) {
        this.timestamp = timestamp;
        this.label = label;
        this.isNovelty = isNovelty;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLabel() {
        return label;
    }

    public boolean isNovelty() {
        return isNovelty;
    }
}

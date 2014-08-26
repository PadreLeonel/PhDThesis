package io.crowbar.diagnostic.spectrum;

import io.crowbar.diagnostic.Diagnostic;
import io.crowbar.diagnostic.DiagnosticElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class Spectrum<A extends Activity,
                               TM extends Metadata> {
    public interface MergeStrategy {
        double reduce (List<Double> scores);
    }

    public static final MergeStrategy AVG =
        new MergeStrategy() {
        @Override
        public double reduce (List<Double> scores) {
            if (scores.size() <= 0) return Double.NaN;

            double total = 0;

            for (Double s : scores) {
                total += s;
            }

            return total / scores.size();
        }
    };

    public static final MergeStrategy MAX =
        new MergeStrategy() {
        @Override
        public double reduce (List<Double> scores) {
            if (scores.size() <= 0) return Double.NaN;

            double max = 0;

            for (Double s : scores) {
                max = Math.max(max, s);
            }

            return max;
        }
    };

    public static final MergeStrategy SUM =
        new MergeStrategy() {
        @Override
        public double reduce (List<Double> scores) {
            if (scores.size() <= 0) return Double.NaN;

            double total = 0;

            for (Double s : scores) {
                total += s;
            }

            return total;
        }
    };


    private abstract class AbstractIterator<T>
    implements Iterator<T> {
        private int id = 0;

        protected abstract T get (int id);
        protected int getId () {
            return id;
        }

        @Override
        public void remove () {
            throw new UnsupportedOperationException();
        }

        @Override
        public T next () {
            if (!hasNext())
                throw new NoSuchElementException();

            return get(id++);
        }
    }

    private class TIterable implements Iterable<Transaction<A, TM> > {
        public Iterator<Transaction<A, TM> > iterator () {
            return new AbstractIterator<Transaction<A, TM> > () {
                       @Override
                       public boolean hasNext () {
                           return getId() < getTransactionCount();
                       }

                       @Override
                       protected Transaction<A, TM> get (int i) {
                           return getTransaction(i);
                       }
            };
        }
    }

    private class CIterable implements Iterable<Probe> {
        public Iterator<Probe> iterator () {
            return new AbstractIterator<Probe> () {
                       @Override
                       public boolean hasNext () {
                           return getId() < getProbeCount();
                       }

                       @Override
                       protected Probe get (int i) {
                           return getProbe(i);
                       }
            };
        }
    }

    Spectrum () {}

    public abstract Tree getTree ();

    public abstract int getTransactionCount ();
    public abstract int getProbeCount ();

    public abstract Transaction<A, TM> getTransaction (int transactionId);
    public abstract Probe getProbe (int probeId);

    public abstract ArrayList<Probe> getProbes ();

    public final Iterable<Probe> byProbe () {
        return new CIterable();
    }

    public final Iterable<Transaction<A, TM> > byTransaction () {
        return new TIterable();
    }

    private List<Double> reduce (List<List<Double> > l,
                                 MergeStrategy ms) {
        List<Double> ret = new ArrayList<Double> (l.size());

        for (List<Double> s : l) {
            if (s == null)
                ret.add(Double.NaN);
            else
                ret.add(ms.reduce(s));
        }

        return ret;
    }

    /**
     * @brief Calculates a score value per Tree Node using an arbitrary MergeStrategy.
     * This is used to convert a multiple fault ranking into single fault ranking.
     * @post ret.size() == getTree.size()
     * @return A list containing the score for each node.
     */
    public List<Double> getScorePerNode (Diagnostic diagnostic,
                                         MergeStrategy ms) {
        List<List<Double> > tmp = new ArrayList<List<Double> > (getTree().size());

        while (tmp.size() <= getTree().size())
            tmp.add(null);

        for (DiagnosticElement e : diagnostic) {
            for (int probeId : e.getCandidate()) {
                Probe probe = getProbe(probeId);

                if (probe == null) continue; // Ignore probes without information

                int nodeId = probe.getNode().getId();
                List<Double> list = tmp.get(nodeId);

                if (list == null) {
                    list = new LinkedList<Double> ();
                    tmp.set(nodeId, list);
                }

                list.add(e.getScore());
            }
        }

        return reduce(tmp, ms);
    }

    /**
     * @brief Calculates a score value per Probe using an arbitrary MergeStrategy.
     * This is used to convert a multiple fault ranking into single fault ranking.
     * @post ret.size() == getProbeCount()
     * @return A list containing the score for each probe.
     */
    public List<Double> getScorePerProbe (Diagnostic diagnostic,
                                          MergeStrategy ms) {
        List<List<Double> > tmp = new ArrayList<List<Double> > (getProbeCount());

        while (tmp.size() <= getProbeCount())
            tmp.add(null);

        for (DiagnosticElement e : diagnostic) {
            for (int probeId : e.getCandidate()) {
                List<Double> list = tmp.get(probeId);

                if (list == null) {
                    list = new LinkedList<Double> ();
                    tmp.set(probeId, list);
                }

                list.add(e.getScore());
            }
        }

        return reduce(tmp, ms);
    }

    @Override
    public final String toString () {
        StringBuilder str = new StringBuilder();


        str.append("{class='Spectrum', ");
        str.append("probes=[");
        boolean first = true;

        for (Probe probe : byProbe()) {
            if (!first)
                str.append(",");

            if (probe == null)
                str.append("null");
            else
                str.append(probe.toString());

            first = false;
        }

        str.append("], ");
        str.append("transactions=[");

        first = true;

        for (Transaction<A, TM> transaction : byTransaction()) {
            if (!first)
                str.append(",");

            str.append(transaction.toString());
            first = false;
        }

        str.append("]}");

        return str.toString();
    }
}
package io.crowbar.instrumentation.events;

import io.crowbar.diagnosis.Spectra;
import io.crowbar.instrumentation.runtime.Node;
import io.crowbar.instrumentation.runtime.Probe;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Pattern;


public class SpectraBuilder extends Spectra implements EventListener {
    private int transactionId = 0;
    private boolean error = false;
    private final List<String> ignorePatterns = new LinkedList<String> ();
    private boolean fallbackAccept = true;


    public SpectraBuilder () {}

    public SpectraBuilder (String... ignorePatterns) {
        this.ignorePatterns.addAll(Arrays.asList(ignorePatterns));
    }

    public SpectraBuilder (List<String> ignorePatterns) {
        this.ignorePatterns.addAll(ignorePatterns);
    }

    @Override
    public final void registerNode (Node node) throws Exception {}

    @Override
    public final void registerProbe (Probe probe) throws Exception {}

    @Override
    public final void startTransaction (int probeId) {}

    @Override
    public final void endTransaction (int probeId,
                                      String exception,
                                      boolean[] hitVector) {
        if (exception != null) {
            for (String ignPat : ignorePatterns) {
                if (Pattern.matches(ignPat, exception)) {
                    reset(false);
                    return;
                }
            }
        }

        setActivity(transactionId, hitVector);
        setError(transactionId, error);
        reset(true);
    }

    @Override
    public final void oracle (int probeId,
                              double error,
                              double confidence) {
        this.error = this.error || (error > 0);
    }

    private void reset (boolean newTransaction) {
        error = false;

        if (newTransaction)
            transactionId++;
    }
}
package io.crowbar.diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class SortedDiagnostic
extends Diagnostic {
    private final List<DiagnosticElement> elements;

    /**
     * @pre candidates.size () == scores.size()
     */
    public SortedDiagnostic (Diagnostic diagnostic) {
        List<DiagnosticElement> tmp = new ArrayList<DiagnosticElement> (diagnostic);
        Collections.sort(tmp);
        elements = Collections.unmodifiableList(tmp);
    }

    @Override
    public DiagnosticElement get (int id) {
        return elements.get(id);
    }

    @Override
    public int size () {
        return elements.size();
    }

    public List<Double> getScorePerProbe () {
        List<Double> lret = new ArrayList<Double> (size());
        Iterator<DiagnosticElement> i = elements.iterator();

        while (i.hasNext()) {
            DiagnosticElement de = (DiagnosticElement) i.next();
            lret.add(de.getScore());
        }

        return lret;
    }
}
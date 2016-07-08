package io.crowbar.instrumentation.passes.matchers;

public class WhiteList extends AbstractActionTaker {
    public WhiteList (Matcher m) {
        super(m);
    }

    @Override
    public final Action getAction (boolean matches) {
        if (matches)
            return Action.ACCEPT;

        return Action.NEXT;
    }
}
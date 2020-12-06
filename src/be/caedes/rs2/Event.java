package be.caedes.rs2;

public class Event {

    private boolean cancelled;

    public Event() {
        this.cancelled = false;
    }

    public int trigger() {
        return 0;
    }

    public final void cancel() {
        this.cancelled = true;
    }

    public final boolean isCancelled() {
        return cancelled;
    }

}

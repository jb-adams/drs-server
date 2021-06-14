package org.ga4gh.starterkit.drs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.ga4gh.starterkit.drs.utils.SerializeView;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonView(SerializeView.Always.class)
public class DrsCartSummary {
    private int requested;
    private int loaded;
    private int unloaded;

    public void setRequested(int requested) {
        this.requested = requested;
    }

    public int getRequested() {
        return requested;
    }

    public void setLoaded(int loaded) {
        this.loaded = loaded;
    }

    public int getLoaded() {
        return loaded;
    }

    public void setUnloaded(int unloaded) {
        this.unloaded = unloaded;
    }

    public int getUnloaded() {
        return unloaded;
    }
}

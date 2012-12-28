package com.bluesmoke.farm.correlator.builder;

import com.bluesmoke.farm.correlator.GenericCorrelator;

public interface CorrelatorBuilder {
    public void build(GenericCorrelator aggressiveParent, GenericCorrelator passiveParent);
    public void build(GenericCorrelator parent);
}

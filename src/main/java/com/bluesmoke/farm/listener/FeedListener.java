package com.bluesmoke.farm.listener;

import com.bluesmoke.farm.model.tickdata.Tick;

public interface FeedListener {

    public void onNewTick(Tick tick);
}

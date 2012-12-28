//This is just a template file for a correlator

import com.bluesmoke.farm.correlator.CorrelatorPool;
import com.bluesmoke.farm.correlator.GenericCorrelator;
import com.bluesmoke.farm.correlator.builder.CorrelatorBuilderManager;
import com.bluesmoke.farm.model.tickdata.Tick;
import com.bluesmoke.farm.service.feed.FeedService;

import java.util.HashMap;

public class BlankClass extends GenericCorrelator{

    public BlankClass(String id, CorrelatorBuilderManager correlatorBuilderManager, CorrelatorPool pool, FeedService feed, GenericCorrelator aggressiveParent, GenericCorrelator passiveParent, HashMap<String, Object> config)
    {
        super("Price_" + pool.getNextID(), correlatorBuilderManager, pool, feed, aggressiveParent, passiveParent, config);
    }


    @Override
    public void createMutant() {
        new BlankClass("Price_" + pool.getNextID(), correlatorBuilderManager, pool, feed, null, null, config);
    }

    @Override
    public String createState() {

        double price = currentTick.getPairData(pair.name()).getMid();
        currentUnderlyingComponents.put("price", price);
        return "" + (int)(price/(10 * resolution));
    }
}

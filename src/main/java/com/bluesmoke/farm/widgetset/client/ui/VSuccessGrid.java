package com.bluesmoke.farm.widgetset.client.ui;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.vaadin.gwtgraphics.client.shape.Rectangle;
import org.vaadin.gwtgraphics.client.shape.Text;

import java.util.TreeMap;

public class VSuccessGrid {

    private VCorrelatorPoolAnalytics canvas;
    private TreeMap<Integer, TreeMap<Integer, Rectangle>> grid = new TreeMap<Integer, TreeMap<Integer, Rectangle>>();
    private TreeMap<Integer, TreeMap<Integer, Long>> gridVals = new TreeMap<Integer, TreeMap<Integer, Long>>();
    private Rectangle screen;
    private Text close;
    private Text title;

    public VSuccessGrid(VCorrelatorPoolAnalytics canvas)
    {
        this.canvas = canvas;
        screen = canvas.rect(0, 0, canvas.width, canvas.height);
        screen.setFillColor("black");
        screen.setFillOpacity(0.8);
        screen.setStrokeOpacity(0);

        close = canvas.text("Close", 20,40);
        close.setStrokeOpacity(0);
        close.setFillColor("white");
        close.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        close.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        title = canvas.text("", 20,20);
        title.setStrokeOpacity(0);
        title.setFillColor("white");

        screen.setVisible(false);
        close.setVisible(false);
        title.setVisible(false);
    }

    public void hide()
    {
        for (TreeMap<Integer, Rectangle> area : grid.values())
        {
            for(Rectangle rect : area.values())
            {
                rect.setVisible(false);
            }
        }
        screen.setVisible(false);
        close.setVisible(false);
        title.setVisible(false);
    }

    public void show(String data)
    {
        String id = data.split(";")[0];
        title.setText(id);
        String gridValues = data.split(";")[1];
        long maxFreq = 0;
        int span = 0;
        gridVals.clear();
        String[] targets = gridValues.split("<");
        for(String target : targets)
        {
            int targetValue = Integer.parseInt(target.split(">")[0]);
            if(Math.abs(targetValue) > span)
            {
                span = Math.abs(targetValue);
            }
            String[] observed = target.split(">")[1].split(",");
            for(String obstr : observed)
            {
                int observedValue = Integer.parseInt(obstr.split("@")[0]);
                if(Math.abs(observedValue) > span)
                {
                    span = Math.abs(observedValue);
                }
                long freq = Long.parseLong(obstr.split("@")[1]);
                if(maxFreq < freq)
                {
                    maxFreq = freq;
                }

                if(!grid.containsKey(targetValue))
                {
                    grid.put(targetValue, new TreeMap<Integer, Rectangle>());
                }
                if(!grid.get(targetValue).containsKey(observedValue))
                {
                    grid.get(targetValue).put(observedValue, canvas.rect(10, 10, 10, 10));
                }

                if(!gridVals.containsKey(targetValue))
                {
                    gridVals.put(targetValue, new TreeMap<Integer, Long>());
                }
                gridVals.get(targetValue).put(observedValue, freq);
            }
        }
        screen.setVisible(true);
        close.setVisible(true);
        canvas.getCanvas().bringToFront(screen);
        screen.setWidth(canvas.width);
        screen.setHeight(canvas.height);
        canvas.getCanvas().bringToFront(close);
        title.setVisible(true);
        canvas.getCanvas().bringToFront(title);

        int length = canvas.height;
        if(length > canvas.width)
        {
            length = canvas.width;
        }
        length = (length - 50)/(2*span);

        for (int target : grid.keySet())
        {
            int x = (canvas.width/2 - span/2) + (target * span);
            TreeMap<Integer, Rectangle> area = grid.get(target);
            for(int observed : area.keySet())
            {
                Rectangle rect = area.get(observed);
                rect.setVisible(true);
                canvas.getCanvas().bringToFront(rect);

                rect.setFillColor("orange");
                rect.setFillOpacity((double) gridVals.get(target).get(observed) / maxFreq);
                rect.setHeight(length);
                rect.setWidth(length);

                int y = (canvas.height/2 - span/2) - (observed * span);
                rect.setX(x);
                rect.setY(y);
            }
        }
    }
}

package com.bluesmoke.farm.widgetset.client.ui;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import org.vaadin.gwtgraphics.client.Shape;
import org.vaadin.gwtgraphics.client.shape.Path;
import org.vaadin.gwtgraphics.client.shape.Rectangle;
import org.vaadin.gwtgraphics.client.shape.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class VHintPopUp implements Refreshable{

    private VCorrelatorPoolAnalytics canvas;
    private Rectangle body;
    private Path arrow;
    private int x;
    private int y;
    private int hideOn = 0;
    private boolean visible = false;

    private boolean mouseOver = false;

    private ArrayList<Shape> components = new ArrayList<Shape>();
    private HashMap<Shape, Coordinates> sCoords = new HashMap<Shape, Coordinates>();
    private HashMap<Shape, String> sType = new HashMap<Shape, String>();

    public VHintPopUp(VCorrelatorPoolAnalytics canvas, int x, int y)
    {
        this.canvas = canvas;
        this.x = x;
        this.y = y;
        canvas.addRefreshable(this);
        body = canvas.rect(x, y, 10, 10);
        body.setStrokeColor("orange");
        body.setFillColor("black");
        body.setFillOpacity(0.75);
        body.setStrokeOpacity(1);
        body.setStrokeWidth(3);

        arrow = canvas.beginPath();
        arrow.setX(x - 5);
        arrow.setY(y + body.getHeight()/2);

        arrow.lineRelativelyTo(5, - 5);
        arrow.lineRelativelyTo(0, 10);
        arrow.lineRelativelyTo(-5, -5);
        arrow.close();

        arrow.setFillColor("orange");
        arrow.setFillOpacity(1);
        arrow.setStrokeOpacity(0);

        hide();

        body.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                mouseOver = true;
            }
        });
        body.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                mouseOver = false;
            }
        });

        arrow.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                mouseOver = true;
            }
        });
        arrow.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                mouseOver = false;
            }
        });
    }

    public void addComponent(Shape vO, int x , int y, String type)
    {
        components.add(vO);
        sType.put(vO, type);
        Coordinates coords = new Coordinates();
        coords.x = x;
        coords.y = y;
        sCoords.put(vO, coords);
        vO.setX(this.x + x);
        vO.setY(this.y + y);

        structure();
        if(hideOn < canvas.frameCount)
        {
            hide();
        }

        vO.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                mouseOver = true;
            }
        });
        vO.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                mouseOver = false;
            }
        });
    }

    public int addComponent(Shape vO, int x, String type)
    {
        int y = body.getHeight() + 5;
        components.add(vO);
        sType.put(vO, type);
        Coordinates coords = new Coordinates();
        coords.x = x;
        coords.y = y;
        sCoords.put(vO, coords);
        vO.setX(this.x + x);
        vO.setY(this.y + y);

        structure();
        if(hideOn < canvas.frameCount)
        {
            hide();
        }

        vO.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                mouseOver = true;
            }
        });
        vO.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                mouseOver = false;
            }
        });
        return y;
    }

    public void refresh() {
        if(hideOn == canvas.frameCount)
        {
            hide();
        }
        if(mouseOver){
            maintainVisibility();
        }
    }

    public void hide()
    {
        hideOn = canvas.frameCount;
        mouseOver = false;
        body.setVisible(false);
        arrow.setVisible(false);
        for(Shape vO : components)
        {
            vO.setVisible(false);
        }
        visible = false;
    }

    public void show()
    {
        maintainVisibility();
        body.setVisible(true);
        arrow.setVisible(true);
        canvas.getCanvas().bringToFront(body);
        canvas.getCanvas().bringToFront(arrow);
        for(Shape vO : components)
        {
            vO.setVisible(true);
            canvas.getCanvas().bringToFront(vO);
        }
        visible = true;
    }

    public void structure()
    {
        int maxW = 0;
        int maxH = 0;
        for(Shape vO : components)
        {
            int height = 0;
            int width = 0;

            if(sType.get(vO).equals("Text"))
            {
                height = ((Text)vO).getTextHeight();
                width = ((Text)vO).getTextWidth();
            }
            else {
                height = vO.getOffsetHeight();
                width = vO.getOffsetWidth();
            }
            vO.setX(this.x + sCoords.get(vO).x);
            vO.setY(this.y + sCoords.get(vO).y);
            if(vO.getAbsoluteTop() + height - y > maxH)
            {
                maxH = vO.getY() + height - y;
            }

            if(vO.getAbsoluteLeft() + width - x > maxW)
            {
                maxW = vO.getX() + width - x;
            }
        }
        body.setWidth(maxW + 10);
        body.setHeight(maxH + 10);
        arrow.setX(x - 5);
        arrow.setY(y + 5);
        show();
    }

    public void move(int x, int y)
    {
        this.x = x;
        this.y = y;
        body.setX(this.x);
        body.setY(this.y);

        structure();
        if(hideOn < canvas.frameCount)
        {
            hide();
        }
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void maintainVisibility()
    {
        hideOn = canvas.frameCount + (5 * canvas.frameRate);
    }
}

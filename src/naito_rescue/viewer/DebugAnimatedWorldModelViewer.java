package naito_rescue.viewer;

import rescuecore2.standard.view.*;
import rescuecore2.standard.entities.*;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import naito_rescue.viewer.layer.*;
/**
   A viewer for StandardWorldModels.
 */
public class DebugAnimatedWorldModelViewer extends StandardWorldModelViewer {
    private static final int FRAME_COUNT = 10;
    private static final int ANIMATION_TIME = 750;
    private static final int FRAME_DELAY = ANIMATION_TIME / FRAME_COUNT;

    private DebugAnimatedHumanLayer humans;
    private Timer timer;
    private final Object lock = new Object();
    private boolean done;

    /**
       Construct an animated world model viewer.
    */
    public DebugAnimatedWorldModelViewer() {
        super();
        timer = new Timer(FRAME_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (lock) {
                        if (done) {
                            return;
                        }
                        done = true;
                        if (humans.nextFrame()) {
                            done = false;
                            repaint();
                        }
                    }
                }
            });
        timer.setRepeats(true);
        timer.start();
    }
	public void setTargetHuman(Human h){
		humans.setTargetHuman(h);
	}
    @Override
    public String getViewerName() {
        return "Animated world model viewer";
    }

    @Override
    public void addDefaultLayers() {
        //addLayer(new DebugBuildingLayer());
        //addLayer(new DebugRoadLayer());
        addLayer(new DebugAreaNeighboursLayer());
        //addLayer(new RoadBlockageLayer());
		addLayer(new DebugRoadLayer());
		addLayer(new RoadBlockageLayer());
		addLayer(new DebugBuildingLayer());
        addLayer(new BuildingIconLayer());
        humans = new DebugAnimatedHumanLayer();
        addLayer(humans);
        DebugCommandLayer commands = new DebugCommandLayer();
        addLayer(commands);
        commands.setRenderMove(false);
        addLayer(new PositionHistoryLayer());
    }

    @Override
    public void view(Object... objects) {
        super.view(objects);
        synchronized (lock) {
            done = false;
            humans.computeAnimation(FRAME_COUNT);
        }
    }
}

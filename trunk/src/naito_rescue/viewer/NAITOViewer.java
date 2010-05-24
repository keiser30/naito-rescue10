package naito_rescue.viewer;

import naito_rescue.viewer.layer.*;

import rescuecore2.messages.control.*;
import rescuecore2.messages.components.*;
import rescuecore2.view.ViewComponent;
import rescuecore2.view.ViewListener;
import rescuecore2.view.RenderedObject;
import rescuecore2.view.EntityInspector;
import rescuecore2.worldmodel.Entity;
import rescuecore2.standard.entities.*;
import sample.*;

import rescuecore2.standard.view.AnimatedWorldModelViewer;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.*;

import java.util.List;

import rescuecore2.standard.components.StandardViewer;

public class NAITOViewer extends StandardViewer {
    private static final int FONT_SIZE = 20;

    //private ViewComponent viewer;
	private DebugAnimatedWorldModelViewer viewer;
	private JFrame      frame, oframe;
    private JLabel      timeLabel;
	private JPanel      eastPanel, eastTopPanel, eastBottomPanel;
	private JScrollPane eastBottomScrollPane;
	private DebugObjectViewer inspector;
	private DebugAgentInfoViewer agentViewer;
	private Human                agentViewerTarget;

    @Override
    protected void postConnect() {
        super.postConnect();
        frame = new JFrame("NAITO DebugViewer 0.1 (" + model.getAllEntities().size() + " entities) ");
		viewer = new DebugAnimatedWorldModelViewer();
        viewer.initialise(config);
        viewer.view(model);
        // CHECKSTYLE:OFF:MagicNumber
        viewer.setPreferredSize(new Dimension(500, 500));
        // CHECKSTYLE:ON:MagicNumber
        timeLabel = new JLabel("Time: Not started", JLabel.CENTER);
        timeLabel.setBackground(Color.WHITE);
        timeLabel.setOpaque(true);
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, FONT_SIZE));

		//右側ペインの作成
		eastPanel = new JPanel();
		eastTopPanel = new JPanel();
		eastBottomPanel = new JPanel();
		eastPanel.setLayout(new GridLayout(2,1));
		eastPanel.setPreferredSize(new Dimension(500,500));
		eastTopPanel.setBorder(BorderFactory.createTitledBorder("Agent Information"));
		eastBottomPanel.setBorder(BorderFactory.createTitledBorder("Object Viewer"));
		//eastTopPanelの設定
		agentViewer = new DebugAgentInfoViewer();
		eastTopPanel.add(agentViewer);
		//eastBottomPanelの設定
		inspector = new DebugObjectViewer();
		eastBottomPanel.setLayout(new GridLayout(1,1));
		eastBottomPanel.add(inspector);
		eastBottomScrollPane = new JScrollPane(eastBottomPanel);
		//右側ペインの設定
		eastPanel.add(eastTopPanel);
		eastPanel.add(eastBottomScrollPane);

		//フレームにペインを貼り付け
        frame.add(viewer, BorderLayout.CENTER);
        frame.add(timeLabel, BorderLayout.NORTH);
		frame.add(eastPanel, BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);
        viewer.addViewListener(new ViewListener() {
                @Override
                public void objectsClicked(ViewComponent view, List<RenderedObject> objects) {
                    for (RenderedObject next : objects) {
                    	if(next.getObject() instanceof Entity){
							inspector.inspect((Entity)next.getObject());
							if(next.getObject() instanceof Human){
								agentViewer.setTargetAgent((Human)next.getObject());
								agentViewerTarget = (Human)next.getObject();
								viewer.setTargetHuman(agentViewerTarget);
							}
						}
					}
                }

                @Override
                public void objectsRollover(ViewComponent view, List<RenderedObject> objects) {
                }
            });
    }

    @Override
    protected void handleTimestep(final KVTimestep t) {
        super.handleTimestep(t);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    timeLabel.setText("Time: " + t.getTime());
                    viewer.view(model, t.getCommands());
                    viewer.repaint();
					if(agentViewerTarget != null){
						agentViewer.setTargetAgent(agentViewerTarget);
						agentViewer.setCommandList(t.getCommands());
					}
                }
            });
    }

    @Override
    public String toString() {
        return "NAITO Debug viewer 0.1";
    }
}

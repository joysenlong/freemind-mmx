/*
 * FreeMind - A Program for creating and viewing Mindmaps Copyright (C)
 * 2000-2004 Joerg Mueller, Daniel Polansky, Christian Foltin and others.
 * 
 * See COPYING for Details
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Created on 19.09.2004
 */
/* $Id: NodeBackgroundColorAction.java,v 1.1.4.3 2005-04-14 20:37:23 christianfoltin Exp $ */

package freemind.modes.actions;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ListIterator;

import javax.xml.bind.JAXBException;

import freemind.controller.Controller;
import freemind.controller.actions.ActionPair;
import freemind.controller.actions.ActorXml;
import freemind.controller.actions.FreemindAction;
import freemind.controller.actions.generated.instance.NodeBackgroundColorFormatAction;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.main.Tools;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;

public class NodeBackgroundColorAction extends FreemindAction implements ActorXml {
    private final ModeController controller;

    public NodeBackgroundColorAction(ModeController controller) {
        super("node_background_color", (String)null, controller);
        this.controller = controller;
        addActor(this);
    }

    
    
    public void actionPerformed(ActionEvent e) {
        Color color = Controller.showCommonJColorChooserDialog(controller
                .getView().getSelected(), controller.getText("choose_node_background_color"), controller.getSelected()
                .getBackgroundColor());
        if (color == null) {
            return;
        }
        for (ListIterator it = controller.getSelecteds().listIterator(); it
                .hasNext();) {
            MindMapNodeModel selected = (MindMapNodeModel) it.next();
            setNodeBackgroundColor(selected, color); 
        }
    }
    
    public static class RemoveNodeBackgroundColorAction extends NodeGeneralAction {

    	private final ModeController controller;
		public RemoveNodeBackgroundColorAction(final ModeController controller) {
            super(controller, "remove_node_background_color", (String)null);
            this.controller = controller;
            setSingleNodeOperation(new SingleNodeOperation(){

				public void apply(MindMapMapModel map, MindMapNodeModel node) {
					controller.setNodeBackgroundColor(node, null);
				}});
        }
    	
    }
    public void setNodeBackgroundColor(MindMapNode node, Color color) {
		try {
			NodeBackgroundColorFormatAction doAction = createNodeBackgroundColorFormatAction(node, color);
			NodeBackgroundColorFormatAction undoAction = createNodeBackgroundColorFormatAction(node, node.getBackgroundColor());
			controller.getActionFactory().startTransaction(this.getClass().getName());
			controller.getActionFactory().executeAction(new ActionPair(doAction, undoAction));
			controller.getActionFactory().endTransaction(this.getClass().getName());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public NodeBackgroundColorFormatAction createNodeBackgroundColorFormatAction(MindMapNode node, Color color) throws JAXBException {
		NodeBackgroundColorFormatAction nodeAction = controller.getActionXmlFactory().createNodeBackgroundColorFormatAction();
		nodeAction.setNode(node.getObjectId(controller));
		nodeAction.setColor(Tools.colorToXml(color));
		return nodeAction;
    }
    
    public void act(XmlAction action) {
		if (action instanceof NodeBackgroundColorFormatAction) {
			NodeBackgroundColorFormatAction nodeColorAction = (NodeBackgroundColorFormatAction) action;
			Color color = Tools.xmlToColor(nodeColorAction.getColor());
			MindMapNode node = controller.getNodeFromID(nodeColorAction.getNode());
			Color oldColor = node.getBackgroundColor() ;
			if (!Tools.safeEquals(color, oldColor)) {
                node.setBackgroundColor(color); // null
                controller.nodeChanged(node);
            }
		}
   }

    public Class getDoActionClass() {
        return NodeBackgroundColorFormatAction.class;
    }

}
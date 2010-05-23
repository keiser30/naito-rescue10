package naito_rescue.viewer;

import static rescuecore2.misc.collections.ArrayTools.convertArrayObjectToString;
import rescuecore2.view.*;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;

/**
   A component for inspecting Entities.
*/
public class DebugObjectViewer extends JTable {
    private static final Comparator<Property> PROPERTY_NAME_COMPARATOR = new Comparator<Property>() {
        @Override
        public int compare(Property p1, Property p2) {
            return p1.getURN().compareTo(p2.getURN());
        }
    };

    private EntityTableModel model;

    /**
       Create a new EntityInspector.
    */
    public DebugObjectViewer() {
        model = new EntityTableModel();
        setModel(model);
		//setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    /**
       Inspect an entity.
       @param e The entity to inspect.
    */
    public void inspect(Entity e) {
        model.setEntity(e);
    }

    private static class EntityTableModel extends AbstractTableModel {
        private Entity e;
        private List<Property> props;

        public EntityTableModel() {
            e = null;
            props = new ArrayList<Property>();
        }

        public void setEntity(Entity entity) {
            e = entity;
            props.clear();
            if (e != null) {
                props.addAll(e.getProperties());
                Collections.sort(props, PROPERTY_NAME_COMPARATOR);
            }
            fireTableStructureChanged();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return props.size() + 2;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch (col) {
            case 0:
                if (row == 0) {
                    return "ID";
                }
                else if (row == 1) {
                    return "Type";
                }
                else {
					String propertyName = props.get(row - 2).getURN();
					int lastColon = propertyName.lastIndexOf(':');
					String simplePropertyName = "property:" + propertyName.substring(lastColon+1);
                    return (Object)simplePropertyName;
                }
            case 1:
                if (row == 0) {
                    return e == null ? "" : e.getID();
                }
                else if (row == 1) {
                    return e == null ? "" : e.getURN();
                }
                else {
                    Property prop = props.get(row - 2);
                    if (prop.isDefined()) {
                        Object value = prop.getValue();
                        if (value.getClass().isArray()) {
                            return convertArrayObjectToString(value);
                        }
                        return value;
                    }
                    else {
                        return "Undefined";
                    }
                }
            default:
                throw new IllegalArgumentException("Invalid column: " + col);
            }
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
            case 0:
                return "Property";
            case 1:
                return "Value";
            default:
                throw new IllegalArgumentException("Invalid column: " + col);
            }
        }
    }
}

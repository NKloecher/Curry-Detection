package dk.picit.ai.display;

import dk.picit.ai.Flavor;
import dk.picit.ai.Result;
import dk.picit.ai.Sample;
import dk.picit.ai.Target;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Point2D;


public class AIResultComponent extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	
	private Flavor[] flavors;
	JPanel panel;
	
	public AIResultComponent(Flavor[] flavors, int width, int height) {
		super(new String[] {"Flavor Id", "Target", "Found", "Quality", "Found 2nd", "Quality", "HitRate"}, 0);
		this.flavors = flavors;
		
		JTable table = new JTable(this);
		table.setDefaultRenderer(Object.class, new ColorRenderer());
		
		JScrollPane tablescrll = new JScrollPane(table);
		Dimension dim = new Dimension(width, height);
		tablescrll.setMaximumSize(dim);
		tablescrll.setMinimumSize(dim);
		tablescrll.setPreferredSize(dim);
		tablescrll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//		tablescrll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		panel = new JPanel();
//		panel.setBorder(new EmptyBorder(0, 20, 20, 20));
		panel.add(tablescrll);
	}
	
	
	public void updateComponent(Sample sample) {
		//synchronized (sample) 
		{
			while(getRowCount() > 0) {
				removeRow(0);
			}
			
			if(sample != null) {
				for(Flavor flavor : flavors) {
					String[] row = new String[getColumnCount()];
					
					Result result = sample.getResult(flavor, 2, -1, false);
					if(result != null) {
						Target target = sample.getTarget(flavor);
						row[0] = flavor.id;
						
						Object value = sample.getValue(flavor);
						if(value != null) {
							if(value instanceof Float) {
								Float p = (Float)value;
								row[1] = String.format("%+3.2f", p);
							}
							else if(value instanceof Point2D.Float) {
								Point2D.Float p = (Point2D.Float)value;
								row[1] = String.format("%+3.2f", p.x) + ";" + String.format("%+3.2f", p.y);
							}
							else row[1] = value.toString();
						}
						else row[1] = "";
						
						for(int i = 0; i < result.targets.length; i++) {
							Object val = result.targets[i].value;
							if(val != null) {
								if(val instanceof Float) {
									Float p = (Float)val;
									row[2 + 2*i] = String.format("%+3.2f", p);
								}
								else if(val instanceof Point2D.Float) {
									Point2D.Float p = (Point2D.Float)val;
									row[2 + 2*i] = String.format("%+3.2f", p.x) + ";" + String.format("%+3.2f", p.y);
								}
								else row[2 + 2*i] = val.toString();
							}
							else row[2 + 2*i] = "";
							
							row[2 + 2*i + 1] = String.format("%4.3f", result.targets[i].quality);
						}
						row[6] = String.format("%4.1f", 100*flavor.hitRateAvgMean);
					}
					
					addRow(row);
				}
			}
		}
	}
	
	
	private class ColorRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 2104490115353450952L;
		
	    public ColorRenderer() {
	        setOpaque(true);
	    }

	    
		@Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			Color bc = c.getBackground();
	        if(column == 0) {
		        for(Flavor flavor : flavors) {
		        	if(flavor.id.equals(value)) {
		        		c.setBackground(flavor.color);
		        	}
		        }
	        }
	        else setBackground(bc);
	        
	        return c;
	    }
	}
}
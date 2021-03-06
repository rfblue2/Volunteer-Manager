/**
 * 
 */
package library.gui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.PatternSyntaxException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import library.database.DbManager;
import library.database.LetterMaker;
import library.database.Volunteer;
import library.gui.VolunteerTableModel;

/**
 * @author Roland
 * Displays a table of volunteers and actions
 */
public class VolunteerPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -376449639733918173L;
	private static JTable volunteerTable;
	private static JButton add;
	private static JButton remove;
	private static JLabel filterLabel;
	private static JTextField filterText;
	private static JComboBox<String> filterFields;
	private static JButton genLetters;
	private static JButton genLabels;
	private Color lightBlue;
	private TableRowSorter<VolunteerTableModel> sorter;
	private Font arial16;
	private JFileChooser fChooser;
	
	public VolunteerPanel()	{
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(new EmptyBorder(10, 10, 10, 10));//adds 10px padding
		lightBlue = new Color(204, 204, 255);
		setBackground(lightBlue);
		arial16 = new Font("Arial", Font.PLAIN, 16);
		
		//instantiate table of volunteers
		//table uses VolunteerTableModel
		VolunteerTableModel vtm = new VolunteerTableModel();
		sorter = new TableRowSorter<VolunteerTableModel>(vtm);
		volunteerTable = new JTable(vtm);
		volunteerTable.setRowSorter(sorter);
		volunteerTable.setPreferredScrollableViewportSize(Toolkit.getDefaultToolkit().getScreenSize());
		JScrollPane tableScroll = new JScrollPane(volunteerTable);
		volunteerTable.setFillsViewportHeight(true);
		volunteerTable.getTableHeader().setFont(arial16);
		
		//instantiate buttons
		JPanel buttonPanel = new JPanel();//holds the buttons
		buttonPanel.setBackground(lightBlue);
		add = new JButton("Add");
		add.addActionListener(this);
		add.setFont(arial16);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		remove.setFont(arial16);
		filterLabel = new JLabel("Filter: ");
		filterLabel.setFont(arial16);
		filterFields = new JComboBox<String>(DbManager.getFields(DbManager.VOLUNTEERS).toArray(new String[DbManager.getFields(DbManager.VOLUNTEERS).size()]));
		filterFields.addActionListener(this);
		filterFields.setFont(arial16);
		filterText = new JTextField(15);
		filterText.setFont(arial16);
		filterText.getDocument().addDocumentListener(new DocumentListener()	{
			@Override
			public void changedUpdate(DocumentEvent e) {newFilter();}
			@Override
			public void insertUpdate(DocumentEvent e) {newFilter();}
			@Override
			public void removeUpdate(DocumentEvent e) {newFilter();}
			
		});
		genLetters = new JButton("Generate Letters");
		genLetters.addActionListener(this);
		genLetters.setFont(arial16);
		
		genLabels = new JButton("Generate Labels");
		genLabels.addActionListener(this);
		genLabels.setFont(arial16);
		
		fChooser = new JFileChooser();
		
		buttonPanel.add(add);
		buttonPanel.add(remove);
		buttonPanel.add(filterLabel);
		buttonPanel.add(filterFields);
		buttonPanel.add(filterText);
		buttonPanel.add(genLetters);
		buttonPanel.add(genLabels);
		
		//add components to volunteerPanel
		add(tableScroll);
		add(buttonPanel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == add)	{
			new AddVolunteerDialog();
		}
		else if(e.getSource() == remove)	{
			if(JOptionPane.showConfirmDialog(this, "Are you sure you want to remove Volunteers?") == JOptionPane.YES_OPTION)	{
				VolunteerTableModel vtm = (VolunteerTableModel) volunteerTable.getModel();
				//delete selected rows, but backwards in order to prevent changing indices from affecting removal
				int[] rows = volunteerTable.getSelectedRows();
				for(int r = rows[rows.length - 1]; r >= rows[0]; r--)	{
					int newR = volunteerTable.convertRowIndexToModel(r);
					if(!DbManager.getVolunteers().get(newR).getAttribute("Student").equals(""))	{
						PairingPanel.unpair(DbManager.getVolunteers().get(newR));
					}
					vtm.removeVolunteer((Integer.parseInt(volunteerTable.getModel().getValueAt(newR, 0).toString())));//gets the ID
				}
			}
		}
		else if(e.getSource() == genLetters)	{
			File vFolder = null;
			String path = null;
			int val = fChooser.showSaveDialog(fChooser);
			if(val == JFileChooser.APPROVE_OPTION)	{
				path = fChooser.getSelectedFile().getPath();
				vFolder = new File(path);
			}
			
			try	{
				vFolder.mkdir();
			} catch(SecurityException e1)	{
				e1.printStackTrace();
			}
			
			int[] rows = volunteerTable.getSelectedRows();
			if(rows.length == 0)	{
				JOptionPane.showMessageDialog(this, "Please select at least one volunteer", "Error", JOptionPane.ERROR_MESSAGE);
			} else	{
				this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				for(int r : rows)	{
					int newR = volunteerTable.convertRowIndexToModel(r);
					LetterMaker.genVolunteerNotification(DbManager.getVolunteers().get(newR), path);
				}
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				JOptionPane.showMessageDialog(this, "Generating Letters Complete", "Finished", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else if(e.getSource() == genLabels)	{
			String path = null;
			int val = fChooser.showSaveDialog(fChooser);
			if(val == JFileChooser.APPROVE_OPTION)
				path = fChooser.getSelectedFile().getPath() + ".docx";
			int[] rows = volunteerTable.getSelectedRows();
			if(rows.length == 0)	{
				JOptionPane.showMessageDialog(this, "Please select at least one volunteer", "Error", JOptionPane.ERROR_MESSAGE);
			} else	{
				this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				ArrayList<Volunteer> vols = new ArrayList<Volunteer>();
				for(int r : rows)	{
					int newR = volunteerTable.convertRowIndexToModel(r);
					vols.add(DbManager.getVolunteers().get(newR));
				}//TODO alphabetize
				LetterMaker.genLabels(vols, path);
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				JOptionPane.showMessageDialog(this, "Generating Labels Complete", "Finished", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	/**
	 * Creates new filter for input text and chosen field
	 */
	private void newFilter()	{
		RowFilter<VolunteerTableModel, Object> rf = null;
		try	{
			rf = RowFilter.regexFilter(filterText.getText(),  filterFields.getSelectedIndex());
		} catch (PatternSyntaxException e)	{
			return;
		}
		sorter.setRowFilter(rf);
	}
	
	/**
	 * @return table model of volunteers
	 */
	public static VolunteerTableModel getVolunteerTableModel()	{
		return (VolunteerTableModel) volunteerTable.getModel();
	}
	
	/**
	 * Dialog allowing user to add volunteer
	 * @author Roland
	 *
	 */
	private static class AddVolunteerDialog extends JDialog	{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8002031765980871025L;
		private Container content;
		private JTextField[] fields;
		private JLabel[] fieldLabels;
		private JPanel[] panels;
		private HashMap<String, JTextField> inputs;
		private JButton ok;
		private JButton cancel;
		private JPanel buttons;
		
		/**
		 * Constructor inits GUI
		 */
		public AddVolunteerDialog()	{
			content = getContentPane();
			//init arrays
			int size = Volunteer.fields.size();
			fields = new JTextField[size];
			fieldLabels = new JLabel[size];
			panels = new JPanel[size];
			inputs = new HashMap<String, JTextField>();
			content.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			//init input panels with label and textfield
			for(int i = 0; i < size; i++)	{
				fieldLabels[i] = new JLabel(Volunteer.fields.get(i));
				fieldLabels[i].setAlignmentX(Component.LEFT_ALIGNMENT);
				fields[i] = new JTextField(15);
				fields[i].setAlignmentX(Component.LEFT_ALIGNMENT);
				inputs.put(Volunteer.fields.get(i), fields[i]);//place text field into hashmap
				panels[i] = new JPanel();
				panels[i].add(fieldLabels[i]);
				panels[i].add(fields[i]);
				panels[i].setAlignmentY(Component.BOTTOM_ALIGNMENT);
				content.add(panels[i]);//add panel into the container
			}
			fields[DbManager.getFields(DbManager.VOLUNTEERS).indexOf("ID")].setEditable(false);//do not allow editing ID
			fields[DbManager.getFields(DbManager.VOLUNTEERS).indexOf("ID")].setText(""+(DbManager.getVolunteers().size() + 1));
			fields[DbManager.getFields(DbManager.VOLUNTEERS).indexOf("Student")].setEditable(false);//do not allow editing pair this way
			//init buttons
			buttons = new JPanel();
			ok = new JButton("Ok");
			ok.addActionListener(new ActionListener()	{
				@Override
				public void actionPerformed(ActionEvent e) {
					//TODO check input?
					Volunteer v = new Volunteer();
					for(int i = 0; i < fields.length; i++)	{
						if(v.getAttribute(i) instanceof Integer)
							v.setAttribute(i, Integer.parseInt(fields[i].getText()));
						else if(v.getAttribute(i) instanceof Long)
							v.setAttribute(i, Long.parseLong(fields[i].getText()));
						else
							v.setAttribute(i, fields[i].getText());
					}
					VolunteerTableModel vtm = (VolunteerTableModel) volunteerTable.getModel();
					vtm.addVolunteer(v);
					dispose();
				}
			});
			cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener()	{
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			
			buttons.add(ok);
			buttons.add(cancel);
			content.add(buttons);
			
			setAlwaysOnTop(true);
			setAutoRequestFocus(true);
			setVisible(true);
			setEnabled(true);
			this.pack();
		}
		
	}
}

/**
 * 
 */
package library.gui;

import java.util.ArrayList;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import library.database.DbManager;
import library.database.Student;

/**
 * @author Roland
 *
 */
public class StudentTableModel extends AbstractTableModel implements TableModelListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2334804479877681805L;
	//temporary, filler column names; later retrieve from database
	private ArrayList<String> colNames;
	private ArrayList<Student> data;
	
	public StudentTableModel()	{
		data = DbManager.getStudents();
		colNames = Student.fields;
		addTableModelListener(this);
	}
	
	@Override
	public int getColumnCount() {
		return colNames.size();
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int r, int c) {
		return data.get(r).getAttribute(c);
	}

	@Override
	public Class<?> getColumnClass(int c) {
		//return getValueAt(0, c).getClass();
		return Object.class;//I guess unless you pass obj, you can't mix...
	}

	@Override
	public String getColumnName(int col) {
		return colNames.get(col);
	}

	@Override
	public boolean isCellEditable(int r, int c) {
		if(c < 1)
			return false;
		else
			return true;
	}

	@Override
	public void setValueAt(Object val, int r, int c) {
		data.get(r).setAttribute(c, val);
		fireTableCellUpdated(r, c);
	}
	
	/**
	 * Adds student to table and database
	 * @param s
	 */
	public void addStudent(Student s)	{
		DbManager.addStudent(s);
		data.add(s);
		fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
	}
	
	/**
	 * Removes student (row) from table and database
	 * @param rowNum
	 */
	public void removeStudent(int rowNum)	{
		DbManager.remove(rowNum, DbManager.STUDENTS);
		data.remove(rowNum - 1);
		fireTableRowsDeleted(rowNum - 1, rowNum - 1);
		//shift ID
		for(int i = rowNum - 1; i < data.size(); i++)	{
			data.get(i).setAttribute(0, Integer.parseInt(data.get(i).getAttribute(0).toString()) - 1);
			fireTableCellUpdated(i, 0);
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		int r = e.getFirstRow();
		int c = e.getColumn();
		if(e.getType() == TableModelEvent.UPDATE)	{
			TableModel model = (TableModel)e.getSource();
			Object data = model.getValueAt(r, c);
			DbManager.editAttribute(r, c, data, DbManager.STUDENTS);
		}
	}
	
	/**
	 * @return data as arraylist
	 */
	public ArrayList<Student> getData()	{
		return data;
	}
}

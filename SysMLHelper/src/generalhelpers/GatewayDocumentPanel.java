package generalhelpers;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;

public class GatewayDocumentPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final public String m_BlankName = "Enter Document Name";
	private JTextField m_ChosenNameTextField = null;
	private JComboBox<String> m_AnalysisTypeComboBox = null;
	private JTextField m_Path = null;
	private String m_OriginalName = null;
	private String m_OriginalAnalysisType = null;
	private String m_OriginalPath = null;
	private JComboBox<Object> m_RequirementsPkgComboBox = null;
	private NamedElementMap m_NamedElementMap = null;
	
	public GatewayDocumentPanel(
			String theOriginalName,
			String[] theAnalysisTypeNames,
			String theSelectedAnalysisType,
			String thePath,
			List<IRPModelElement> thePackagesToImportInto,
			IRPProject theProject) throws FileNotFoundException {
		
		super();
		
		m_OriginalName = theOriginalName;
		m_OriginalAnalysisType = theSelectedAnalysisType;
		m_OriginalPath = thePath;
		
		m_ChosenNameTextField = new JTextField( theOriginalName );
		m_ChosenNameTextField.setMinimumSize( new Dimension( 200, 20 ) );
		m_ChosenNameTextField.setPreferredSize( new Dimension( 200, 20 ) );
		
		m_AnalysisTypeComboBox = new JComboBox<String>( theAnalysisTypeNames );
		m_AnalysisTypeComboBox.setSelectedItem( theSelectedAnalysisType );
		
		m_NamedElementMap = new NamedElementMap( thePackagesToImportInto );
		
		m_RequirementsPkgComboBox = new JComboBox<Object>( m_NamedElementMap.getFullNames() );
		
 		m_Path = new JTextField( thePath );
		m_Path.setMinimumSize( new Dimension(200, 20 ) );
		m_Path.setPreferredSize( new Dimension( 200, 20 ) );

		JPanel line1 = new JPanel();
		line1.setLayout( new BoxLayout( line1, BoxLayout.X_AXIS )  );
		line1.setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );
		line1.add( new JLabel("Create:  "));
		line1.add( m_ChosenNameTextField );
		line1.add( new JLabel("  with analysis type:  "));
		line1.add( m_AnalysisTypeComboBox );
		
		JPanel line2 = new JPanel();
		line2.setLayout( new BoxLayout( line2, BoxLayout.X_AXIS )  );
		line2.setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );
		line2.add( new JLabel("in destination package:  "));
		line2.add( m_RequirementsPkgComboBox );	
		line2.add( new JLabel("  with path (optional/leave blank):  "));
		line2.add( m_Path );	

		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );	
		add( line1 );
		add( line2 );
	}

	public String getOriginalName(){
		return m_OriginalName;
	}
	
	public String getOriginalAnalysisType(){
		return m_OriginalAnalysisType;
	}
	
	public String getOriginalPath(){
		return m_OriginalPath;
	}
	
	public String getReqtsPkgName(){
		
		return m_ChosenNameTextField.getText();
	}
	
	public IRPPackage getRootPackage(){
		
		Object theSelectedItem = m_RequirementsPkgComboBox.getSelectedItem();
		
		IRPModelElement theEl = m_NamedElementMap.getElementUsingFullName( theSelectedItem );
		
		if (theEl==null){
			Logger.writeLine("Error in getRootPackage, null element in list detected");
		}
		
		if (!(theEl instanceof IRPPackage)){
			Logger.writeLine("Error in getRootPackage, theEl is not an IRPPackage");
		}
		
		return (IRPPackage)theEl;
	}
	
	public String getAnalysisTypeName(){
		
		String theAnalysisTypeName = m_AnalysisTypeComboBox.getSelectedItem().toString();
		return theAnalysisTypeName;
	}
	
	public String getPathName(){
		
		String thePath = m_Path.getText();
		return thePath;
	} 
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)

    This file is part of SysMLHelperPlugin.

    SysMLHelperPlugin is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SysMLHelperPlugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SysMLHelperPlugin.  If not, see <http://www.gnu.org/licenses/>.
*/
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
	private JTextField m_ChosenBaselineTextField = null;
	private String m_OriginalName = null;
	private String m_OriginalAnalysisType = null;
	private String m_OriginalPath = null;
	private String m_OriginalBaseline = null;
	private JComboBox<Object> m_RequirementsPkgComboBox = null;
	private NamedElementMap m_NamedElementMap = null;
	
	public GatewayDocumentPanel(
			String theOriginalName,
			String[] theAnalysisTypeNames,
			String theSelectedAnalysisType,
			String thePath,
			String theBaseline,
			List<IRPModelElement> thePackagesToImportInto,
			IRPModelElement theDefaultPackage,
			boolean isImmutable,
			IRPProject theProject) throws FileNotFoundException {
		
		super();
		
		m_OriginalName = theOriginalName;
		m_OriginalAnalysisType = theSelectedAnalysisType;
		m_OriginalPath = thePath;
		m_OriginalBaseline = theBaseline;
		
		m_ChosenNameTextField = new JTextField( theOriginalName );
		m_ChosenNameTextField.setMinimumSize( new Dimension( 200, 20 ) );
		m_ChosenNameTextField.setPreferredSize( new Dimension( 200, 20 ) );
		m_ChosenNameTextField.setEnabled( !isImmutable );
		
		m_AnalysisTypeComboBox = new JComboBox<String>( theAnalysisTypeNames );
		m_AnalysisTypeComboBox.setSelectedItem( theSelectedAnalysisType );
		m_AnalysisTypeComboBox.setEnabled( !isImmutable );
		
		m_NamedElementMap = new NamedElementMap( thePackagesToImportInto );
		
		m_RequirementsPkgComboBox = new JComboBox<Object>( m_NamedElementMap.getFullNames() );
		
		m_RequirementsPkgComboBox.setSelectedItem( theDefaultPackage.getFullPathName()  );
		m_RequirementsPkgComboBox.setEnabled( !isImmutable );
		
 		m_Path = new JTextField( thePath );
		m_Path.setMinimumSize( new Dimension(200, 20 ) );
		m_Path.setPreferredSize( new Dimension( 200, 20 ) );
		m_Path.setEnabled( !isImmutable );

 		m_ChosenBaselineTextField = new JTextField( theBaseline );
 		m_ChosenBaselineTextField.setMinimumSize( new Dimension( 40, 20 ) );
 		m_ChosenBaselineTextField.setPreferredSize( new Dimension( 40, 20 ) );
 		m_ChosenBaselineTextField.setEnabled( !isImmutable );
		
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
		line2.add( new JLabel("  and baseline:  "));
		line2.add( m_ChosenBaselineTextField );	
		
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );	
		add( line1 );
		add( line2 );
	}

	public String getOriginalName(){
		return m_OriginalName.trim();
	}
	
	public String getOriginalAnalysisType(){
		return m_OriginalAnalysisType.trim();
	}
	
	public String getOriginalPath(){
		return m_OriginalPath.trim();
	}
	
	public String getOriginalBaseline(){
		return m_OriginalBaseline.trim();
	}
	
	public String getReqtsPkgName(){
		
		return m_ChosenNameTextField.getText().trim();
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
		
		String thePath = m_Path.getText().trim();
		return thePath;
	} 
	
	public String getBaseline(){
		
		String theBaseline = m_ChosenBaselineTextField.getText().trim();
		return theBaseline;
	} 
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #039 17-JUN-2016: Minor fixes and improvements to robustness of Gateway project setup (F.J.Chadburn)
    #051 06-JUL-2016: Re-factored the GW panel to allow it to incrementally add to previous setup (F.J.Chadburn)
    #066 19-JUL-2016: Added optional baseline box to the fast Gateway setup panel (F.J.Chadburn)

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
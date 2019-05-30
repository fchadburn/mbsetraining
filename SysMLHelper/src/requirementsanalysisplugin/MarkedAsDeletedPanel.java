package requirementsanalysisplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import com.telelogic.rhapsody.core.*;

import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.Logger;
import generalhelpers.NamedElementMap;
import generalhelpers.UserInterfaceHelpers;

public class MarkedAsDeletedPanel extends CreateStructuralElementPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<IRPModelElement> m_FoundReqts = new ArrayList<IRPModelElement>();
	
	public static void launchThePanel(
			final List<IRPModelElement> theSelectedEls ){
		
		final String theAppID = 
				UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );

				String theCaption;
				
				if( theSelectedEls.size()==1 ){
					theCaption = "Delete the 'Deleted_At_High_Level' tagged requirements for " + 
							Logger.elementInfo( theSelectedEls.get( 0 ) );
				} else {
					theCaption = "Delete the 'Deleted_At_High_Level' tagged requirements for " + 
							theSelectedEls.size() + " selected elements";
				}
				
				JFrame frame = new JFrame( theCaption );
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				MarkedAsDeletedPanel thePanel = 
						new MarkedAsDeletedPanel( theSelectedEls, theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	MarkedAsDeletedPanel(
			List<IRPModelElement> theSelectedEls,
			String theAppID ){
		
		super( theAppID );
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		Box theBox = Box.createVerticalBox();

		Set<IRPModelElement> theCandidateReqts = 
				buildSetOfRequirementsBasedOn( theSelectedEls ); 
		
		m_FoundReqts = filterTaggedRequirementsBasedOn( theCandidateReqts, "Deleted_At_High_Level" );
		
		JLabel theLabel;
		
		if( m_FoundReqts.isEmpty() ){
			theLabel = new JLabel("There are no Deleted_At_High_Level requirements");
			theLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theLabel );
			
		} else {
			
			final NamedElementMap theNamedElMap = new NamedElementMap( m_FoundReqts );
			
			Object[] dataList = theNamedElMap.getFullNamesIn();

			JList<Object> list = new JList<>( dataList );
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			list.addMouseListener(new MouseAdapter() {
			    public void mouseClicked(MouseEvent evt) {
			        @SuppressWarnings("rawtypes")
					JList list = (JList)evt.getSource();
			        
			        if (evt.getClickCount() >= 2) {

			            // Double-click detected
			            int index = list.locationToIndex(evt.getPoint());
			            
			            IRPModelElement theElement = theNamedElMap.getElementAt(index);
			            
			            if( theElement == null ){
			            	
			    			JDialog.setDefaultLookAndFeelDecorated(true);
			    			
			    			JOptionPane.showMessageDialog(
			    					null, 
			    					"Element no longer exists", 
			    					"Warning",
			    					JOptionPane.WARNING_MESSAGE);
			            	
			            } else {
			            	theElement.highLightElement();
			            }   
			            
			            Logger.writeLine( theElement, "was double-clicked" );
			        }
			    }
			});
			
			JScrollPane theScrollPane = new JScrollPane(list);
			theScrollPane.setBounds(1,1,16,58);
			
		    theScrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		    
		    JLabel theStartLabel = new JLabel("The following " + m_FoundReqts.size() + " requirements have the tag '" + "Deleted_At_High_Level" + "' applied:\n");
		    theStartLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theStartLabel );
			theBox.add( theScrollPane );
			
			 JLabel theEndLabel = new JLabel( "Do you want to delete them from the project?\n" );
			    theEndLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
				theBox.add( theEndLabel );
		}
		
		add( theBox, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	public static Set<IRPModelElement> buildSetOfRequirementsBasedOn(
			List<IRPModelElement> theSelectedEls ) {
		
		Set<IRPModelElement> theReqts = new HashSet<IRPModelElement>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			IRPModelElement theElementToSearchUnder = theSelectedEl;
			
			if( theSelectedEl instanceof IRPActivityDiagram ){	
				theElementToSearchUnder = theElementToSearchUnder.getOwner();
			}
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReqtsToAdd = 
					theElementToSearchUnder.getNestedElementsByMetaClass(
							"Requirement", 1 ).toList();
			
			theReqts.addAll( theReqtsToAdd );	
		}

		return theReqts;
	}
	
	public static List<IRPModelElement> filterTaggedRequirementsBasedOn(
			Set<IRPModelElement> theCandidateSet,
			String andTagName ){
		
		List<IRPModelElement> theFoundReqts = new ArrayList<IRPModelElement>();
		
		for( IRPModelElement theCandidateReqt : theCandidateSet ) {
			
			IRPTag theTag = theCandidateReqt.getTag( andTagName ); //"Deleted_At_High_Level" );
			
			if( theTag != null ){
				theFoundReqts.add( theCandidateReqt );
			}
		}
		
		return theFoundReqts;
	}

	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {
		return true;
	}

	@Override
	protected void performAction() {
		if( checkValidity( false ) ){
		
			for( IRPModelElement theReqtToDelete : m_FoundReqts ){
				
				if( theReqtToDelete != null && theReqtToDelete instanceof IRPModelElement ){
					Logger.writeLine("Deleting " + Logger.elementInfo( theReqtToDelete ) + " from project");
					theReqtToDelete.deleteFromProject();	
				}				
			}
		}
		
	}

}

/**
 * Copyright (C) 2017-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #155 25-JAN-2017: Added new panel to find and delete Gateway Deleted_At_High_Level req'ts with Rhp 8.2 (F.J.Chadburn)
    #256 29-MAY-2019: Rewrite to Java Swing dialog launching to make thread safe between versions (F.J.Chadburn)

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
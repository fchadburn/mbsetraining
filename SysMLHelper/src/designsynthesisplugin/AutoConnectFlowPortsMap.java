package designsynthesisplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

import designsynthesisplugin.AutoConnectFlowPortsInfo.FlowType;

public class AutoConnectFlowPortsMap extends HashMap<IRPInstance, AutoConnectFlowPortsInfo> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Set<AutoConnectFlowPortsInfo> getAutoConnectFlowPortsInfoThatMatch( 
			FlowType theFlowType ){
		
		Set<AutoConnectFlowPortsInfo> theMatches = new HashSet<AutoConnectFlowPortsInfo>();
		
		for( java.util.Map.Entry<IRPInstance, AutoConnectFlowPortsInfo> entry : this.entrySet() ){
		
			AutoConnectFlowPortsInfo theValue = entry.getValue();
			
			if( theValue.getExistingAttributeFlowType() == theFlowType ){
				theMatches.add( theValue );
			}
		}
		
		return theMatches;
	}
	
	public List<IRPInstance> getAutoConnectInstancesThatMatch( 
			FlowType theFlowType ){
		
		List<IRPInstance> theMatches = new ArrayList<IRPInstance>();
		
		for( java.util.Map.Entry<IRPInstance, AutoConnectFlowPortsInfo> entry : this.entrySet() ){
		
			IRPInstance theKey = entry.getKey();
			AutoConnectFlowPortsInfo theValue = entry.getValue();
			
			if( theValue.getExistingAttributeFlowType() == theFlowType ){
				theMatches.add( theKey );
			}
		}
		
		return theMatches;
	}

	public IRPInstance selectAutoConnectInstanceThatMatches( 
			FlowType theFlowType ){
		
		IRPInstance theMatch = null;
		
		List<IRPInstance> theMatches = 
				getAutoConnectInstancesThatMatch( theFlowType );
		
		if( theMatches.size()==1 ){
			theMatch = theMatches.get( 0 );
		}
		
		return theMatch;
	}
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #213 09-JUL-2017: Add dialogs to auto-connect «publish»/«subscribe» FlowPorts for white-box simulation (F.J.Chadburn)
    #237 27-SEP-2017: Resolved Java path problems with Entry in certain versions of Eclipse (F.J.Chadburn)
        
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
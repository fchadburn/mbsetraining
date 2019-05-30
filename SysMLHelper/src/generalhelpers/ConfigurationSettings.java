package generalhelpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import com.telelogic.rhapsody.core.*;

public class ConfigurationSettings {

	protected ResourceBundle _resources = null;
	protected Properties _properties = null;
	protected String _propertyFileName = null;
	protected String _resourceBundleFileName = null;

	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		IRPProject theRhpPrj = theRhpApp.activeProject();
		
		// set the properties
    	ConfigurationSettings theConfigSettings = 
    			new ConfigurationSettings( 
    					"ExecutableMBSE.properties", 
    					"ExecutableMBSE_MessagesBundle" );
    	
    	theConfigSettings.setPropertiesValuesRequestedInConfigFile( 
    			theRhpPrj,
    			"setPropertyForExecutableMBSEModel" );
	}
		   
	public ConfigurationSettings(
			String thePropertyFileName,
			String theResourceBundleFileName ) {
		
		_propertyFileName = thePropertyFileName;
		_resourceBundleFileName = theResourceBundleFileName;
		
		if( _properties == null ){
			
			InputStream inputStream = null;
			
			try {
				_properties = new Properties();
	 
				inputStream = getClass().getClassLoader().getResourceAsStream( thePropertyFileName );
	 
				if( inputStream != null ){
					_properties.load( inputStream );
				} else {
					String theMsg = "Exception: Expected property file called '" + 
							thePropertyFileName + "' was not found in the classpath";
					
					Logger.error( theMsg );
					throw new FileNotFoundException( theMsg );
				}
	 
			} catch( Exception e ){
				Logger.error( "Exception trying to open '" + thePropertyFileName + "': " + e.getMessage() );
				
			} finally {
				try {
					inputStream.close();
					
				} catch( IOException e ){
					Logger.error( "Exception trying to close '" + thePropertyFileName + "'" );
				}
			}
		}
			
		if( _properties != null && _resources == null ){
			
			String language = _properties.getProperty( "DefaultLanguage", "en" );
			String country = _properties.getProperty( "DefaultCountry", "US" );

			Locale currentLocale = new Locale( language, country );

			try {
				_resources = ResourceBundle.getBundle( 
						theResourceBundleFileName, 
						currentLocale ); 
				
				Logger.info( "PluginSettingsAndResources has loaded properties from " +
						thePropertyFileName + " and resource bundle from " + 
						theResourceBundleFileName + " for language=" + language + 
						" and county=" + country);

			} catch( Exception e ){
				Logger.error( "Exception while trying ResourceBundle.getBundle" );
			}
		}
	}
	
	public String getProperty(
			String key ){
		
		String value = _properties.getProperty( key );
		return value;
	}
	
	public String getProperty(
			String key, 
			String defaultValue ){
		
		String value = _properties.getProperty( key, defaultValue );
		return value;
	}
	
	public String getString(
			String key ){
		
		String value = _resources.getString( key );
		return value;		
	}
	
	public void setPropertiesValuesRequestedInConfigFile(
			IRPModelElement onTheElement,
			String basedOnContext ){

		for( String key : _properties.stringPropertyNames() ) {
			
			if( key.startsWith( basedOnContext ) ){
				
				String thePropertyName = key.replace( basedOnContext + ".", "");
				String thePropertyValue = getProperty( key );
				
				Logger.info( "Setting '" + thePropertyName + 
						"' to '" + thePropertyValue + "' based on '" + 
						_propertyFileName + "'" ); 
				
				try {
					onTheElement.setPropertyValue( 
							thePropertyName, thePropertyValue );
					
				} catch (Exception e) {
					
					Logger.error( "Exception in setPropertiesValuesRequestedInConfigFile, " +
							"unable to set " + thePropertyName + " to " + thePropertyValue + 
							" on " + Logger.elementInfo( onTheElement ) );
				}
			}
		}
	}
}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #109 06-NOV-2016: Added .properties support for localisation of menus (F.J.Chadburn)
    #110 06-NOV-2016: PluginVersion now comes from Config.properties file, rather than hard wired (F.J.Chadburn)
    #118 13-NOV-2016: Default FunctionalAnalysisPkg tags now set in Config.properties file (F.J.Chadburn)
    #142 18-DEC-2016: Project properties now set via config.properties, e.g., to easily switch off backups (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic features for /settings loading (F.J.Chadburn)
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

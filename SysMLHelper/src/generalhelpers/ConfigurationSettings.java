package generalhelpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import com.telelogic.rhapsody.core.IRPModelElement;

public class ConfigurationSettings {

	protected ResourceBundle m_resources = null;
	protected Properties m_properties = null;
	protected static ConfigurationSettings m_instance = null;

	public static ConfigurationSettings getInstance() {
		if( m_instance == null ){
			m_instance = new ConfigurationSettings();
		}
		return m_instance;
	}
	   
	protected ConfigurationSettings() {
		
		if( m_properties == null ){
			
			InputStream inputStream = null;
			
			try {
				m_properties = new Properties();
				String propFileName = "config.properties";
	 
				inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
	 
				if (inputStream != null) {
					m_properties.load( inputStream );
				} else {
					throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
				}
	 
			} catch (Exception e) {
				System.out.println("Exception trying to open config.properties: " + e);
				
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					Logger.writeLine("Exception trying to close config.properties");
				}
			}
		}
			
		if( m_properties != null && m_resources == null ){
			
			String language = m_properties.getProperty("DefaultLanguage", "en");
			String country = m_properties.getProperty("DefaultCountry", "US");

			Locale currentLocale = new Locale(language, country);

			Logger.writeLine("Loading resources for language=" + language + " and county=" + country);

			try {
				m_resources = ResourceBundle.getBundle("MessagesBundle", currentLocale); 

			} catch (Exception e) {
				Logger.writeLine("Exception while trying ResourceBundle.getBundle");
			}
		}
	}
	
	public String getProperty(String key){
		
		String value = m_properties.getProperty( key );
		return value;
	}
	
	public String getProperty(String key, String defaultValue){
		
		String value = m_properties.getProperty( key, defaultValue );
		return value;
	}
	
	public String getString(String key){
		
		String value = m_resources.getString( key );
		return value;		
	}
	
	public void setPropertiesValuesRequestedInConfigFile(
			IRPModelElement onTheElement,
			String basedOnContext ){

		for( String key : m_properties.stringPropertyNames() ) {
			
			if( key.startsWith( basedOnContext ) ){
				
				String thePropertyName = key.replace( basedOnContext + ".", "");
				String thePropertyValue = getProperty( key );
				
				Logger.writeLine( "Setting '" + thePropertyName + 
						"' to '" + thePropertyValue + "' based on config.properties value" ); 
				
				try {
					onTheElement.setPropertyValue( thePropertyName, thePropertyValue );
					
				} catch (Exception e) {
					
					Logger.writeLine("Exception in setPropertiesValuesRequestedInConfigFile, unable to set " + 
							thePropertyName + " to " + thePropertyValue + " on " + Logger.elementInfo(onTheElement));
				}
			}
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #109 06-NOV-2016: Added .properties support for localisation of menus (F.J.Chadburn)
    #110 06-NOV-2016: PluginVersion now comes from Config.properties file, rather than hard wired (F.J.Chadburn)
    #118 13-NOV-2016: Default FunctionalAnalysisPkg tags now set in Config.properties file (F.J.Chadburn)
    #142 18-DEC-2016: Project properties now set via config.properties, e.g., to easily switch off backups (F.J.Chadburn)

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

I-Logix-RPY-Archive version 8.11.0 C++ 9499822
{ IProject 
	- _id = GUID db2d59f3-a4b2-406d-b2b9-3dd0919de68e;
	- _myState = 8192;
	- _properties = { IPropertyContainer 
		- Subjects = { IRPYRawContainer 
			- size = 1;
			- value = 
			{ IPropertySubject 
				- _Name = "General";
				- Metaclasses = { IRPYRawContainer 
					- size = 1;
					- value = 
					{ IPropertyMetaclass 
						- _Name = "Model";
						- Properties = { IRPYRawContainer 
							- size = 1;
							- value = 
							{ IProperty 
								- _Name = "RenameUnusedFiles";
								- _Value = "True";
								- _Type = Bool;
							}
						}
					}
				}
			}
		}
	}
	- _name = "SysMLHelper";
	- _modifiedTimeWeak = 4.10.2016::21:22:29;
	- _description = { IDescription 
		- _textRTF = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang2057{\\fonttbl{\\f0\\fnil Courier New;}{\\f1\\fnil\\fcharset0 Courier New;}{\\f2\\fnil\\fcharset0 Arial;}}
{\\colortbl ;\\red63\\green95\\blue191;\\red127\\green127\\blue159;}
\\viewkind4\\uc1\\pard\\cf1\\f0\\fs20 /**\\cf0\\par
\\cf1  * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)\\cf0\\par
\\par
\\cf1     Change history:\\cf0\\par
\\cf1     #\\f1 XXX\\f0  \\f1 DD\\cf2\\f0 -\\f1 MMM\\f0 -\\f1 YYYY\\cf1\\f0 : \\f1 What changed\\f0  (\\f1 Who?\\f0 )\\cf0\\par
\\cf1         \\cf0\\par
\\cf1     This file is part of SysMLHelperPlugin.\\cf0\\par
\\par
\\cf1     SysMLHelperPlugin is free software: you can redistribute it and/or modify\\cf0\\par
\\cf1     it under the terms of the GNU General Public License as published by\\cf0\\par
\\cf1     the Free Software Foundation, either version 3 of the License, or\\cf0\\par
\\cf1     (at your option) any later version.\\cf0\\par
\\par
\\cf1     SysMLHelperPlugin is distributed in the hope that it will be useful,\\cf0\\par
\\cf1     but WITHOUT ANY WARRANTY; without even the implied warranty of\\cf0\\par
\\cf1     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\\cf0\\par
\\cf1     GNU General Public License for more details.\\cf0\\par
\\par
\\cf1     You should have received a copy of the GNU General Public License\\cf0\\par
\\cf1     along with SysMLHelperPlugin.  If not, see <http://www.gnu.org/licenses/>.\\cf0\\par
\\cf1 */\\cf0\\f2\\par
}
";
	}
	- _lastID = 8;
	- _UserColors = { IRPYRawContainer 
		- size = 16;
		- value = 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 
	}
	- _defaultSubsystem = { ISubsystemHandle 
		- _m2Class = "ISubsystem";
		- _filename = "GatewayProjectFiles.sbs";
		- _subsystem = "";
		- _class = "";
		- _name = "GatewayProjectFiles";
		- _id = GUID a6ca3a64-92a1-4a2b-b824-1cdb9f5661f0;
	}
	- _component = { IHandle 
		- _m2Class = "IComponent";
		- _filename = "NotUsedCmp.cmp";
		- _subsystem = "";
		- _class = "";
		- _name = "NotUsedCmp";
		- _id = GUID 15e00bf1-6846-47dc-acb4-6a0add0593b4;
	}
	- Multiplicities = { IRPYRawContainer 
		- size = 4;
		- value = 
		{ IMultiplicityItem 
			- _name = "1";
			- _count = -1;
		}
		{ IMultiplicityItem 
			- _name = "*";
			- _count = -1;
		}
		{ IMultiplicityItem 
			- _name = "0,1";
			- _count = -1;
		}
		{ IMultiplicityItem 
			- _name = "1..*";
			- _count = -1;
		}
	}
	- Subsystems = { IRPYRawContainer 
		- size = 6;
		- value = 
		{ IProfile 
			- fileName = "SysMLHelperProfile";
			- _id = GUID 1f16e485-bfb5-4a1d-b046-ef4230d5c187;
		}
		{ IProfile 
			- fileName = "GlobalPreferencesProfile";
			- _id = GUID e6cbeb25-2cf4-4c66-9c75-cbb99e029222;
		}
		{ IProfile 
			- fileName = "SysML";
			- _persistAs = "$OMROOT\\Profiles\\SysML\\SysMLProfile_rpy";
			- _id = GUID d9689b73-885e-44c4-896b-de43defa0a33;
			- _isReference = 1;
		}
		{ ISubsystem 
			- fileName = "GatewayProjectFiles";
			- _id = GUID a6ca3a64-92a1-4a2b-b824-1cdb9f5661f0;
		}
		{ ISubsystem 
			- fileName = "RequirementsAnalysisPkg";
			- _id = GUID a225e158-d38b-4900-b98e-97d73f017e2a;
		}
		{ IProfile 
			- fileName = "RequirementsAnalysisProfile";
			- _id = GUID ee68d643-007f-404c-89e5-7c61c7008170;
		}
	}
	- Diagrams = { IRPYRawContainer 
		- size = 0;
	}
	- Components = { IRPYRawContainer 
		- size = 1;
		- value = 
		{ IComponent 
			- fileName = "NotUsedCmp";
			- _id = GUID 15e00bf1-6846-47dc-acb4-6a0add0593b4;
		}
	}
}


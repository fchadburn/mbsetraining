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
	- Stereotypes = { IRPYRawContainer 
		- size = 1;
		- value = 
		{ IHandle 
			- _m2Class = "IStereotype";
			- _filename = "RequirementsAnalysisProfile.sbs";
			- _subsystem = "RequirementsAnalysisProfile";
			- _class = "";
			- _name = "SimpleMenu";
			- _id = GUID 47f1a7be-3b10-4ec3-9b4b-45b40969ee9b;
		}
	}
	- _modifiedTimeWeak = 3.30.2016::18:45:4;
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
		- size = 7;
		- value = 
		{ IProfile 
			- fileName = "SysMLHelperProfile";
			- _id = GUID 1f16e485-bfb5-4a1d-b046-ef4230d5c187;
		}
		{ IProfile 
			- fileName = "RequirementsAnalysisProfile";
			- _id = GUID ee68d643-007f-404c-89e5-7c61c7008170;
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
		{ IProfile 
			- fileName = "FunctionalAnalysisProfile";
			- _id = GUID a138619d-4c0d-42e8-a715-647195b23512;
		}
		{ ISubsystem 
			- fileName = "RequirementsAnalysisPkg";
			- _id = GUID a225e158-d38b-4900-b98e-97d73f017e2a;
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


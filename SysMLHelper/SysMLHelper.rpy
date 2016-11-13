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
			- _filename = "$OMROOT\\Profiles\\SysML\\SysMLProfile_rpy\\SysML.sbs";
			- _subsystem = "SysML";
			- _class = "";
			- _name = "SysML";
			- _id = GUID 052b8171-a32b-4f45-a829-5585f79f9deb;
		}
	}
	- _modifiedTimeWeak = 11.9.2016::7:5:1;
	- _lastID = 13;
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
		- size = 12;
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
		{ IProfile 
			- fileName = "FunctionalAnalysisProfile";
			- _id = GUID a138619d-4c0d-42e8-a715-647195b23512;
		}
		{ ISubsystem 
			- fileName = "RequirementsAnalysisPkg";
			- _id = GUID a225e158-d38b-4900-b98e-97d73f017e2a;
		}
		{ IProfile 
			- fileName = "RequirementsAnalysisProfile";
			- _id = GUID ee68d643-007f-404c-89e5-7c61c7008170;
		}
		{ ISubsystem 
			- fileName = "FunctionalAnalysisPkg";
			- _id = GUID f9b46480-d50d-44ea-badb-e0b5c851b7fc;
		}
		{ ISubsystem 
			- fileName = "DesignSynthesisPkg";
			- _id = GUID ef01473f-f4ad-4b16-90a4-a42254559db4;
		}
		{ IProfile 
			- fileName = "DesignSynthesisProfile";
			- _id = GUID 8867afa3-3659-4a6e-a860-1fc1426764d7;
		}
		{ ISubsystem 
			- fileName = "BasePkg";
			- _id = GUID c44ec7cb-7a75-45aa-92db-122629461c4b;
		}
		{ IProfile 
			- fileName = "FunctionalAnalysisSimpleProfile";
			- _id = GUID 5fffd2f7-d270-486c-9552-bf45c4bc162a;
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


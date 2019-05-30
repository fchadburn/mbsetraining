I-Logix-RPY-Archive version 8.13.0 C++ 9794446
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
	- _name = "TauMigrator";
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
	- _modifiedTimeWeak = 2.11.2019::7:56:33;
	- _lastID = 17;
	- _UserColors = { IRPYRawContainer 
		- size = 16;
		- value = 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 
	}
	- _defaultSubsystem = { ISubsystemHandle 
		- _m2Class = "ISubsystem";
		- _filename = "NotUsedPkg.sbs";
		- _subsystem = "";
		- _class = "";
		- _name = "NotUsedPkg";
		- _id = GUID f7c3249f-ad5e-495b-9fee-8615705d48cc;
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
		- size = 3;
		- value = 
		{ IProfile 
			- fileName = "SysML";
			- _persistAs = "$OMROOT\\Profiles\\SysML\\SysMLProfile_rpy";
			- _id = GUID d9689b73-885e-44c4-896b-de43defa0a33;
			- _partOfTheModelKind = referenceunit;
		}
		{ ISubsystem 
			- fileName = "NotUsedPkg";
			- _id = GUID f7c3249f-ad5e-495b-9fee-8615705d48cc;
		}
		{ IProfile 
			- fileName = "TauMigratorProfile";
			- _id = GUID 15a1b173-1be9-45f8-9b48-f29d7d6ce3d6;
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


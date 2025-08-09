[Setup]
AppName=Sampath Car Deco Shop
AppVersion=1.0
DefaultDirName={pf}\CarDecoShop
DefaultGroupName=Car Deco Shop
OutputDir=output
OutputBaseFilename=CarDecoInstaller
Compression=lzma
SolidCompression=yes

[Files]
Source: "ShopApp.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "lib\*"; DestDir: "{app}\lib"; Flags: recursesubdirs
Source: "resources\*"; DestDir: "{app}\resources"; Flags: recursesubdirs
Source: "database\*"; DestDir: "{app}\database"; Flags: recursesubdirs

[Icons]
Name: "{group}\Car Deco Shop"; Filename: "{app}\ShopApp.exe"
Name: "{group}\Uninstall Car Deco Shop"; Filename: "{uninstallexe}"

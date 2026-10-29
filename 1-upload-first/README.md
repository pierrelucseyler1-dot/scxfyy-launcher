# scxfyy launcher
## Launcher
1. Install Node.js 18+. The in-game menu build downloads its own JDK 21, so you do not need to install one.
2. In this folder: npm install, then npm start.
3. Sign in with Microsoft, pick a version, tick "Use Fabric" in Settings, Launch.
## In-game menu (scxfyy client mod, Minecraft 1.21.4 only)
Right Shift opens a searchable list of about 60 modules. Click = on/off, right click or the ... button = keybind and values. Cards marked "Coming soon" are not built yet.
1. Double-click build-mod.bat to build the mod. The launcher then adds it to every Fabric 1.21.4 instance on Play.
2. Press Play on a Fabric 1.21.4 instance, then Right Shift in game.
Left click a setting = on/off. Right click = change keybind and values.
Each Minecraft version has its own folder under ~/.scxfyy-launcher/game/instances.

Your Microsoft login is remembered (stored encrypted with Windows). Customize (top bar) has Sign out and the color themes.

## Getting the .exe (no command prompt)
You get two files: scxfyy-launcher.exe (double-click and it runs, nothing to install) and scxfyy-launcher-setup.exe (installs it with a shortcut).

### Way 1: build it online with GitHub (nothing to install on your PC)
1. Make a free account at github.com and create a new PUBLIC repository.
2. Click "uploading an existing file" and drag in everything from this folder (not node_modules).
3. If the .github folder did not upload: Add file > Create new file, name it .github/workflows/build.yml and paste in the text from build-online.yml.
4. Open the Actions tab. The build starts by itself (or click Run workflow). It takes about 10 minutes.
5. Open Releases on the right side of the repo page and download scxfyy-launcher.exe. Share that link with players.

### Way 2: build it on your PC
Double-click build-exe.bat. It downloads what it needs and puts the exe files in the dist folder.

Windows may show an "unknown publisher" warning because the exe is not code-signed. Click More info, then Run anyway.

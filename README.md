# Java-Racer-Online

A multiplayer racing game built with Java and JavaFX.

	- Features
		- 4 different car color options
		- Local and online multiplayer
		- 2 - 4 Players in the same game
		- Public and private chat functions
		- Realistic car location calculation

	- Controls
		W - Gas
		S - Break
		A - Steer left
		D - Steer right

	- Game start instruction
		- To start the game, click JavaRacerOnline.bat
			- Enter Server IP
			- Enter Password(if password is not set, leave the field empty)
			- Enter your nickname
			- Select car color
			- Click "Start" button

		- To host a race, click JRO_Server
			- Enter password(if password is not needed, leave the field empty)
			- Select the number of players
			- Enter the number of laps
			- Click "Start the game" button

	- Game configuration
		In-game
		- After entering all of the information press "Save  config" button in order to save the current configuration
		- In order to reset the game configuration to it's standard values click "Reset config" button
		
		Using game-configuration.xml
		- Open game-configuration.xml (if this file doesn't exist, it will be created after you start the game for the first time)
		- Alter needed settings and save the file
		- Reload both game and server in order for the new configuration to load
		
		Note
		Server port can only be changed using the game-configuration.xml file
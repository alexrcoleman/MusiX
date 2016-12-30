# MusiX v1.2.0 #
Your standard program for downloading legally bought music from video sites.

## Setup ##
Click the **MusiX.jar** file, and then click the **Raw** button on that page. Also, if the last update for the .jar file is ever older than the last update for any other files (you can see the last update for a file on the homepage on the right), tell me and I'll push an update to the .jar file (I might make changes and forget to export it to the jar)

Also, the readme tells you the latest version number, so you can check and make sure that matches the one at the top of your program. If not, download the new version!

## Newest Features ##
### Planned Updates ###
* Update the spotify album URI feature to work with newest API so you can add a whole album
* Add option to paste in spotify URI for a track
* Make the list of tracks filter based on what is currently being typed in the search field
* Autodetect available updates

### v1.2.0 ###
* Switched to a new downloader since the old one seems to be down
* Some other stuff that I can't remember
### v1.1.0 ###
* Updated to use spotify's latest API (old is now deprecated and won't work)
* Now adds disc number / track number counts more accurately
* Fixes bug where the iTunes button would only let you add instead of delete if the track is part of a multi-disc album
* Fixed bug where lyrics were fetched before updating the title/artist with spotify data
* Added album to spotify prompt to ensure the correct album is selected
* Forced spotify prompt to always show up when using the Scrape Metadata button, to let you pick the right album (used to not pop up if the name/artist matched perfectly)


### Older ###
* Metadata (information about the song like title and lyrics) will automatically write to the file before closing or adding to iTunes, so once all the information is added you don't have to wait for it to save
* Lyrics and album information will be searched for once using the title/artist detected from the youtube video title, and again when the **Scrape Metadata** button is pressed. If you change the title/artist, you can click this to update.
* You can now use the up and down arrow keys to cycle through different autocompletion options (ex. typing h gives "hotline bling", pressing down gives "hello adele". Press up again to get back to "hotline bling")


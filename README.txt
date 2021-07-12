This is a project that allows you to use SceneBuilder and JavaFX (the thing that we did before starting canvas) to create custom main menus.

This is so that you can create custom main menus easily and with a drag-and-drop system rather than manually and counting pixels.

It also lets you create animations in a more familiar setting rather than the new FXGL UI.

It's a nice addition to an otherwise annoying task, but it comes with three major caveats:
- You need to manually create buttons if they relate to the main game.
	- For example, a "Start Game" button needs to be created in the MainMenu file and in the title.fxml file.
	- Just type in "fire" in the MainMenu section to see a list of functions that this relates to.
- Everything is located under layers of panes, with the main pane being the only one that you can change.
	- The main pane has the id "aPane" in this example.
	- This means that some code likely won't work if you just copy and paste.
- It's often just easier to do the same code under a FXGL AnimationBuilder, as that has a lot of inbuilt functions that need to be coded manually in JavaFX
	- But the positioning of elements is a pain when you do it through code, so using SceneBuilder makes it quicker and easier to do custom placement.

Other than that, everything's basically the same as normal FXML files. 
Just use this and you can have changing backgrounds, firing bullets, and other cool stuff with something that you're more familiar with.
I've demonstrated this using an old color-changing background, but you can do anything with it.

Just use this to position the static elements, then go into MainMenu and do the animated elements by yourself.

There's also a bullet demonstration, but that's already there in Geometry Wars so you can just ignore that.
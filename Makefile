.PHONY: test run clean loader

SRC_DIR=gigglebot/src

loader:
	java -jar TriBot_Loader.jar 

test:
	javac -cp TRiBot.jar -d ~/Library/Application\ Support/tribot/bin/ gigglebot/scripts/TutorialScript.java
